package cn.xishan.oftenporter.oftendb.mybatis;

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

/**
 * Created by chenyg on 2018-03-07.
 */
class ConnectionWrap2 extends ConnectionWrap implements InvocationHandler
{
    _MyBatis myBatis;

    public ConnectionWrap2(SqlSession sqlSession, _MyBatis myBatis)
    {
        super(sqlSession);
        this.myBatis = myBatis;
    }

    @Override
    public void close() throws SQLException
    {
        connection.close();
    }

    static SqlSession wrapForParams(SqlSession sqlSession, _MyBatis myBatis)
    {
        if (!(sqlSession instanceof ConnectionWrap2))
        {
            sqlSession = (SqlSession) Proxy
                    .newProxyInstance(sqlSession.getClass().getClassLoader(), new Class[]{sqlSession.getClass()}, new ConnectionWrap2(sqlSession, myBatis));
        }
        return sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.getName().equals("getConnection"))
        {
            return this;
        } else
        {
            return method.invoke(sqlSession, args);
        }
    }
}
