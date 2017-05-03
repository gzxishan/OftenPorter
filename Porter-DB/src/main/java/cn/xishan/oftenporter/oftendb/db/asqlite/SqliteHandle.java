package cn.xishan.oftenporter.oftendb.db.asqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlHandle;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlQuerySettings;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;


import java.io.IOException;
import java.util.Arrays;

/**
 * 安卓端的sqlite数据库操作。
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class SqliteHandle implements DBHandle
{

    private String tableName;
    private SQLiteDatabase db;
    private static final TypeUtil.Type<ContentValues>[] TYPES_ADD;
    private static final TypeUtil.Type<TypeUtil.StatementObj>[] TYPES_MULTI_ADD;
    private boolean isTransaction = false;
    private boolean canOpenOrClose = true;

    static
    {
        TYPES_ADD = TypeUtil.getTypesForAdd();
        TYPES_MULTI_ADD = TypeUtil.getTypesForMultiAdd();
    }

    public SqliteHandle(SQLiteDatabase db, String tableName)
    {
        this.db = db;
        this.tableName = tableName;
    }

    @Override
    public void setLogger(Logger logger)
    {

    }

    void close(SQLiteStatement sqLiteStatement)
    {
        if (sqLiteStatement != null)
        {
            try
            {
                sqLiteStatement.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private Condition checkCondition(Condition condition)
    {
        return SqlHandle.checkCondition(condition);
    }

    static void close(Cursor cursor)
    {
        if (cursor != null)
        {
            try
            {
                cursor.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getTableName()
    {
        return tableName;
    }

    private void bind(int index, Object value, SQLiteStatement sQLiteStatement)
    {
        if (value == null)
        {
            sQLiteStatement.bindNull(index);
        } else
        {
            int i = Arrays.binarySearch(TYPES_ADD, TypeUtil.Type.forSearch(value.getClass()));
            if (i >= 0)
            {
                TypeUtil.Type<TypeUtil.StatementObj> type = TYPES_MULTI_ADD[i];
                type.put(null, value, new TypeUtil.StatementObj(index, sQLiteStatement));
            } else
            {
                throw new DBException("unknown type of " + value.getClass() + " for sqlite");
            }

        }
    }

    private void put(String name, Object value, ContentValues contentValues)
    {
        if (value == null)
        {
            contentValues.putNull(name);
        } else
        {
            int index = Arrays.binarySearch(TYPES_ADD, TypeUtil.Type.forSearch(value.getClass()));
            if (index >= 0)
            {
                TypeUtil.Type<ContentValues> type = TYPES_ADD[index];
                type.put(name, value, contentValues);
            } else
            {
                throw new DBException("unknown type of " + value.getClass() + " for sqlite");
            }

        }
    }

    private ContentValues parse(NameValues nameValues)
    {
        ContentValues contentValues = new ContentValues(nameValues.size());
        for (int i = 0; i < nameValues.size(); i++)
        {
            put("`" + nameValues.name(i) + "`", nameValues.value(i), contentValues);
        }
        return contentValues;
    }

    @Override
    public boolean add(NameValues nameValues) throws DBException
    {
        try
        {
            long id = db.insertOrThrow(tableName, null, parse(nameValues));
            return id != -1;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }

    }

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException
    {
        String sql = SqlUtil.toInsertOrReplace(true, tableName, multiNameValues.getNames(), false);
        SQLiteStatement ss = null;
        try
        {
            ss = db.compileStatement(sql);

            int[] is = null;
            if (!isTransaction())
            {
                db.beginTransaction();
            } else
            {
                is = new int[multiNameValues.count()];
            }


            for (int i = 0; i < multiNameValues.count(); i++)
            {
                Object[] values = multiNameValues.values(i);
                for (int j = 0; j < values.length; j++)
                {
                    bind(j + 1, values[j], ss);
                }
                is[i] = ss.executeInsert() == -1 ? 0 : 1;
            }

            if (!isTransaction())
            {
                db.setTransactionSuccessful();
                db.endTransaction();
                return new int[0];
            } else
            {
                return is;
            }

        } catch (DBException e)
        {
            throw e;
        } catch (SQLException e)
        {
            throw new DBException(e);
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(ss);
        }
    }

    @Override
    public boolean replace(Condition query, NameValues nameValues) throws DBException
    {
        try
        {
            long id = db.replaceOrThrow(tableName, null, parse(nameValues));
            return id != -1;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public int del(Condition query) throws DBException
    {
        SQLiteStatement statement = null;
        try
        {
            SqlUtil.WhereSQL whereSQL = SqlUtil.toDelete(tableName, checkCondition(query), true);

            statement = db.compileStatement(whereSQL.sql);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++)
            {
                bind(i + 1, args[i], statement);
            }

            return statement.executeUpdateDelete();
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }

    }

    private SqliteAdvancedQuery getSqliteAdvancedQuery(AdvancedQuery advancedQuery) throws DBException
    {
        if (!(advancedQuery instanceof SqliteAdvancedQuery))
        {
            throw new DBException("the object must be " + SqliteAdvancedQuery.class);
        }
        SqliteAdvancedQuery sqliteAdvancedQuery = (SqliteAdvancedQuery) advancedQuery;
        return sqliteAdvancedQuery;
    }


    @Override
    public JSONArray advancedQuery(AdvancedQuery advancedQuery) throws DBException
    {
        SqliteAdvancedQuery sqliteAdvancedQuery = getSqliteAdvancedQuery(advancedQuery);
        return _getJSONS(sqliteAdvancedQuery.whereSQL, sqliteAdvancedQuery.keys);
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(AdvancedQuery advancedQuery) throws DBException
    {
        SqliteAdvancedQuery sqliteAdvancedQuery = getSqliteAdvancedQuery(advancedQuery);

        return getDBEnumerations(sqliteAdvancedQuery.whereSQL, sqliteAdvancedQuery.keys);
    }

    private Cursor rawQuery(SqlUtil.WhereSQL whereSQL)
    {
        String[] args = null;
        if (whereSQL.args != null)
        {
            Object[] argsV = whereSQL.args;
            args = new String[argsV.length];

            for (int i = 0; i < args.length; i++)
            {
                if (argsV[i] != null)
                {
                    args[i] = String.valueOf(argsV[i]);
                }
            }
        }

        Cursor cursor = db.rawQuery(whereSQL.sql, args);
        return cursor;
    }

    private JSONArray _getJSONS(SqlUtil.WhereSQL whereSQL, String[] keys)
    {
        JSONArray list = new JSONArray();
        Cursor cursor = null;
        try
        {
            cursor = rawQuery(whereSQL);
            while (cursor.moveToNext())
            {
                list.add(getJSONObject(cursor, keys));
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
        return list;
    }

    private JSONObject getJSONObject(Cursor cursor, String[] keys) throws Exception
    {
        JSONObject jsonObject = new JSONObject();
        if (keys == null || keys.length == 0)
        {
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++)
            {
                jsonObject.put(cursor.getColumnName(i), TypeUtil.getObject(cursor, i));
            }
        } else
        {
            for (String string : keys)
            {
                int index = cursor.getColumnIndexOrThrow(string);
                jsonObject.put(string, TypeUtil.getObject(cursor, index));
            }
        }

        return jsonObject;
    }

    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException
    {
        if (!(advancedExecutor instanceof SqliteAdvancedExecutor))
        {
            throw new DBException("the object must be " + SqliteAdvancedExecutor.class);
        }
        SqliteAdvancedExecutor sqliteAdvancedExecutor = (SqliteAdvancedExecutor) advancedExecutor;
        return sqliteAdvancedExecutor.execute(db, this);
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException
    {
        JSONArray list = getJSONs(query, SqlQuerySettings.FIND_ONE, keys);
        Object obj = list.size() > 0 ? list.get(0) : null;
        return (JSONObject) obj;
    }

    @Override
    public JSONArray getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException
    {
        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlHandle.checkQuerySettings(querySettings),
                        false, keys);
        return _getJSONS(whereSQL, keys);
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(Condition query, QuerySettings querySettings,
            String... keys) throws DBException
    {
        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlHandle.checkQuerySettings(querySettings),
                        false, keys);
        return getDBEnumerations(whereSQL, keys);
    }

    private DBEnumeration<JSONObject> getDBEnumerations(SqlUtil.WhereSQL whereSQL,
            String[] keys) throws DBException
    {
        try
        {
            Cursor cursor = rawQuery(whereSQL);
            DBEnumeration<JSONObject> enumeration = new DBEnumeration<JSONObject>()
            {
                int hasNext = -1;

                @Override
                public boolean hasMoreElements() throws DBException
                {
                    if (hasNext == -1)
                    {
                        try
                        {
                            hasNext = cursor.moveToNext() ? 1 : 0;
                        } catch (Exception e)
                        {
                            throw new DBException(e);
                        }
                    }
                    return hasNext == 1;
                }

                @Override
                public JSONObject nextElement() throws DBException
                {
                    if (!hasMoreElements())
                    {
                        throw new DBException("no more elements!");
                    }
                    try
                    {
                        JSONObject jsonObject = getJSONObject(cursor, keys);
                        hasNext = -1;
                        return jsonObject;
                    } catch (Exception e)
                    {
                        throw new DBException(e);
                    }
                }

                @Override
                public void close() throws DBException
                {
                    SqliteHandle.close(cursor);
                    canOpenOrClose = true;
                }
            };
            canOpenOrClose = false;
            return enumeration;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public boolean canOpenOrClose()
    {
        return canOpenOrClose;
    }

    @Override
    public JSONArray get(Condition query, QuerySettings querySettings, String key) throws DBException
    {

        JSONArray list = new JSONArray();

        SqlUtil.WhereSQL whereSQL = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlHandle.checkQuerySettings(querySettings),
                        false, key);

        Cursor cursor = null;
        try
        {
            cursor = rawQuery(whereSQL);
            while (cursor.moveToNext())
            {
                list.add(TypeUtil.getObject(cursor, 0));
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }

        return list;
    }


    @Override
    public int update(Condition query, NameValues nameValues) throws DBException
    {
        if (nameValues == null || nameValues.size() == 0)
        {
            return 0;
        }
        int n = 0;
        SQLiteStatement statement = null;
        try
        {
            SqlUtil.WhereSQL whereSql = SqlUtil
                    .toSetValues(tableName, nameValues.names(), checkCondition(query), false);
            statement = db.compileStatement(whereSql.sql);
            for (int i = 0; i < nameValues.size(); i++)
            {
                bind(i + 1, nameValues.value(i), statement);
            }
            Object[] args = whereSql.args;
            for (int i = 0, k = nameValues.size() + 1; i < args.length; i++, k++)
            {
                bind(k, args[i], statement);
            }
            n = statement.executeUpdateDelete();

        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }

        return n;
    }

    @Override
    public long exists(Condition query) throws DBException
    {
        SqlUtil.WhereSQL whereSQL = SqlUtil.toCountSelect(tableName, "", checkCondition(query), true);
        return exists(whereSQL);
    }

    @Override
    public long exists(AdvancedQuery advancedQuery) throws DBException
    {
        SqliteAdvancedQuery sqliteAdvancedQuery = getSqliteAdvancedQuery(advancedQuery);
        return exists(sqliteAdvancedQuery.whereSQL);
    }

    public long exists(SqlUtil.WhereSQL whereSQL) throws DBException
    {
        Cursor cursor = null;
        try
        {
            cursor = rawQuery(whereSQL);
            long n = 0;
            if (cursor.moveToNext())
            {
                n = cursor.getLong(0);
            }
            return n;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
    }

    @Override
    public boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException
    {
        SQLiteStatement statement = null;
        try
        {
            SqlUtil.WhereSQL whereSQL = SqlUtil.toUpdate(tableName, checkCondition(query), name, true);
            statement = db.compileStatement(whereSQL.sql);
            byte[] bs = new byte[length];
            System.arraycopy(data, offset, bs, 0, length);
            statement.bindBlob(1, bs);
            Object[] args = whereSQL.args;
            for (int i = 0; i < args.length; i++)
            {
                bind(i + 2, args[i], statement);
            }
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }
    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException
    {
        SqlUtil.WhereSQL wsql = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlQuerySettings.FIND_ONE, true, name);
        Cursor cursor = null;
        try
        {
            cursor = rawQuery(wsql);
            byte[] bs = null;
            if (cursor.moveToNext())
            {
                bs = cursor.getBlob(0);
            }
            return bs;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            db.close();
        } catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public boolean supportTransaction() throws DBException
    {
        return true;
    }

    @Override
    public boolean isTransaction()
    {
        return isTransaction;
    }

    @Override
    public void startTransaction() throws DBException
    {
        db.beginTransaction();
        isTransaction = true;
    }

    @Override
    public void commitTransaction() throws DBException
    {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void rollback() throws DBException
    {
        db.endTransaction();
    }

    private Object tempObject;

    @Override
    public Object tempObject(Object tempObject)
    {
        Object obj = this.tempObject;
        this.tempObject = tempObject;
        return obj;
    }
}
