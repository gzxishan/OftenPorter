package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.DBException;

import java.io.Closeable;
import java.io.IOException;

/**
 * 事务操作
 *
 * @author ZhuiFeng
 */
public interface TransactionHandle<T> extends Closeable
{
    T common();


    /**
     * 开启事务
     *
     * @throws DBException
     */
    void startTransaction() throws DBException;

    /**
     * 提交事务
     *
     * @throws DBException
     */
    void commitTransaction() throws DBException;

    /**
     * 回滚
     *
     * @throws DBException
     */
    void rollback() throws DBException;

    /**
     * 关闭
     */
    void close() throws IOException;
}
