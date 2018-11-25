package cn.xishan.oftenporter.oftendb.mybatis;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private boolean needReRegFileCheck = false;
    private JSONObject dataSourceConf;
    private DataSource dataSourceObject;
    private String[] initSqls;
    private List<Interceptor> interceptors;
    private MyBatisOption.IMybatisStateListener mybatisStateListener;
    private Environment environment;
    private Map<String, Set<String>> tableColumnsMap = new HashMap<>();

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
        needReRegFileCheck = true;
    }

    //检测文件修改
    private synchronized void regFileCheck(boolean isForInit) throws Exception
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
        //final Set<String> regedNames = new HashSet<>();
        Set<String> reged = new HashSet<>();
        FileListener fileListener = files -> {
            if (files == null || !state[0])
            {
                return;
            }
            for (File file : files)
            {
                //regedNames.add(file.getAbsolutePath());
                File dir = file.getParentFile();
                if (reged.contains(dir.getAbsolutePath()))
                {
                    continue;
                }
                reged.add(dir.getAbsolutePath());
                LOGGER.info("listen change of dir:{}", dir);
                synchronized (this)
                {
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
//                        String filePath = path.toAbsolutePath().toFile().getAbsolutePath();
//                        if (regedNames.contains(filePath))
//                        {
//                            willBuild = true;
//                            break;
//                        }
                        willBuild = true;
                        //break;
                    }
                    if (willBuild)
                    {
                        Thread.sleep(1000);
                        try
                        {
                            LOGGER.info("start reload mybatis...");
                            build();
                            LOGGER.info("reload mybatis complete!");
                        } catch (Exception e)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                        state[0] = false;
                        needReRegFileCheck = true;
                        watchService.close();
                        MSqlSessionFactoryBuilder.this.watchService = null;
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

            if (needReRegFileCheck)
            {
                needReRegFileCheck = false;
                LOGGER.info("will rereg...");
                try
                {
                    if (!isForInit && mybatisStateListener != null)
                    {
                        mybatisStateListener.beforeReload();
                    }
                    regFileCheck(false);
                    if (!isForInit && mybatisStateListener != null)
                    {
                        mybatisStateListener.afterReload();
                    }
                } catch (Exception e)
                {
                    if (!isForInit && mybatisStateListener != null)
                    {
                        mybatisStateListener.onReloadFailed(e);
                    }
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

    }

    public synchronized void onStart() throws Exception
    {
        if (isStarted)
        {
            return;
        }
        build();
        regFileCheck(true);
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
        this.dataSourceObject = myBatisOption.dataSourceObject;
        this.initSqls = myBatisOption.initSqls;
        if (dataSourceObject == null)
        {
            this.dataSourceConf = myBatisOption.dataSource;
        }
        this.enableMapperOverride = myBatisOption.enableMapperOverride;
        this.checkMapperFileChange = myBatisOption.checkMapperFileChange;
        this.configData = configData;
        this.interceptors = myBatisOption.interceptors;
        this.mybatisStateListener = myBatisOption.mybatisStateListener;
        if (myBatisOption.javaFuns != null)
        {
            methodMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : myBatisOption.javaFuns.entrySet())
            {
                methodMap.put(entry.getKey(), entry.getValue().getName());
            }
        }
    }

    public synchronized void build() throws Exception
    {
        SqlSessionFactory _sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(new ByteArrayInputStream(configData));
        Configuration configuration = _sqlSessionFactory.getConfiguration();
        if (enableMapperOverride)
        {
            ConfigurationHandle.setForOverride(configuration);
        }

        DataSource dataSource = null;
        if (environment != null)
        {
            dataSource = environment.getDataSource();
        }

        JSONObject dataSourceConf = this.dataSourceConf;
        Environment.Builder builder = new Environment.Builder(MyBatisBridge.class.getName());
        builder.transactionFactory(new JdbcTransactionFactory());
        if (dataSource == null)
        {
            dataSource = dataSourceObject != null ? dataSourceObject : MyBatisBridge.buildDataSource(dataSourceConf);
        }
        builder.dataSource(dataSource);
        this.environment = builder.build();

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

    private static String getSchema(Connection conn) throws Exception
    {
        String schema = conn.getMetaData().getUserName();
        if (OftenTool.isEmpty(schema))
        {
            throw new Exception("ORACLE数据库模式不允许为空");
        }
        return schema.toUpperCase();

    }

    /**
     * 得到数据表的字段列表
     *
     * @param tableName
     * @return
     */
    public synchronized Set<String> getTableColumns(String tableName)
    {
        if(tableName==null){
            return Collections.emptySet();
        }
        Set<String> columnsSet = tableColumnsMap.get(tableName);
        if (columnsSet == null)
        {
            try (Connection connection = environment.getDataSource().getConnection())
            {
                Set<String> set = new HashSet<>();
                ResultSet rs = connection.getMetaData().getColumns(null, getSchema(connection), tableName, "%");
                while (rs.next())
                {
                    String colName = rs.getString("COLUMN_NAME");
                    set.add(colName);
                }
                tableColumnsMap.put(tableName,set);
                columnsSet=set;
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
