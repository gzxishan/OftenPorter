package cn.xishan.oftenporter.oftendb.jbatis;

import cn.xishan.oftenporter.oftendb.data.Common;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.sql.Connection;

/**
 * Created by chenyg on 2017-05-15.
 */
public class _SqlSorce
{
    private SqlSource sqlSource;
    private WObject wObject;

    public _SqlSorce(SqlSource sqlSource, WObject wObject)
    {
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
}
