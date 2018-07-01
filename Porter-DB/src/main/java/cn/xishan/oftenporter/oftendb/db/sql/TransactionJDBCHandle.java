package cn.xishan.oftenporter.oftendb.db.sql;

import cn.xishan.oftenporter.oftendb.annotation.TransactionJDBC;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class TransactionJDBCHandle extends AspectOperationOfNormal.HandleAdapter<TransactionJDBC>
{
    @AutoSet
    IConfigData configData;
    private TransactionJDBC transactionJDBC;

    private static final Method __openSession;

    static
    {
        try
        {
            __openSession = MyBatisBridge.class.getMethod("__openSession", String.class);
            __openSession.setAccessible(true);
        } catch (NoSuchMethodException e)
        {
            throw new InitException(e);
        }
    }

    private static final ThreadLocal<Map<String, IConnection>> threadLocal = ThreadLocal.withInitial(
            () -> new ConcurrentHashMap<>(1));

    public static IConnection __getConnection__(String source)
    {
        return threadLocal.get().get(source);
    }

    public static void __setConnection__(String source, IConnection connection)
    {
        threadLocal.get().put(source, connection);
    }

    public static void __removeConnection__(String source)
    {
        threadLocal.get().remove(source);
    }

    @Override
    public boolean init(TransactionJDBC current, IConfigData configData, Object originObject,
            Method originMethod) throws Exception
    {
        this.transactionJDBC = current;
        if ("mybatis".equals(transactionJDBC.type()))
        {

        } else
        {
            throw new RuntimeException("unknown type:" + transactionJDBC.type());
        }
        return configData.getBoolean("enableTransactionJDBC", true);
    }

    @Override
    public boolean preInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn) throws Throwable
    {
        if (isTop)
        {
            if ("mybatis".equals(transactionJDBC.type()))
            {
                __openSession.invoke(null, transactionJDBC.dbSource());
            }
        }
        IConnection iConnection = threadLocal.get().get(transactionJDBC.dbSource());
        if (iConnection != null)
        {
            Connection connection = iConnection.getConnection();
            connection.setAutoCommit(false);
            connection.setReadOnly(transactionJDBC.readonly());
            if (transactionJDBC.level() != TransactionJDBC.Level.DEFAULT)
            {
                connection.setTransactionIsolation(transactionJDBC.level().getLevel());
            }
        }
        return false;
    }

    @Override
    public Object onEnd(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object lastFinalReturn) throws Throwable
    {
        if (isTop)
        {
            IConnection iConnection = threadLocal.get().get(transactionJDBC.dbSource());
            if (iConnection != null)
            {
                __removeConnection__(transactionJDBC.dbSource());
                iConnection.getConnection().commit();
            }
        }

        return lastFinalReturn;
    }

    @Override
    public void onException(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Throwable throwable) throws Throwable
    {
        if (isTop)
        {
            IConnection iConnection = threadLocal.get().get(transactionJDBC.dbSource());
            if (iConnection != null)
            {
                __removeConnection__(transactionJDBC.dbSource());
                iConnection.getConnection().rollback();
            }
        }

    }
}
