package cn.xishan.oftenporter.oftendb.data;

import java.sql.Connection;

/**
 * Created by chenyg on 2017-04-29.
 */
public interface SqlSource
{
    Connection getConnection();
}
