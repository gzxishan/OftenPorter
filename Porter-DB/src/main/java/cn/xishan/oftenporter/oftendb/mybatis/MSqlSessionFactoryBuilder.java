package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/4.
 */
class MSqlSessionFactoryBuilder
{

    interface FileListener
    {
        void onGetFiles(File[] files) throws Exception;
    }

    interface BuilderListener
    {
        void onParse() throws Exception;

        void onBindAlias() throws Exception;

        boolean willCheckMapperFile();

        void listenFiles(FileListener fileListener) throws Exception;

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MSqlSessionFactoryBuilder.class);
    private byte[] configData;
    private boolean enableMapperOverride;
    private SqlSessionFactory sqlSessionFactory;
    private Set<BuilderListener> builderListenerSet = new HashSet<>();
    private ExecutorService executorService;
    private WatchService watchService;
    private boolean checkMapperFileChange;
    private boolean isDestroyed = false;
    private boolean isStarted = false;
    private Boolean metaDataTableColumnsToLowercase;
    private JSONObject dataSourceConf;
    private String dataSourceProperPrefix;
    private DataSource dataSourceObject;
    private String[] initSqls;
    private List<Interceptor> interceptors;
    private MyBatisOption.IMybatisStateListener mybatisStateListener;
    private MyBatisOption.IConnectionBridge connectionBridge;
    private Map<String, Set<String>> tableColumnsMap = new HashMap<>();

    private String id;

    Map<String, String> methodMap;

    synchronized void regNewMapper(BuilderListener builderListener) throws Exception
    {
        LOGGER.info("reg new mapper listener:{}", builderListener);
        builderListenerSet.add(builderListener);
        builderListener.onBindAlias();
        builderListener.onParse();

        if (watchService != null)
        {
            OftenTool.close(watchService);
            watchService = null;
        }
    }

    //检测文件修改
    private synchronized void regFileCheck() throws Exception
    {
        if (builderListenerSet.size() == 0 && !checkMapperFileChange)
        {
            return;
        }
        boolean willCheckFile = false;
        for (BuilderListener builderListener : builderListenerSet)
        {
            if (builderListener.willCheckMapperFile())
            {
                willCheckFile = true;
                break;
            }
        }
        if (!willCheckFile)
        {
            return;
        }

        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            });
        }
        this.watchService = FileSystems.getDefault().newWatchService();

        final WatchService finalWatchService = this.watchService;
        final boolean[] state = {true};
        String currentId = this.id;
        Set<String> reged = new HashSet<>();
        FileListener fileListener = files -> {
            synchronized (MSqlSessionFactoryBuilder.this)
            {
                if (files == null || !state[0]||!currentId.equals(MSqlSessionFactoryBuilder.this.id))
                {
                    return;
                }
                for (File file : files)
                {
                    File dir = file.getParentFile();
                    if (reged.contains(dir.getAbsolutePath()))
                    {
                        continue;
                    }
                    reged.add(dir.getAbsolutePath());
                    LOGGER.info("listen change of dir:{}", dir);

                    Paths.get(dir.getAbsolutePath()).register(finalWatchService, StandardWatchEventKinds.ENTRY_MODIFY);
                }
            }
        };


        for (BuilderListener listener : builderListenerSet)
        {
            if (listener.willCheckMapperFile())
            {
                listener.listenFiles(fileListener);
            }
        }


        executorService.execute(() -> {
            while (!isDestroyed)
            {
                try
                {
                    boolean willBuild = false;
                    WatchKey key = finalWatchService.take();
                    for (WatchEvent<?> event : key.pollEvents())
                    {
                        Path path = (Path) event.context();
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.info("change event:{}-->{}", path.toAbsolutePath(), event.kind());
                        }
                        willBuild = true;
                    }
                    if (willBuild)
                    {
                        state[0] = false;
                        synchronized (MSqlSessionFactoryBuilder.this)
                        {
                            if (currentId.equals(id))
                            {
                                try
                                {
                                    reload();
                                } catch (Throwable e)
                                {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                        }
                        break;
                    }
                    // 重设WatchKey
                    key.reset();
                } catch (Exception e)
                {
                    if (!(e instanceof ClosedWatchServiceException))
                    {
                        LOGGER.error(e.getMessage(), e);
                    }
                    if (isDestroyed || MSqlSessionFactoryBuilder.this.watchService == null)
                    {
                        LOGGER.info("closed WatchService!");
                        break;
                    }
                }

            }
        });

    }

    public String getId()
    {
        return id;
    }

    public synchronized void onStart(IConfigData configData) throws Throwable
    {
        if (isStarted)
        {
            return;
        }
        if (dataSourceConf == null && OftenTool.notEmpty(dataSourceProperPrefix))
        {
            JSONObject jsonObject = configData.getJSONByKeyPrefix(dataSourceProperPrefix);
            dataSourceConf = jsonObject;
            dataSourceProperPrefix = null;
        }

        reload();
        isStarted = true;
        isDestroyed = false;
        if (mybatisStateListener != null)
        {
            mybatisStateListener.onStart();
        }
    }

    public synchronized void onDestroy()
    {
        isDestroyed = true;
        isStarted = false;
        if (executorService != null)
        {
            executorService.shutdownNow();
            executorService = null;
        }
        OftenTool.close(watchService);
        watchService = null;
        if (mybatisStateListener != null)
        {
            mybatisStateListener.onDestroy();
        }
    }

    public MSqlSessionFactoryBuilder(MyBatisOption myBatisOption, byte[] configData)
    {
        this.metaDataTableColumnsToLowercase = myBatisOption.metaDataTableColumnsToLowercase;
        this.dataSourceObject = myBatisOption.dataSourceObject;
        this.initSqls = myBatisOption.initSqls;
        if (dataSourceObject == null)
        {
            this.dataSourceConf = myBatisOption.dataSource;
        }
        this.dataSourceProperPrefix = myBatisOption.dataSourceProperPrefix;
        this.enableMapperOverride = myBatisOption.enableMapperOverride;
        this.checkMapperFileChange = myBatisOption.checkMapperFileChange;
        this.configData = configData;
        this.interceptors = myBatisOption.interceptors;
        this.mybatisStateListener = myBatisOption.mybatisStateListener;
        this.connectionBridge = myBatisOption.iConnectionBridge;
        if (myBatisOption.javaFuns != null)
        {
            methodMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : myBatisOption.javaFuns.entrySet())
            {
                methodMap.put(entry.getKey(), entry.getValue().getName());
            }
        }
    }

    public synchronized void reload() throws Throwable
    {
        id = OftenKeyUtil.randomUUID();
        LOGGER.info("start reload mybatis...");
        if (this.watchService != null)
        {
            WatchService watchService = this.watchService;
            this.watchService = null;
            watchService.close();
        }
        tableColumnsMap.clear();
        build();
        regListener();
        LOGGER.info("reload mybatis complete!");
    }

    public synchronized JSONObject getDataSourceConf()
    {
        return this.dataSourceConf;
    }

    public synchronized DataSource setDataSourceConf(JSONObject dataSourceConf) throws Throwable
    {
        if (dataSourceConf == null)
        {
            throw new NullPointerException();
        }
        DataSource last = this.dataSourceObject;
        this.dataSourceObject = null;
        this.dataSourceConf = dataSourceConf;
        reload();
        if (connectionBridge != null)
        {
            connectionBridge.onDataSourceChanged(this.dataSourceObject);
        }
        return last;
    }

    public synchronized DataSource setDataSource(DataSource dataSource) throws Throwable
    {
        if (dataSource == null)
        {
            throw new NullPointerException();
        }
        if (dataSource == this.dataSourceObject)
        {
            return dataSource;
        }
        DataSource last = this.dataSourceObject;
        this.dataSourceObject = dataSource;
        this.reload();//重新加载
        if (connectionBridge != null)
        {
            connectionBridge.onDataSourceChanged(dataSource);
        }
        return last;
    }

    public synchronized DataSource getDataSource()
    {
        return dataSourceObject;
    }

    private void regListener()
    {
        LOGGER.info("will rereg...");
        try
        {
            if (mybatisStateListener != null)
            {
                mybatisStateListener.beforeReload();
            }
            regFileCheck();
            if (mybatisStateListener != null)
            {
                mybatisStateListener.afterReload();
            }
        } catch (Exception e)
        {
            if (mybatisStateListener != null)
            {
                mybatisStateListener.onReloadFailed(e);
            }
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void build() throws Throwable
    {
        SqlSessionFactory _sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(new ByteArrayInputStream(configData));
        Configuration configuration = _sqlSessionFactory.getConfiguration();
        if (enableMapperOverride)
        {
            ConfigurationHandle.setForOverride(configuration);
        }
        DataSource dataSource = this.dataSourceObject;

        Environment.Builder builder = new Environment.Builder(MyBatisBridge.class.getName());
        builder.transactionFactory(new JdbcTransactionFactory());
        if (dataSource == null)
        {
            JSONObject dataSourceConf = this.dataSourceConf;
            dataSource = MyBatisBridge.buildDataSource(dataSourceConf);
        }
        this.dataSourceObject = dataSource;
        builder.dataSource(dataSource);
        Environment environment = builder.build();

        if (initSqls != null)
        {
            String[] sqls = initSqls;
            initSqls = null;

            try (Connection connection = dataSource.getConnection())
            {
                for (String sql : sqls)
                {
                    LOGGER.debug("init sql={}", sql);
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.execute();
                    ps.close();
                }
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }


        configuration.setEnvironment(environment);
        if (interceptors != null)
        {
            for (Interceptor interceptor : interceptors)
            {
                configuration.addInterceptor(interceptor);
            }
        }

        this.sqlSessionFactory = _sqlSessionFactory;
        for (BuilderListener listener : builderListenerSet)
        {
            listener.onBindAlias();
        }
        for (BuilderListener listener : builderListenerSet)
        {
            listener.onParse();
        }
    }


    /**
     * 得到数据表的字段列表
     *
     * @param tableName
     * @return
     */
    public synchronized Set<String> getTableColumns(String tableName)
    {
        if (tableName == null)
        {
            return Collections.emptySet();
        }
        Set<String> columnsSet = tableColumnsMap.get(tableName);
        if (columnsSet == null)
        {
            try (Connection connection = getDataSource().getConnection())
            {
                DatabaseMetaData metaData = connection.getMetaData();
                Set<String> set = new HashSet<>();
                ResultSet rs = metaData.getColumns(null, metaData.getUserName(), tableName, "%");
                boolean has = false;
                if (!rs.next())
                {
                    rs.close();
                    rs = metaData.getColumns(null, "%", tableName, "%");
                    if (!rs.next())
                    {
                        rs.close();
                        rs = metaData.getColumns(null, null, tableName, "%");
                        has = rs.next();
                    } else
                    {
                        has = true;
                    }
                } else
                {
                    has = true;
                }

                if (has)
                {
                    do
                    {
                        String colName = rs.getString("COLUMN_NAME");
                        if (metaDataTableColumnsToLowercase != null)
                        {
                            if (metaDataTableColumnsToLowercase)
                            {
                                colName = colName.toLowerCase();
                            } else
                            {
                                colName = colName.toUpperCase();
                            }
                        }
                        set.add(colName);
                    } while (rs.next());
                }
                tableColumnsMap.put(tableName, set);
                columnsSet = set;
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return columnsSet == null ? Collections.emptySet() : columnsSet;
    }

    public synchronized void addListener(BuilderListener builderListener)
    {
        builderListenerSet.add(builderListener);
    }

    public SqlSessionFactory getFactory()
    {
        return sqlSessionFactory;
    }
}
