package cn.xishan.oftenporter.oftendb.db.sql;

import java.sql.Connection;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IConnection
{
    Connection getConnection();
}
