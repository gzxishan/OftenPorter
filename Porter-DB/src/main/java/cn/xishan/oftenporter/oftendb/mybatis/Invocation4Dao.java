package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.sql.TransactionDBHandle;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;

import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2020/3/13.
 */
class Invocation4Dao extends InvocationHandlerWithCommon
{
    private MyBatisDaoImpl myBatisDao;
    private Class<?> type;
    private String source;
    private boolean wrapDaoThrowable;

    public Invocation4Dao(MyBatisDaoImpl myBatisDao, Class<?> type, String source, boolean wrapDaoThrowable)
    {
        super(myBatisDao);
        this.myBatisDao = myBatisDao;
        this.type = type;
        this.source = source;
        this.wrapDaoThrowable = wrapDaoThrowable;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.equals(TO_STRING_METHOD))
        {
            return type.getName() + "@@" + myBatisDao.getClass().getSimpleName() + myBatisDao.hashCode();
        }
        if (wrapDaoThrowable)
        {
            try
            {
                return super.invoke(proxy, method, args);
            } catch (Throwable e)
            {
                if (e instanceof OftenCallException)
                {
                    throw e;
                } else
                {
                    throw new OftenCallException(e);
                }
            }
        } else
        {
            return super.invoke(proxy, method, args);
        }
    }

    private void mayClose(ConnectionWrap connectionWrap) throws SQLException
    {
        if (connectionWrap == null)
        {
            return;
        }
        if (connectionWrap.getAutoCommit())
        {
            connectionWrap.close();
        } else if (connectionWrap.isBridgeConnection())
        {
            TransactionDBHandle.__removeConnection__(source);
        }
    }

    @Override
    public Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable
    {
        ConnectionWrap connectionWrap = MyBatisBridge.__openConnection(source, false);
        boolean isInvokeError = true;
        try
        {
            Object dao = myBatisDao.getMapperDao(connectionWrap.getSqlSession(), type);
            Object rs = method.invoke(dao, args);
            isInvokeError = false;
            mayClose(connectionWrap);
            return rs;

        } catch (Throwable throwable)
        {
            if (isInvokeError)
            {
                mayClose(connectionWrap);
            }
            throw throwable;
        }


    }
}
