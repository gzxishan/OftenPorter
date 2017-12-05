package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.TransactionConfig;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlTransactionConfig;
import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public class MyBatisBridge
{


    public static void init(PorterConf porterConf, MyBatisOption myBatisOption, String resourcePath) throws IOException
    {
        InputStream in = Resources.getResourceAsStream(resourcePath);
        init(porterConf, myBatisOption, in);
    }

    public static void init(PorterConf porterConf, MyBatisOption myBatisOption, InputStream configStream)
    {
        if (myBatisOption == null)
        {
            throw new NullPointerException(MyBatisOption.class.getSimpleName() + " is null!");
        }
        try
        {

            byte[] configData = FileTool.getData(configStream, 1024);

            MSqlSessionFactoryBuilder mSqlSessionFactoryBuilder = new MSqlSessionFactoryBuilder(
                    myBatisOption.checkMapperFileDelaySeconds, configData);
            mSqlSessionFactoryBuilder.build();

            porterConf.addContextAutoSet(MSqlSessionFactoryBuilder.class, mSqlSessionFactoryBuilder);
            porterConf.addContextAutoSet(MyBatisOption.class, myBatisOption);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public static void startTransaction(WObject wObject, SqlTransactionConfig sqlTransactionConfig)
    {
        DBCommon.startTransaction(wObject, newDBSource(wObject), sqlTransactionConfig);
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

    public static TransactionHandle<Void> getTransactionHandle(WObject wObject)
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
                DBCommon.startTransaction(wObject, newDBSource(wObject), config);
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

    static SqlSession openSession(WObject wObject)
    {
        SqlSession sqlSession = wObject.getAttribute(SqlSession.class);

        if (sqlSession == null)
        {
            MSqlSessionFactoryBuilder sqlSessionFatoryBuilder = wObject.savedObject(MSqlSessionFactoryBuilder.class);
            MyBatisOption myBatisOption = wObject.savedObject(MyBatisOption.class);
            sqlSession = sqlSessionFatoryBuilder.getFactory().openSession(myBatisOption.autoCommit);
            wObject.setAttribute(SqlSession.class, sqlSession);
        }
        return sqlSession;
    }

    private static DBSource newDBSource(WObject wObject)
    {
        DBHandle dbHandle = new DBHandleOnlyTS(openSession(wObject));
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
                tConfig.dbSource = newDBSource(wObject);
                return tConfig;
            }
        };
        _AutoTransactionCheckPassable checkPassable = new _AutoTransactionCheckPassable(transactionConfirm);
        return checkPassable;
    }
}
