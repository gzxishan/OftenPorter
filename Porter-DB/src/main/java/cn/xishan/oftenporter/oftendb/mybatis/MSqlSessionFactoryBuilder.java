package cn.xishan.oftenporter.oftendb.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/4.
 */
class MSqlSessionFactoryBuilder
{

    interface BuilderListener
    {
        void onBuild() throws Exception;

        boolean isMapperFileChange();

        boolean willCheckMapperFile();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MSqlSessionFactoryBuilder.class);
    private byte[] configData;
    private SqlSessionFactory sqlSessionFactory;
    private Set<BuilderListener> builderListenerSet = new HashSet<>();
    private ScheduledExecutorService scheduledExecutorService;
    private int checkMapperFileDelaySeconds;


    public MSqlSessionFactoryBuilder(int checkMapperFileDelaySeconds, byte[] configData)
    {
        this.checkMapperFileDelaySeconds = checkMapperFileDelaySeconds;
        this.configData = configData;
    }

    public void build() throws Exception
    {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(new ByteArrayInputStream(configData));
        this.sqlSessionFactory = sqlSessionFactory;
        for (BuilderListener listener : builderListenerSet)
        {
            listener.onBuild();
        }
    }

    public void addListener(BuilderListener builderListener)
    {
        builderListenerSet.add(builderListener);
        if (builderListener.willCheckMapperFile() && scheduledExecutorService == null)
        {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            });
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                        boolean willBuild = false;
                        for (BuilderListener listener : builderListenerSet)
                        {
                            if (listener.willCheckMapperFile()&&listener.isMapperFileChange())
                            {
                                willBuild = true;
                                break;
                            }
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
                        }
                    }, checkMapperFileDelaySeconds, checkMapperFileDelaySeconds,
                    TimeUnit.SECONDS);
        }
    }

    public SqlSessionFactory getFactory()
    {
        return sqlSessionFactory;
    }
}
