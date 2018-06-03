package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.TransactionConfig;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

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
    void startTransaction(@MayNull TransactionConfig config) throws DBException;

    /**
     * 提交事务,注意：若之前出现异常，则会回滚,同时抛出异常。
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
