package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlAdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Created by chenyg on 2017-04-29.
 */
public class JsInterface
{
    public String tableNamePrefix;

    public JsInterface(String tableNamePrefix)
    {
        this.tableNamePrefix = tableNamePrefix;
    }


    public AdvancedExecutor sqlExecutor(String sql, Object argsObj)
    {
        JSqlArgs jSqlArgs = sqlArgs(sql, argsObj);
        return SqlAdvancedExecutor.withSqlAndArgs(jSqlArgs.sql, jSqlArgs.args);
    }

    public AdvancedQuery sqlQuery(String sql, Object argsObj)
    {
        JSqlArgs jSqlArgs = sqlArgs(sql, argsObj);
        AdvancedQuery advancedQuery = new SqlAdvancedQuery(new SqlUtil.WhereSQL(jSqlArgs.sql, jSqlArgs.args));
        return advancedQuery;
    }

    public JSqlArgs sqlArgs(String sql, Object argsObj)
    {
        Object[] args;
        if (argsObj == null)
        {
            args = null;
        } else if (argsObj instanceof Object[])
        {
            args = (Object[]) argsObj;
        } else
        {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) argsObj;
            args = new Object[scriptObjectMirror.size()];
            for (int i = 0; i < args.length; i++)
            {
                args[i] = scriptObjectMirror.get(i + "");
            }
        }
        return new JSqlArgs(sql, args);
    }

    public String filterLike(String content)
    {
        return SqlUtil.filterLike(content);
    }

}
