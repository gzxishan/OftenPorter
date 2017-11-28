package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlTransactionConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
class DBHandleOnlyTS implements DBHandle
{
    private boolean isTransaction;
    private SqlSession sqlSession;

    public DBHandleOnlyTS(SqlSession sqlSession)
    {
        this.sqlSession = sqlSession;
    }

    @Override
    public boolean add(NameValues nameValues) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public void setLogger(Logger logger)
    {

    }

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public boolean replace(Condition query, NameValues nameValues) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public int del(Condition query) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public JSONArray advancedQuery(AdvancedQuery advancedQuery, QuerySettings querySettings) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(AdvancedQuery advancedQuery,
            QuerySettings querySettings) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public JSONArray getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(Condition query, QuerySettings querySettings,
            String... keys) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public boolean canOpenOrClose()
    {
        throw new RuntimeException("stub");
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public JSONArray get(Condition query, QuerySettings querySettings, String key) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public int update(Condition query, NameValues nameValues) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public long exists(Condition query) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public long exists(AdvancedQuery advancedQuery) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException
    {
        throw new RuntimeException("stub");
    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException
    {
        throw new RuntimeException("stub");
    }



    @Override
    public boolean supportTransaction() throws DBException
    {
        return true;
    }

    @Override
    public void startTransaction(TransactionConfig transactionConfig) throws DBException
    {
        try
        {
            sqlSession.getConnection().setAutoCommit(false);
            SqlTransactionConfig sqlTransactionConfig = (SqlTransactionConfig) transactionConfig;
            if (sqlTransactionConfig != null && sqlTransactionConfig.transactionLevel != null)
            {
                sqlSession.getConnection().setTransactionIsolation(sqlTransactionConfig.transactionLevel);
            }
            isTransaction = true;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public void commitTransaction() throws DBException
    {
        try
        {
            sqlSession.getConnection().commit();
            isTransaction = false;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public boolean isTransaction()
    {
        return isTransaction;
    }

    @Override
    public void rollback() throws DBException
    {
        try
        {
            sqlSession.getConnection().rollback();
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public void close() throws IOException
    {
        sqlSession.close();
    }

    private Object tempObject;

    @Override
    public Object tempObject(Object tempObject)
    {
        Object obj = this.tempObject;
        this.tempObject = tempObject;
        return obj;
    }

    @Override
    public void setCollectionName(String collectionName)
    {
        throw new RuntimeException("stub");
    }
}
