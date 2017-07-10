package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.DBCommon;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlHandle;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by chenyg on 2017-05-15.
 */
public class _SqlSorce
{
    private DBSource dbSource;
    private SqlSource sqlSource;
    private WObject wObject;
    private Logger LOGGER;

    private static final Object[] ZERO_ARGS = new Object[0];

    _SqlSorce(DBSource dbSource, SqlSource sqlSource, WObject wObject, Logger LOGGER)
    {
        this.dbSource = dbSource;
        this.sqlSource = sqlSource;
        this.wObject = wObject;
        this.LOGGER = LOGGER;
    }

    public Connection getConnection()
    {
        return getConn(wObject);
    }

    private Connection getConn(WObject wObject)
    {
        SqlSource source = DBCommon.getSqlSource(wObject);
        if (source == null)
        {
            source = sqlSource;
        }
        return source.getConnection();
    }


    public JResponse doCommonSqlExecutor(String sql)
    {
        return doCommonSqlExecutor(sql, ZERO_ARGS);
    }

    public JResponse doCommonSqlExecutor(String sql, Object[] args)
    {
        AdvancedExecutor advancedExecutor = _JsInterface._sqlExecutor(sql, args);
        return DBCommon.C.advancedExecute(wObject, dbSource, advancedExecutor);
    }

    public JResponse doCommonSqlQuery(String sql)
    {
        return doCommonSqlQuery(sql, ZERO_ARGS);
    }

    public JResponse doCommonSqlQuery(String sql, Object[] args)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, args);
        return DBCommon.C.advancedQuery(wObject, dbSource, advancedQuery, null);
    }

    public JResponse doCommonSqlOneQuery(String sql)
    {
        return doCommonSqlOneQuery(sql, ZERO_ARGS);
    }

    public JResponse doCommonSqlOneQuery(String sql, Object[] args)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, args);
        return DBCommon.C.queryOne(wObject, dbSource, advancedQuery);
    }


    public JResponse doCommonSqlEnumerationQuery(String sql)
    {
        return doCommonSqlEnumerationQuery(sql, ZERO_ARGS);
    }

    public JResponse doCommonSqlEnumerationQuery(String sql, Object[] args)
    {
        AdvancedQuery advancedQuery = _JsInterface._sqlQuery(sql, args);
        return DBCommon.C.queryEnumeration(wObject, dbSource, advancedQuery, null);
    }

    public BlobData getBlobData(String sql, String columnName)
    {
        return getBlobData(sql, ZERO_ARGS, columnName);
    }

    public BlobData getBlobData(String sql, Object[] args, String columnName)
    {
        Connection conn = null;
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("{}", sql);
                StringBuilder builder = new StringBuilder();
                SqlHandle.logArgs(args, builder);
                LOGGER.debug("{}", builder);
            }
            conn = sqlSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++)
            {
                Object obj = args[i];
                SqlHandle.setObject(ps, i + 1, obj);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                Blob blob = rs.getBlob(columnName);
                return new BlobDataImpl(conn, blob);
            } else
            {
                WPTool.close(conn);
                return null;
            }
        } catch (Exception e)
        {
            WPTool.close(conn);
            throw new DBException(e);
        }


    }
}
