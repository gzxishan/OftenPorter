package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.*;
import java.sql.*;

public class SqlHandle implements DBHandle, SqlSource {

    private static final QuerySettings FIND_ONE = new QuerySettings().setLimit(1).setSkip(0);

    private Connection conn;
    private String tableName;// 表名

    private boolean isTransaction;
    private Boolean field2LowerCase = null;
    private static final Logger _LOGGER = LogUtil.logger(SqlHandle.class);
    private Logger LOGGER = _LOGGER;
    private boolean canOpenOrClose = true;

    /**
     * 创建一个DbRwMysql
     *
     * @param conn 数据库连接对象
     */
    public SqlHandle(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void setLogger(Logger Logger) {
        LOGGER = Logger;
    }

    /**
     * 是否数据库的返回结果转换为小写：true转为小写，false转为大写，null什么都不做。
     *
     * @param field2LowerCase
     */
    public SqlHandle setField2LowerCase(Boolean field2LowerCase) {
        this.field2LowerCase = field2LowerCase;
        return this;
    }

    @Override
    public void setCollectionName(String collectionName) {
        this.tableName = collectionName;
    }

    public String getTableName() {
        return tableName;
    }

    private static final SqlCondition TRUE = null;// new SqlCondition();

    public static SqlCondition checkCondition(Condition condition) {
        if (condition == null) {
            return TRUE;
        }
        if (condition instanceof SqlCondition) {
            return (SqlCondition) condition;
        } else {
            throw new DBException("the condition type of " + SqlCondition.class
                    + " is accept."
                    + "Current is "
                    + condition.getClass());
        }
    }


    @Override
    public boolean add(NameValues addFields) throws DBException {
        return executePS(conn, true, tableName, addFields) == 1;
    }

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException {
        return addPS(conn, isTransaction, tableName, multiNameValues);
    }

    public static void logArgs(Object[] args, StringBuilder builder) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            logArg(value, builder);
        }
    }

    private static void logArg(Object value, StringBuilder builder) {
        value = value != null && (value instanceof InputStream) ? value.getClass() : value;
        String str = String.valueOf(value);
        if (str.length() > 256) {
            str = str.substring(0, 256) + "...";
            value = str;
        }
        builder.append(value).append("(").append(value == null ? null : value.getClass().getSimpleName()).append("),");
    }

    private static void logArgs(NameValues nameValues, StringBuilder builder) {
        for (int i = 0; i < nameValues.size(); i++) {
            Object value = nameValues.value(i);
            logArg(value, builder);
        }
    }

    /**
     * @param query 不会被使用
     */
    @Override
    public boolean replace(Condition query, NameValues updateFields) throws DBException {
        return executePS(conn, false, tableName, updateFields) > 0;
    }

    private int execute(SqlUtil.WhereSQL whereSQL) {
        PreparedStatement ps = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", whereSQL.sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSQL.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(whereSQL.sql);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                setObject(ps, i + 1, obj);
            }
            int n = ps.executeUpdate();
            return n;
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }
    }

    @Override
    public int del(Condition query) throws DBException {
        SqlUtil.WhereSQL whereSQL = SqlUtil.toDelete(tableName, checkCondition(query), true);
        return execute(whereSQL);
    }


    @Override
    public JSONArray getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException {
        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), querySettings, true, keys);
        return _getJSONS(whereSQL, null, keys);
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(Condition query, QuerySettings querySettings,
                                                       String... keys) throws DBException {
        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), querySettings, true, keys);
        return getDBEnumerations(whereSQL, null, keys);
    }

    private DBEnumeration<JSONObject> getDBEnumerations(SqlUtil.WhereSQL whereSQL, QuerySettings querySettings,
                                                        String[] keys) throws DBException {

        try {
            String sql = querySettings == null ? whereSQL.sql : whereSQL.sql + SqlUtil.toOrder(querySettings, true);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSQL.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            PreparedStatement ps = conn.prepareStatement(sql);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                setObject(ps, i + 1, obj);
            }
            ResultSet rs = ps.executeQuery();

            DBEnumeration<JSONObject> dbEnumeration = new DBEnumeration<JSONObject>() {
                int hasNext = -1;

                @Override
                public void close() throws DBException {
                    WPTool.close(ps);
                    canOpenOrClose = true;
                }

                @Override
                public boolean hasMoreElements() throws DBException {
                    if (hasNext == -1) {
                        try {
                            hasNext = rs.next() ? 1 : 0;
                        } catch (SQLException e) {
                            throw new DBException(e);
                        }
                    }
                    return hasNext == 1;
                }

                @Override
                public JSONObject nextElement() {
                    if (!hasMoreElements()) {
                        throw new DBException("no more elements!");
                    }
                    try {
                        JSONObject jsonObject = getJSONObject(rs, keys);
                        hasNext = -1;
                        return jsonObject;
                    } catch (SQLException e) {
                        throw new DBException(e);
                    }
                }
            };
            canOpenOrClose = false;
            return dbEnumeration;
        } catch (Exception e) {
            throw new DBException(e);
        }


    }

    @Override
    public boolean canOpenOrClose() {
        return canOpenOrClose;
    }

    private JSONArray _getJSONS(SqlUtil.WhereSQL whereSQL, QuerySettings querySettings, String[] keys) {
        JSONArray list = new JSONArray();
        PreparedStatement ps = null;

        try {
            String sql = querySettings == null ? whereSQL.sql : whereSQL.sql + SqlUtil.toOrder(querySettings, true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSQL.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(sql);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                setObject(ps, i + 1, obj);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(getJSONObject(rs, keys));
            }
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }
        return list;
    }

    private SqlAdvancedQuery getSqlAdvancedQuery(AdvancedQuery advancedQuery) {
        if (!(advancedQuery instanceof SqlAdvancedQuery)) {
            throw new DBException("the object must be " + SqlAdvancedQuery.class);
        }
        SqlAdvancedQuery advanced = (SqlAdvancedQuery) advancedQuery;
        return advanced;
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(AdvancedQuery advancedQuery,
                                                       QuerySettings querySettings) throws DBException {
        SqlAdvancedQuery advanced = getSqlAdvancedQuery(advancedQuery);

        SqlUtil.WhereSQL whereSQL = advanced.whereSQL;
        String[] keys = advanced.keys;
        return getDBEnumerations(whereSQL, querySettings, keys);
    }

    private JSONObject getJSONObject(ResultSet rs, String[] keys) throws JSONException, SQLException {
        JSONObject jsonObject;
        if (keys == null || keys.length == 0) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            jsonObject = new JSONObject(columnCount);
            for (int i = 1; i <= columnCount; i++) {
//                jsonObject.put(field2LowerCase == null ? metaData.getColumnName(i) : (field2LowerCase ? metaData
//                        .getColumnName(i).toLowerCase() : metaData.getColumnName(i).toUpperCase()), rs.getObject(i));
                String label = metaData.getColumnLabel(i);
                jsonObject.put(field2LowerCase == null ? label : (field2LowerCase ? label.toLowerCase() : label
                        .toUpperCase()), rs.getObject(i));
            }
        } else {
            jsonObject = new JSONObject(keys.length);
            for (String string : keys) {
                jsonObject.put(string, rs.getObject(string));
            }
        }

        return jsonObject;
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException {
        JSONArray list = getJSONs(query, FIND_ONE, keys);
        Object obj = list.size() > 0 ? list.get(0) : null;
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }


    @Override
    public JSONArray get(Condition query, QuerySettings querySettings, String key) throws DBException {

        JSONArray list = new JSONArray();

        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), querySettings, true, key);

        PreparedStatement ps = null;

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", whereSQL.sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSQL.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(whereSQL.sql);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++) {
                setObject(ps, i + 1, args[i]);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getObject(key));
            }
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

        return list;
    }

    @Override
    public int update(Condition query, NameValues updateFields) throws DBException {
        if (updateFields == null || updateFields.size() == 0) {
            return 0;
        }

        return executeSet(conn, tableName, query, updateFields);
    }

    @Override
    public long exists(Condition query) throws DBException {
        return exists(conn, checkCondition(query), tableName);
    }

    @Override
    public boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException {
        PreparedStatement ps = null;
        try {
            SqlUtil.WhereSQL whereSQL = SqlUtil.toUpdate(tableName, checkCondition(query), name, true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", whereSQL.sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSQL.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(whereSQL.sql);
            ps.setBinaryStream(1, new ByteArrayInputStream(data, offset, length), length);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++) {
                setObject(ps, i + 2, args[i]);
            }
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException {
        SqlUtil.WhereSQL ws = SqlUtil.toSelect(tableName, checkCondition(query), FIND_ONE, true, name);
        PreparedStatement ps = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", ws.sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = ws.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(ws.sql);
            for (int i = 0; i < ws.args.length; i++) {
                setObject(ps, i + 1, ws.args[i]);
            }

            ResultSet rs = ps.executeQuery();
            byte[] bs = null;
            if (rs.next()) {
                bs = FileTool.getData(rs.getBinaryStream(1), 1024);
            }
            return bs;
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            conn.close();
            LOGGER.debug("conn closed:{}", tableName);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public JSONArray advancedQuery(AdvancedQuery advancedQuery, QuerySettings querySettings) throws DBException {
        SqlAdvancedQuery advanced = getSqlAdvancedQuery(advancedQuery);
        return _getJSONS(advanced.whereSQL, querySettings, advanced.keys);
    }

    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException {
        if (!(advancedExecutor instanceof SqlAdvancedExecutor)) {
            throw new DBException("the object must be " + SqlAdvancedExecutor.class);
        }
        SqlAdvancedExecutor sqlAdvancedNeed = (SqlAdvancedExecutor) advancedExecutor;
        return sqlAdvancedNeed.execute(conn, this);
    }

    @Override
    public boolean supportTransaction() throws DBException {
        return true;
    }

    @Override
    public void startTransaction(TransactionConfig transactionConfig) throws DBException {
        try {
            conn.setAutoCommit(false);
            SqlTransactionConfig sqlTransactionConfig = (SqlTransactionConfig) transactionConfig;
            if (sqlTransactionConfig != null && sqlTransactionConfig.transactionLevel != null) {
                conn.setTransactionIsolation(sqlTransactionConfig.transactionLevel);
            }
            isTransaction = true;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public void commitTransaction() throws DBException {
        try {
            conn.commit();
            isTransaction = false;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public boolean isTransaction() {
        return isTransaction;
    }

    @Override
    public void rollback() throws DBException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private Object tempObject;

    @Override
    public Object tempObject(Object tempObject) {
        Object obj = this.tempObject;
        this.tempObject = tempObject;
        return obj;
    }

    //////////////////////////////////////

    private int executeSet(Connection conn, String tableName, Condition query,
                           NameValues updateFields) throws DBException {
        int n = 0;
        PreparedStatement ps = null;
        try {
            SqlUtil.WhereSQL whereSQL = SqlUtil
                    .toSetValues(tableName, updateFields.names(), checkCondition(query), true);
            ps = conn.prepareStatement(whereSQL.sql);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", whereSQL.sql);
                StringBuilder builder = new StringBuilder();
                logArgs(updateFields, builder);
                logArgs(whereSQL.args, builder);
                LOGGER.debug("{}", builder);
            }

            for (int i = 0; i < updateFields.size(); i++) {
                setObject(ps, i + 1, updateFields.value(i));
            }

            Object[] args = whereSQL.args;
            for (int i = 0, k = updateFields.size() + 1; i < args.length; i++, k++) {
                setObject(ps, k, args[i]);
            }

            n = ps.executeUpdate();

        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

        return n;
    }

    private int executePS(Connection conn, boolean isInsert, String tableName, NameValues addFields) {
        int n = 0;
        PreparedStatement ps = null;
        try {
            String sql = SqlUtil.toInsertOrReplace(isInsert, tableName, addFields.names(), true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", sql);
                StringBuilder builder = new StringBuilder();
                logArgs(addFields, builder);
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < addFields.size(); i++) {
                setObject(ps, i + 1, addFields.value(i));
            }
            n = ps.executeUpdate();

        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

        return n;
    }

    public static void setObject(PreparedStatement ps, int column,
                                 Object object) throws SQLException, FileNotFoundException {
        if (object == null) {
            ps.setObject(column, object);
        } else {
            if (object instanceof File) {
                File file = (File) object;
                ps.setBlob(column, new FileInputStream(file), file.length());
            } else if (object instanceof CharSequence || object instanceof JSONArray || object instanceof JSONObject) {
                ps.setString(column, String.valueOf(object));
            } else {
                ps.setObject(column, object);
            }
        }
    }

    private int[] addPS(Connection conn, boolean isTransaction, String tableName,
                        MultiNameValues multiNameValues) {
        String[] names = multiNameValues.getNames();
        PreparedStatement ps = null;
        try {

            String sql = SqlUtil.toInsertOrReplace(true, tableName, names, true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", sql);
                StringBuilder builder = new StringBuilder("[");

                int n = multiNameValues.count();
                for (int j = 0; j < n; j++) {
                    Object[] values = multiNameValues.values(j);
                    logArgs(values, builder);
                    builder.append("\n");
                }
                builder.append("]");
                LOGGER.debug("{}", builder);
            }
            ps = conn.prepareStatement(sql);
            if (!isTransaction) {
                conn.setAutoCommit(false);
            }

            int n = multiNameValues.count();
            for (int j = 0; j < n; j++) {
                Object[] values = multiNameValues.values(j);
                for (int k = 0; k < values.length; k++) {
                    setObject(ps, k + 1, values[k]);
                }
                ps.addBatch();
            }
            int[] rs = ps.executeBatch();
            if (!isTransaction) {
                conn.commit();
            }

            return rs;
        } catch (BatchUpdateException e) {

            throw new DBException(e);

        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

    }

    @Override
    public long exists(AdvancedQuery advancedQuery) throws DBException {
        SqlAdvancedQuery advanced = getSqlAdvancedQuery(advancedQuery);
        SqlUtil.WhereSQL whereSQL = advanced.whereSQL;
        return _countSql(conn, SqlUtil.toCountSelect(whereSQL, "", true));
    }

    /**
     * count某个条件
     */

    public long exists(Connection conn, Condition condition, String tableName) throws DBException {
        SqlUtil.WhereSQL whereSql = SqlUtil
                .toCountSelect(tableName, "", checkCondition(condition), true);
        return _countSql(conn, whereSql);
    }

    public long _countSql(Connection conn, SqlUtil.WhereSQL whereSql) throws DBException {

        long n = 0;

        PreparedStatement ps = null;
        try {


            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}", whereSql.sql);
                StringBuilder builder = new StringBuilder();
                Object[] args = whereSql.args;
                logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }

            ps = conn.prepareStatement(whereSql.sql);

            Object[] args = whereSql.args;
            for (int i = 0; i < args.length; i++) {
                setObject(ps, i + 1, args[i]);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                n = rs.getLong(1);
            }
            ps.close();
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            WPTool.close(ps);
        }

        return n;
    }

}
