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

    public String filterLike(String content)
    {
        return SqlUtil.filterLike(content);
    }

}
