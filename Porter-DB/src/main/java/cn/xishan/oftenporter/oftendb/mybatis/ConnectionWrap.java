package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.oftendb.annotation.tx.Readonly;
import cn.xishan.oftenporter.oftendb.db.ConnectionWrapper;
import cn.xishan.oftenporter.oftendb.db.sql.IConnection;
import org.apache.ibatis.session.SqlSession;

import java.sql.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/6.
 */
class ConnectionWrap extends ConnectionWrapper implements IConnection
{
    protected SqlSession sqlSession;
    private int queryTimeoutSeconds = -1;
    private int transactionCount = 0;
    private Readonly lastReadonly = Readonly.DEFAULT;
    private Isolation lastLevel = Isolation.DEFAULT;
    private MyBatisOption.IConnectionBridge iConnectionBridge;
    private String builderId;
    private MSqlSessionFactoryBuilder builder;
    private int refCount = 1;
    private Thread thread;

    public ConnectionWrap(MSqlSessionFactoryBuilder builder, SqlSession sqlSession,
            MyBatisOption.IConnectionBridge iConnectionBridge,
            Connection bridgeConnection)
    {
        super(iConnectionBridge != null ? bridgeConnection : sqlSession.getConnection());
        this.builderId = builder.getId();
        this.builder = builder;
        this.sqlSession = sqlSession;
        this.iConnectionBridge = iConnectionBridge;
        this.thread = Thread.currentThread();
    }

    public Thread getThread()
    {
        return thread;
    }

    public boolean isNotSameThread()
    {
        if (thread != Thread.currentThread())
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 降低引用次数
     *
     * @return true：无引用
     */
    public boolean decRefCount()
    {
        return --this.refCount <= 0;
    }

    /**
     * 增加引用次数
     */
    public ConnectionWrap incRefCount()
    {
        this.refCount++;
        return this;
    }

    public boolean isBridgeConnection()
    {
        return iConnectionBridge != null;
    }

    @Override
    public void setLevel(Isolation level) throws SQLException
    {
        if (level.getLevel() > lastLevel.getLevel())
        {
            connection.setTransactionIsolation(level.getLevel());
            lastLevel = level;
        }
    }

    @Override
    public void setReadonly(Readonly readonly) throws SQLException
    {
        if (lastReadonly == Readonly.FALSE)
        {
            return;
        }
        lastReadonly = readonly;
        connection.setReadOnly(lastReadonly == Readonly.TRUE);
    }

    @Override
    public boolean willStartTransactionOk()
    {
        if (transactionCount == 0)
        {
            return true;
        } else
        {
            transactionCount++;
            return false;
        }
    }

    @Override
    public void startTransactionOk()
    {
        transactionCount = 1;
    }

    @Override
    public boolean willCommit()
    {
        if (transactionCount == 1)
        {
            return true;
        } else if (transactionCount > 1)
        {
            transactionCount--;
        }
        return false;
    }


    @Override
    public void doCommit() throws SQLException
    {
        if (transactionCount != 1)
        {
            throw new SQLException("illegal transactionCount:" + transactionCount);
        }
        this.commit();
        this.close();
    }

    @Override
    public boolean doRollback(Savepoint savepoint) throws SQLException
    {
        if (transactionCount < 1)
        {
            throw new SQLException("illegal transactionCount:" + transactionCount);
        }
        this.rollback(savepoint);
        return --transactionCount == 0;
    }

    @Override
    public void doRollback() throws SQLException
    {
        this.rollback();
        this.close();
    }

    @Override
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds)
    {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }


    public SqlSession getSqlSession()
    {
        if (!this.builderId.equals(builder.getId()))
        {
            sqlSession = builder.getFactory().openSession(connection);
            this.builderId = builder.getId();
        }
        return sqlSession;
    }

    @Override
    public Connection getConnection()
    {
        return this;
    }

    private final <T extends Statement> T settings(T statement) throws SQLException
    {
        if (queryTimeoutSeconds != -1)
        {
            try
            {
                statement.setQueryTimeout(queryTimeoutSeconds);
            } catch (SQLException e)
            {
                statement.close();
                throw e;
            }
        }
        return statement;
    }

    @Override
    public Statement createStatement() throws SQLException
    {
        return settings(super.createStatement());
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return settings(super.prepareStatement(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return settings(super.prepareCall(sql));
    }

    @Override
    public void close() throws SQLException
    {
        this.thread = null;
        if (isBridgeConnection())
        {
            Connection connection = getOriginConnection();
            this.iConnectionBridge
                    .closeConnection(sqlSession.getConfiguration().getEnvironment().getDataSource(), connection);
        }
        sqlSession.close();
    }


    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return settings(super.createStatement(resultSetType, resultSetConcurrency));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        return settings(super.prepareStatement(sql, resultSetType, resultSetConcurrency));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return settings(super.prepareCall(sql, resultSetType, resultSetConcurrency));
    }


    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return settings(super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return settings(super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return settings(super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        return settings(super.prepareStatement(sql, autoGeneratedKeys));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return settings(super.prepareStatement(sql, columnIndexes));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return settings(super.prepareStatement(sql, columnNames));
    }


}
