package cn.xishan.oftenporter.oftendb.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IConnection
{
    Connection getConnection();

    void setQueryTimeoutSeconds(int queryTimeoutSeconds);

    /**
     * 防止重复开启事务
     *
     * @return
     */
    boolean willStartTransactionOk();

    void startTransactionOk();

    boolean willCommit();

    void doCommit() throws SQLException;

    void doRollback() throws SQLException;
}
