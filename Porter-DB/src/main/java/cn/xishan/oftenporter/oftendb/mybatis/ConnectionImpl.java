package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.sql.IConnection;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
class ConnectionImpl implements IConnection
{
    private SqlSession sqlSession;

    public ConnectionImpl(SqlSession sqlSession)
    {
        this.sqlSession = sqlSession;
    }

    public SqlSession getSqlSession()
    {
        return sqlSession;
    }

    @Override
    public Connection getConnection()
    {
        return sqlSession.getConnection();
    }
}
