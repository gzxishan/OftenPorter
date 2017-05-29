package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.Common2;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.EmptyWObject;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;
import cn.xishan.oftenporter.porter.core.JResponse;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;

import java.util.Map;

/**
 * Created by chenyg on 2017-04-29.
 */
public class _JsInterface
{
    public String tableNamePrefix;

    _JsInterface(DBSource dbSource, String tableNamePrefix)
    {
        this.tableNamePrefix = tableNamePrefix;
    }

    public AdvancedExecutor sqlExecutor(String sql, Object argsObj)
    {
        return _sqlExecutor(sql,argsObj);
    }

    static AdvancedExecutor _sqlExecutor(String sql, Object argsObj)
    {
        JSqlArgs jSqlArgs = _sqlArgs(sql, argsObj);
        return SqlAdvancedExecutor.withSqlAndArgs(jSqlArgs.sql, jSqlArgs.args);
    }

    public AdvancedQuery sqlQuery(String sql, Object argsObj)
    {
        return _sqlQuery(sql,argsObj);
    }

    static AdvancedQuery _sqlQuery(String sql, Object argsObj)
    {
        JSqlArgs jSqlArgs = _sqlArgs(sql, argsObj);
        AdvancedQuery advancedQuery = new SqlAdvancedQuery(new SqlUtil.WhereSQL(jSqlArgs.sql, jSqlArgs.args));
        return advancedQuery;
    }

    public JSqlArgs sqlArgs(String sql, Object argsObj)
    {
        return _sqlArgs(sql,argsObj);
    }

    static JSqlArgs _sqlArgs(String sql, Object argsObj)
    {
        Object[] args = toArray(argsObj);
        return new JSqlArgs(sql, args);
    }

    public static Object[] toArray(Object object)
    {
        if (object == null)
        {
            return null;
        } else if (object instanceof ScriptObjectMirror)
        {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) object;
            if (!scriptObjectMirror.isArray())
            {
                throw new IllegalArgumentException("ScriptObjectMirror is no array");
            }

            if (scriptObjectMirror.isEmpty())
            {
                return new Object[0];
            }

            Object[] array = new Object[scriptObjectMirror.size()];

            int i = 0;

            for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet())
            {
                Object result = entry.getValue();
                if (result == null)
                {
                    array[i] = result;
                } else if (result instanceof ScriptObjectMirror && scriptObjectMirror.isArray())
                {
                    array[i] = toArray(result);
                } else if (result instanceof NativeArray)
                {
                    array[i] = ((NativeArray) result).asObjectArray();
                } else
                {
                    array[i] = result;
                }
                i++;
            }

            return array;
        } else if (object instanceof NativeArray)
        {
            Object[] array = ((NativeArray) object).asObjectArray();
            return array;
        } else
        {
            throw new IllegalArgumentException("it is not an array:" + object);
        }
    }

    public String filterLike(String content)
    {
        return SqlUtil.filterLike(content);
    }

}
