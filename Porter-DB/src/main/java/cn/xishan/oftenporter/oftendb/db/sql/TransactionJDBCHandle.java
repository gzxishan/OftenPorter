package cn.xishan.oftenporter.oftendb.db.sql;

import cn.xishan.oftenporter.oftendb.annotation.TransactionJDBC;
import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class TransactionJDBCHandle extends AspectOperationOfNormal.HandleAdapter<TransactionJDBC>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionJDBCHandle.class);

    private TransactionJDBC transactionJDBC;
    private String source;

    private static final Method __openSession;

    static
    {
        try
        {
            __openSession = MyBatisBridge.class.getDeclaredMethod("__openSession", String.class);
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
        this.source = transactionJDBC.dbSource();
        return configData.getBoolean("enableTransactionJDBC", true);
    }

    @Override
    public boolean preInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, boolean hasInvoked,
            Object lastReturn) throws Throwable
    {
        IConnection iConnection = threadLocal.get().get(source);
        if (iConnection == null || iConnection.willStartTransactionOk())
        {
            if ("mybatis".equals(transactionJDBC.type()))
            {
                LOGGER.debug("open source({}) for mybatis...", source);
                Object rs = __openSession.invoke(null, source);
                LOGGER.debug("opened source({}) for mybatis:{}", source, rs);
            }

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("start transaction:source={},queryTimeoutSeconds={},readonly={},level={},method={}.{}",
                        source, transactionJDBC.queryTimeoutSeconds(), transactionJDBC.readonly(),
                        transactionJDBC.level(), originMethod.getDeclaringClass().getName(), originMethod.getName());
            }
            iConnection = threadLocal.get().get(source);
            if (transactionJDBC.queryTimeoutSeconds() != -1)
            {
                iConnection.setQueryTimeoutSeconds(transactionJDBC.queryTimeoutSeconds());
            }
            Connection connection = iConnection.getConnection();
            connection.setAutoCommit(false);
            iConnection.startTransactionOk();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("start transaction ok:source={},method={}", source, originMethod.getName());
            }
        }

        Connection connection = iConnection.getConnection();
        connection.setReadOnly(transactionJDBC.readonly());
        if (transactionJDBC.level() != Isolation.DEFAULT)
        {
            connection.setTransactionIsolation(transactionJDBC.level().getLevel());
        }

        iConnection.setEndCommit(transactionJDBC.endCommit());
        return false;
    }

    private void checkCommit(Method originMethod, boolean isAfterOrEnd) throws Throwable
    {
        IConnection iConnection = threadLocal.get().get(transactionJDBC.dbSource());
        if (iConnection != null && (iConnection.isEndCommit() && !isAfterOrEnd || !iConnection
                .isEndCommit() && isAfterOrEnd))
        {
            if (iConnection.willCommit())
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("commit... transaction:source={},method={}.{}", source,
                            originMethod.getDeclaringClass().getName(), originMethod.getName());
                }
                __removeConnection__(transactionJDBC.dbSource());
                iConnection.doCommit();
                LOGGER.debug("commit-ok transaction:source={},method={}", source, originMethod.getName());
            }
        }
    }

    @Override
    public Object afterInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn) throws Throwable
    {
        checkCommit(originMethod, true);
        return lastReturn;
    }

    @Override
    public void onEnd(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object lastFinalReturn) throws Throwable
    {
        checkCommit(originMethod, false);
    }

    @Override
    public void onException(WObject wObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Throwable throwable) throws Throwable
    {

        IConnection iConnection = threadLocal.get().get(transactionJDBC.dbSource());
        if (iConnection != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("rollback... transaction:source={},method={}.{},errmsg={}", source,
                        originMethod.getDeclaringClass().getName(), originMethod.getName(),
                        WPTool.getCause(throwable).toString());
            }
            __removeConnection__(transactionJDBC.dbSource());
            iConnection.doRollback();
            LOGGER.debug("rollback-finished transaction:source={},method={}", source, originMethod.getName());
        }
    }
}
