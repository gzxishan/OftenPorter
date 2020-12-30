package cn.xishan.oftenporter.oftendb.db;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-16.
 */
public class ConnectionWrapper implements Connection
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionWrapper.class);

    private static final AtomicLong count = new AtomicLong();
    private final String ID;

    static final ThreadLocal<Map<String, List<Consumer>>> ON_CONNECTION_CLOSED_LOCAL = new ThreadLocal<>();
    static final ThreadLocal<WeakReference<ConnectionWrapper>> CONN_FINAL_WRAP_THREAD_LOCAL = new ThreadLocal<>();

    protected Connection connection;

    public ConnectionWrapper(Connection connection)
    {
        this.ID = String.valueOf(count.getAndIncrement());
        this.connection = connection;
        if (!(connection instanceof ConnectionWrapper))
        {
            CONN_FINAL_WRAP_THREAD_LOCAL.set(new WeakReference<>(this));
        }
    }

    public static void onCurrentTSConnectionClosed(Consumer<Void> consumer)
    {
        WeakReference<ConnectionWrapper> ref = CONN_FINAL_WRAP_THREAD_LOCAL.get();
        ConnectionWrapper wrapper;
        if (ref != null && (wrapper = ref.get()) != null)
        {
            Map<String, List<Consumer>> map = ON_CONNECTION_CLOSED_LOCAL.get();
            if (map == null)
            {
                map = new HashMap<>();
                ON_CONNECTION_CLOSED_LOCAL.set(map);
            }

            if (!map.containsKey(wrapper.ID))
            {
                map.put(wrapper.ID, new ArrayList<>(1));
            }

            map.get(wrapper.ID).add(consumer);
        } else
        {
            consumer.accept(null);
        }
    }

    public Connection getOriginConnection()
    {
        if (connection instanceof ConnectionWrapper)
        {
            return ((ConnectionWrapper) connection).getOriginConnection();
        } else
        {
            return connection;
        }
    }

    @Override
    public Statement createStatement() throws SQLException
    {
        return connection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return connection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return connection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        return connection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        connection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        return connection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException
    {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException
    {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException
    {
        try
        {
            connection.close();

        } finally
        {
            WeakReference<ConnectionWrapper> ref = CONN_FINAL_WRAP_THREAD_LOCAL.get();
            if (ref != null && ref.get() == this)
            {
                CONN_FINAL_WRAP_THREAD_LOCAL.remove();
            }

            Map<String, List<Consumer>> map = ON_CONNECTION_CLOSED_LOCAL.get();
            if (map != null)
            {
                if (map.containsKey(ID))
                {
                    List<Consumer> list = map.remove(ID);
                    for (Consumer consumer : list)
                    {
                        try
                        {
                            consumer.accept(null);
                        } catch (Exception e)
                        {
                            LOGGER.warn(e.getMessage(), e);
                        }
                    }
                }

                if (map.isEmpty())
                {
                    CONN_FINAL_WRAP_THREAD_LOCAL.remove();
                }
            }
        }
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return connection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        connection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        return connection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        connection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException
    {
        return connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        connection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        connection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return connection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException
    {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        return connection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        connection.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        connection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException
    {
        return connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        return connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        return connection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        connection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        connection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException
    {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        return connection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return connection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return connection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException
    {
        return connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        return connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        return connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        return connection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        return connection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        connection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
        return connection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        return connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        return connection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        return connection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException
    {
        connection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException
    {
        return connection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
        connection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        connection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        return connection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return connection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return connection.isWrapperFor(iface);
    }
}
