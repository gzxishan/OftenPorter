package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/6.
 */
class MybatisConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisConfig.class);

    static class MOption
    {
        MyBatisOption myBatisOption;
        MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder;
        MyBatisOption.IConnectionBridge iConnectionBridge;

        public MOption(MyBatisOption myBatisOption,
                MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder)
        {
            this.myBatisOption = myBatisOption;
            this.mSqlSessionFactoryBuilder = mSqlSessionFactoryBuilder;
            this.iConnectionBridge = myBatisOption.iConnectionBridge;
        }
    }


    private Map<String, MOption> optionMap;
    //确保至少有一个Dao，从而会进行初始化
    @AutoSet
    MyBatisDao myBatisDao;

    public MybatisConfig()
    {
        optionMap = new HashMap<>();
    }

    public void start(IConfigData configData)
    {
        for (MOption mOption : optionMap.values())
        {
            try
            {
                mOption.mSqlSessionFactoryBuilder.onStart(configData);
            } catch (Throwable e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public void destroy()
    {
        for (MOption mOption : optionMap.values())
        {
            try
            {
                mOption.mSqlSessionFactoryBuilder.onDestroy();
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public MOption getOption(String source)
    {
        return optionMap.get(source);
    }

    public void put(MyBatisOption myBatisOption,
            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder)
    {
        myBatisOption = myBatisOption.clone();
        MOption mOption = optionMap.put(myBatisOption.source, new MOption(myBatisOption, mSqlSessionFactoryBuilder));
        if (mOption != null)
        {
            LOGGER.warn("already add source:{}", myBatisOption.source);
        } else
        {
            LOGGER.debug("add source:{}", myBatisOption.source);
        }
    }
}
