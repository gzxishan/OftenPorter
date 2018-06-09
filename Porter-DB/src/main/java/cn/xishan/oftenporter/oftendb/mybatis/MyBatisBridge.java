package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.TransactionConfig;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlTransactionConfig;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisBridge
{

    private static MybatisConfig mybatisConfig;

    static void start()
    {
        mybatisConfig.start();
    }

    static void destroy()
    {
        mybatisConfig.start();
    }

    static MybatisConfig.MOption getMOption(String source)
    {
        if (mybatisConfig == null)
        {
            throw new InitException("not init!");
        }
        return mybatisConfig.getOption(source);
    }

    /**
     * 见{@linkplain MyBatisOption#dataSource}
     *
     * @param propertiesJson dsType{@linkplain DataSource}实现类.
     * @return
     */
    public static DataSource buildDataSource(JSONObject propertiesJson)
    {
        JSONObject json = propertiesJson;
        propertiesJson = new JSONObject();
        propertiesJson.putAll(json);
        String dsType = (String) propertiesJson.remove("dsType");
        Properties properties = new Properties();
        for (String key : propertiesJson.keySet())
        {
            if (key.endsWith("--ignore"))
            {
                continue;
            }
            properties.setProperty(key, propertiesJson.getString(key));
        }
        return buildDataSource(dsType, properties);
    }


    private static class TempFactory extends UnpooledDataSourceFactory
    {
        public TempFactory(DataSource dataSource)
        {
            super.dataSource = dataSource;
        }
    }

    /**
     * @param dataSourceClass {@linkplain DataSource}实现类.
     * @param properties
     * @return
     */
    public static DataSource buildDataSource(String dataSourceClass, Properties properties)
    {
        try
        {
            Class<?> clazz = PackageUtil.newClass(dataSourceClass, null);
            DataSource dataSource = (DataSource) WPTool.newObject(clazz);
            DataSourceFactory factory = new TempFactory(dataSource);
            factory.setProperties(properties);
            return factory.getDataSource();
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public static void init(PorterConf porterConf, MyBatisOption myBatisOption, String resourcePath) throws IOException
    {
        InputStream in = Resources.getResourceAsStream(resourcePath);
        init(porterConf, myBatisOption, in);
    }

    public synchronized static void init(PorterConf porterConf, MyBatisOption myBatisOption, InputStream configStream)
    {
        if (myBatisOption == null)
        {
            throw new NullPointerException(MyBatisOption.class.getSimpleName() + " is null!");
        }
        try
        {

            byte[] configData = FileTool.getData(configStream, 1024);
            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder = new MSqlSessionFactoryBuilder(
                    myBatisOption, configData);
            if (mybatisConfig == null)
            {
                mybatisConfig = new MybatisConfig();
            }

            if (myBatisOption.resourcesDir != null && !myBatisOption.resourcesDir.endsWith("/"))
            {
                myBatisOption.resourcesDir += "/";
            }
            mybatisConfig.put(myBatisOption, mSqlSessionFactoryBuilder);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public static void startTransaction(WObject wObject, SqlTransactionConfig sqlTransactionConfig)
    {
        DBCommon.startTransaction(wObject, newDBSource(wObject, MyBatisOption.DEFAULT_SOURCE), sqlTransactionConfig);
    }

    public static boolean rollbackTransaction(WObject wObject)
    {
        return DBCommon.rollbackTransaction(wObject);
    }

    public static boolean commitTransaction(WObject wObject)
    {
        return DBCommon.commitTransaction(wObject);
    }

    public static boolean closeTransaction(WObject wObject)
    {
        return DBCommon.closeTransaction(wObject);
    }

    public static TransactionHandle<Void> getTransactionHandle(WObject wObject, String source)
    {
        TransactionHandle<Void> handle = new TransactionHandle<Void>()
        {
            @Override
            public Void common()
            {
                return null;
            }

            @Override
            public void startTransaction(TransactionConfig config) throws DBException
            {
                DBCommon.startTransaction(wObject, newDBSource(wObject, source), config);
            }

            @Override
            public void commitTransaction() throws DBException
            {
                DBCommon.commitTransaction(wObject);
            }

            @Override
            public void rollback() throws DBException
            {
                DBCommon.rollbackTransaction(wObject);
            }

            @Override
            public void close() throws IOException
            {
                DBCommon.closeTransaction(wObject);
            }
        };

        return handle;

    }

    static SqlSession openSession(@NotNull WObject wObject, String source)
    {
        if (wObject == null)
        {
            throw new NullPointerException(WObject.class.getSimpleName() + " is null!");
        }
        return _openSession(wObject, source);
    }

    static SqlSession _openSession(@MayNull WObject wObject, String source)
    {
        SqlSession sqlSession = wObject == null ? null : wObject.original().getRequestData(SqlSession.class);

        if (sqlSession == null)
        {
            MybatisConfig.MOption mOption = getMOption(source);
            MSqlSessionFactoryBuilder sqlSessionFactoryBuilder = mOption.mSqlSessionFactoryBuilder;
            MyBatisOption myBatisOption = mOption.myBatisOption;
            sqlSession = sqlSessionFactoryBuilder.getFactory().openSession(myBatisOption.autoCommit);
            if (wObject != null)
            {
                wObject.putRequestData(SqlSession.class, sqlSession);
                if (wObject.isSupportAfterInvokeListener())
                {
                    wObject.addAfterInvokeListener(object -> {
                        SqlSession session = wObject.getRequestData(SqlSession.class);
                        if (session != null)
                        {
                            session.close();
                        }
                    });
                }
            }
        }
        return sqlSession;
    }

    private static DBSource newDBSource(WObject wObject, String source)
    {
        DBHandle dbHandle = new DBHandleOnlyTS(openSession(wObject, source));
        DBSource dbSource = new DBSource()
        {
            @Override
            public DBSource newInstance()
            {
                throw new RuntimeException("stub!");
            }

            @Override
            public DBSource newInstance(ConfigToDo configToDo)
            {
                throw new RuntimeException("stub!");
            }

            @Override
            public Condition newCondition()
            {
                throw new RuntimeException("stub!");
            }

            @Override
            public void afterClose(DBHandle dbHandle)
            {

            }

            @Override
            public DBHandle getDBHandle() throws DBException
            {
                return dbHandle;
            }

            @Override
            public Configed getConfiged()
            {
                throw new RuntimeException("stub!");
            }

            @Override
            public ConfigToDo getConfigToDo()
            {
                throw new RuntimeException("stub!");
            }
        };
        return dbSource;
    }


    public static CheckPassable autoTransaction(TransactionConfirm confirm)
    {
        return autoTransaction(confirm, MyBatisOption.DEFAULT_SOURCE);
    }

    public static CheckPassable autoTransaction(TransactionConfirm confirm, String source)
    {

        TransactionConfirm transactionConfirm = new TransactionConfirm()
        {
            @Override
            public boolean needTransaction(WObject wObject, DuringType type, CheckHandle checkHandle)
            {
                return confirm.needTransaction(wObject, type, checkHandle);
            }

            @Override
            public TConfig getTConfig(WObject wObject, DuringType type, CheckHandle checkHandle)
            {
                TConfig tConfig = confirm.getTConfig(wObject, type, checkHandle);
                tConfig.dbSource = newDBSource(wObject, source);
                return tConfig;
            }
        };
        _AutoTransactionCheckPassable checkPassable = new _AutoTransactionCheckPassable(transactionConfirm);
        return checkPassable;
    }
}
