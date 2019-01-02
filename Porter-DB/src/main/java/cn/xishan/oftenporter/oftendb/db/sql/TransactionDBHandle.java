package cn.xishan.oftenporter.oftendb.db.sql;

import cn.xishan.oftenporter.oftendb.annotation.TransactionDB;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class TransactionDBHandle extends AspectOperationOfNormal.HandleAdapter<TransactionDB>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDBHandle.class);

    private TransactionDB transactionDB;
    private String source;

    private static final Method __openConnection;

    static
    {
        try
        {
            __openConnection = MyBatisBridge.class.getDeclaredMethod("__openConnection", String.class);
            __openConnection.setAccessible(true);
        } catch (NoSuchMethodException e)
        {
            throw new InitException(e);
        }
    }

    static class SavePointHolder
    {
        Savepoint savepoint;
    }

    //!!!!!!:防止获取已经过时的连接，记得清楚、同时获取时判断是否已经closed
    private static final ThreadLocal<Map<String, IConnection>> threadLocal = ThreadLocal.withInitial(
            () -> new ConcurrentHashMap<>(1));
    private static final ThreadLocal<Stack<SavePointHolder>> savePointStackThreadLocal = ThreadLocal
            .withInitial(Stack::new);

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
    public boolean init(TransactionDB current, IConfigData configData, @MayNull Object originObject,
            Class originClass,Method originMethod) throws Exception
    {
        this.transactionDB = current;
        if ("mybatis".equals(transactionDB.type()))
        {

        } else
        {
            throw new RuntimeException("unknown type:" + transactionDB.type());
        }
        this.source = transactionDB.dbSource();
        return configData.getBoolean("enableTransactionJDBC", true);
    }

    @Override
    public boolean preInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, boolean hasInvoked,
            Object lastReturn) throws Exception
    {
        IConnection iConnection = threadLocal.get().get(source);
        if (transactionDB.setSavePoint())
        {
            SavePointHolder savePointHolder = new SavePointHolder();
            savePointStackThreadLocal.get().push(savePointHolder);
        }
        if (iConnection == null || iConnection.willStartTransactionOk())
        {
            if ("mybatis".equals(transactionDB.type()))
            {
                LOGGER.debug("open source({}) for mybatis...", source);
                Object rs = __openConnection.invoke(null, source);
                LOGGER.debug("opened source({}) for mybatis:{}", source, rs);
            }

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("start transaction:source={},queryTimeoutSeconds={},readonly={},level={},method={}.{}",
                        source, transactionDB.queryTimeoutSeconds(), transactionDB.readonly(),
                        transactionDB.level(), originMethod.getDeclaringClass().getName(), originMethod.getName());
            }
            iConnection = threadLocal.get().get(source);
            if (transactionDB.queryTimeoutSeconds() != -1)
            {
                iConnection.setQueryTimeoutSeconds(transactionDB.queryTimeoutSeconds());
            }
            Connection connection = iConnection.getConnection();
            connection.setAutoCommit(false);
            iConnection.startTransactionOk();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("start transaction ok:source={},method={}", source, originMethod.getName());
            }
        }

        iConnection.setReadonly(transactionDB.readonly());
        iConnection.setLevel(transactionDB.level());
        iConnection.setQueryTimeoutSeconds(transactionDB.queryTimeoutSeconds());

        if (transactionDB.setSavePoint())
        {
            Connection connection = iConnection.getConnection();
            SavePointHolder savePointHolder = savePointStackThreadLocal.get().peek();
            Savepoint savepoint = connection.setSavepoint(originMethod.getName() + "-SavePoint");
            savePointHolder.savepoint = savepoint;
        }
        return false;
    }

    private void checkCommit(Method originMethod) throws Exception
    {
        IConnection iConnection = threadLocal.get().get(transactionDB.dbSource());

        if (iConnection != null && iConnection.willCommit())
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("commit... transaction:source={},method={}.{}", source,
                        originMethod.getDeclaringClass().getName(), originMethod.getName());
            }
            __removeConnection__(transactionDB.dbSource());
            iConnection.doCommit();
            LOGGER.debug("commit-ok transaction:source={},method={}", source, originMethod.getName());
        }
        if (transactionDB.setSavePoint())
        {
            savePointStackThreadLocal.get().pop();
        }
    }

    @Override
    public Object afterInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Object lastReturn) throws Exception
    {
        checkCommit(originMethod);
        return lastReturn;
    }


    @Override
    public void onException(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
            AspectOperationOfNormal.Invoker invoker, Object[] args, Throwable throwable) throws Throwable
    {

        IConnection iConnection = threadLocal.get().get(transactionDB.dbSource());
        if (iConnection != null)
        {
            boolean needRollback = true;
            if (transactionDB.setSavePoint())
            {
                SavePointHolder savePointHolder = savePointStackThreadLocal.get().pop();
                if (savePointHolder.savepoint != null)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("rollback savePoint... transaction:source={},method={}.{},errmsg={}", source,
                                originMethod.getDeclaringClass().getName(), originMethod.getName(),
                                OftenTool.getCause(throwable).toString());
                    }
                    needRollback = iConnection.doRollback(savePointHolder.savepoint);
                    LOGGER.debug("rollback savePoint ok!");
                }
            }

            if (needRollback)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("rollback... transaction:source={},method={}.{},errmsg={}", source,
                            originMethod.getDeclaringClass().getName(), originMethod.getName(),
                            OftenTool.getCause(throwable).toString());
                }

                __removeConnection__(transactionDB.dbSource());
                iConnection.doRollback();
                LOGGER.debug("rollback-finished transaction:source={},method={}", source, originMethod.getName());
            }
        }
    }
}
