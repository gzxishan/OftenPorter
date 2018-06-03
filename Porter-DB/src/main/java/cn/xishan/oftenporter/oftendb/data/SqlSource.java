package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.DBException;

import java.sql.Connection;

/**
 * Created by chenyg on 2017-04-29.
 */
public interface SqlSource
{
    Connection getConnection() throws DBException;
}
