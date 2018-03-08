package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private SqlSessionFactory sqlSessionFactory;
    private Set<BuilderListener> builderListenerSet = new HashSet<>();
    private ExecutorService executorService;
    private WatchService watchService;
    private boolean checkMapperFileChange;
    private boolean isDestroyed = false;
    private boolean isStarted = false;
    private boolean needReRegFileCheck = false;
    private JSONObject dataSourceConf;
    private List<Interceptor> interceptors;
    private Environment environment;

    synchronized void regNewMapper(BuilderListener builderListener) throws Exception
    {
        LOGGER.debug("reg new mapper listener:{}", builderListener);
        builderListenerSet.add(builderListener);
        builderListener.onBindAlias();
        builderListener.onParse();

        if (watchService != null)
        {
            WPTool.close(watchService);
            watchService = null;
        }
        needReRegFileCheck = true;
    }

    //检测文件修改
    private synchronized void regFileCheck() throws Exception
    {
        if (builderListenerSet.size() == 0 && !checkMapperFileChange)
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
                LOGGER.debug("listen change of dir:{}", dir);
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
                            LOGGER.debug("change event:{}-->{}", path.toAbsolutePath(), event.kind());
                        }
//                        String filePath = path.toAbsolutePath().toFile().getAbsolutePath();
//                        if (regedNames.contains(filePath))
//                        {
//                            willBuild = true;
//                            break;
//                        }
                        willBuild = true;
                        break;
                    }
                    if (willBuild)
                    {
                        try
                        {
                            LOGGER.debug("start reload mybatis...");
                            build();
                            LOGGER.debug("reload mybatis complete!");
                        } catch (Exception e)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                        state[0] = false;
                        needReRegFileCheck=true;
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
                        LOGGER.debug("closed WatchService!");
                        break;
                    }
                }

            }

            if (needReRegFileCheck)
            {
                needReRegFileCheck = false;
                LOGGER.debug("will rereg...");
                try
                {
                    regFileCheck();
                } catch (Exception e)
                {
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
        isStarted = true;
        isDestroyed = false;
        regFileCheck();
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
        WPTool.close(watchService);
        watchService = null;
    }

    public MSqlSessionFactoryBuilder(MyBatisOption myBatisOption, byte[] configData)
    {
        this.dataSourceConf = myBatisOption.dataSource;
        this.checkMapperFileChange = myBatisOption.checkMapperFileChange;
        this.configData = configData;
        this.interceptors = myBatisOption.interceptors;
    }

    public synchronized void build() throws Exception
    {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(new ByteArrayInputStream(configData));

        if (dataSourceConf != null)
        {
            JSONObject dataSourceConf = this.dataSourceConf;
            this.dataSourceConf = null;
            Environment.Builder builder = new Environment.Builder(MyBatisBridge.class.getName());
            builder.transactionFactory(new JdbcTransactionFactory());
            builder.dataSource(MyBatisBridge.buildDataSource(dataSourceConf));
            this.environment = builder.build();
        }

        if (environment != null)
        {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            configuration.setEnvironment(environment);
            if (interceptors != null)
            {
                for (Interceptor interceptor : interceptors)
                {
                    configuration.addInterceptor(interceptor);
                }
            }
        }

        this.sqlSessionFactory = sqlSessionFactory;
        for (BuilderListener listener : builderListenerSet)
        {
            listener.onBindAlias();
        }
        for (BuilderListener listener : builderListenerSet)
        {
            listener.onParse();
        }
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
