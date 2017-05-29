package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.sql.Connection;

/**
 * Created by chenyg on 2017-05-15.
 */
public class _SqlSorce
{
    private DBSource dbSource;
    private SqlSource sqlSource;
    private WObject wObject;

    _SqlSorce(DBSource dbSource, SqlSource sqlSource, WObject wObject)
    {
        this.dbSource = dbSource;
        this.sqlSource = sqlSource;
        this.wObject = wObject;
    }

    public Connection getConnection()
    {
        return getConn(wObject);
    }

    private Connection getConn(WObject wObject)
    {
        SqlSource source = Common.getSqlSource(wObject);
        if (source == null)
        {
            source = sqlSource;
        }
        return source.getConnection();
    }

    public JResponse doCommonSqlExecutor(String sql, Object argsObj)
    {
        AdvancedExecutor advancedExecutor = _JsInterface._sqlExecutor(sql, argsObj);
        return Common2.C.advancedExecute(dbSource, advancedExecutor, new EmptyWObject());
    }

    public JResponse doCommonSqlQuery(String sql, Object argsObj)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, argsObj);
        return Common2.C.queryAdvanced(dbSource, advancedQuery, new EmptyWObject());
    }

    public JResponse doCommonSqlOneQuery(String sql, Object argsObj)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, argsObj);
        return Common2.C.queryOneAdvanced(dbSource, advancedQuery, wObject);
    }

    public JResponse doCommonSqlEnumerationQuery(String sql, Object argsObj)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, argsObj);
        return Common2.C.queryEnumeration(dbSource, advancedQuery, null, wObject);
    }
}
