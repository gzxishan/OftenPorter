package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;

/**
 * Created by chenyg on 2017-04-29.
 */
public class _JsInterface
{
    public String tableNamePrefix;
    private static final Object[] ZERO_ARGS = new Object[0];


    _JsInterface(DBSource dbSource, String tableNamePrefix)
    {
        this.tableNamePrefix = tableNamePrefix;
    }


    public AdvancedExecutor sqlExecutor(String sql)
    {
        return _sqlExecutor(sql, ZERO_ARGS);
    }

    public AdvancedExecutor sqlExecutor(String sql, Object[] args)
    {
        return _sqlExecutor(sql, args);
    }

    static AdvancedExecutor _sqlExecutor(String sql, Object[] args)
    {
        return SqlAdvancedExecutor.withSqlAndArgs(sql, args);
    }

    public AdvancedQuery sqlQuery(String sql)
    {
        return _sqlQuery(sql, ZERO_ARGS);
    }

    public AdvancedQuery sqlQuery(String sql, Object[] args)
    {
        return _sqlQuery(sql, args);
    }

    static AdvancedQuery _sqlQuery(String sql, Object[] args)
    {
        //JSqlArgs jSqlArgs = _sqlArgs(sql, argsObj);

        int n = 0;
        int len = sql.length();
        for (int i = 0; i < len; i++)
        {
            if (sql.charAt(i) == '?')
            {
                n++;
            }
        }

        if (n != args.length)
        {
            Object[] objects = new Object[n];
            if (n > args.length)
            {
                System.arraycopy(args, 0, objects, 0, args.length);
            } else
            {
                System.arraycopy(args, 0, objects, 0, n);
            }
            args = objects;
        }

        AdvancedQuery advancedQuery = new SqlAdvancedQuery(new SqlUtil.WhereSQL(sql, args));
        return advancedQuery;
    }

//    public JSqlArgs sqlArgs(String sql, Object[] args)
//    {
//        return _sqlArgs(sql,argsObj);
//    }

//    static JSqlArgs _sqlArgs(String sql, Object[] args)
//    {
//        Object[] args = toArray(argsObj);
//        return new JSqlArgs(sql, args);
//    }

//    public static Object[] toArray(Object object)
//    {
//        if (object == null)
//        {
//            return null;
//        } else if (object instanceof ScriptObjectMirror)
//        {
//            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) object;
//            if (!scriptObjectMirror.isArray())
//            {
//                throw new IllegalArgumentException("ScriptObjectMirror is no array");
//            }
//
//            if (scriptObjectMirror.isEmpty())
//            {
//                return new Object[0];
//            }
//
//            Object[] array = new Object[scriptObjectMirror.size()];
//
//            int i = 0;
//
//            for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet())
//            {
//                Object result = entry.getValue();
//                if (result == null)
//                {
//                    array[i] = result;
//                } else if (result instanceof ScriptObjectMirror && scriptObjectMirror.isArray())
//                {
//                    array[i] = toArray(result);
//                } else if (result instanceof NativeArray)
//                {
//                    array[i] = ((NativeArray) result).asObjectArray();
//                } else
//                {
//                    array[i] = result;
//                }
//                i++;
//            }
//
//            return array;
//        } else if (object instanceof NativeArray)
//        {
//            Object[] array = ((NativeArray) object).asObjectArray();
//            return array;
//        } else
//        {
//            throw new IllegalArgumentException("it is not an array:" + object);
//        }
//    }

    public String filterLike(String content)
    {
        return SqlUtil.filterLike(content);
    }

}
