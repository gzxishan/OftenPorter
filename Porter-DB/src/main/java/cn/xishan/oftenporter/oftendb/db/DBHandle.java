package cn.xishan.oftenporter.oftendb.db;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;

public interface DBHandle extends Closeable
{
    /**
     * 添加。
     *
     * @param nameValues 添加的内容
     * @return 是否添加成功
     * @throws DBException 操作异常
     */
    boolean add(NameValues nameValues) throws DBException;

    void setLogger(Logger logger);

    /**
     * 批量添加.
     *
     * @param multiNameValues 批量添加的内容
     * @return 返回每一条的结果.或者返回一个大小为0的数组表示全部成功
     * @throws DBException 操作异常
     */
    int[] add(MultiNameValues multiNameValues) throws DBException;

    /**
     * 替换。若不存在相关记录，则添加；否则替换。相当于删除已经存在的（若存在），再添加。
     *
     * @param query      条件
     * @param nameValues 要替换的内容
     * @return 是否替换成功。
     * @throws DBException 操作异常
     */
    boolean replace(Condition query, NameValues nameValues) throws DBException;

    /**
     * 删除操作。
     *
     * @param query 判断条件
     * @return 删除的条数
     * @throws DBException 操作异常
     */
    int del(Condition query) throws DBException;

    /**
     * 高级查询，相对简单查询来数，通用性更小。
     *
     * @param advancedQuery 高级查询对象
     * @return 查询结果,存放的元素是json对象
     * @throws DBException 操作异常
     */
    JSONArray advancedQuery(AdvancedQuery advancedQuery) throws DBException;

    DBEnumeration<JSONObject> getDBEnumerations(AdvancedQuery advancedQuery) throws DBException;

    /**
     * @param advancedExecutor 高级执行对象
     * @return 执行结果
     * @throws DBException 操作异常
     */
    Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException;


    /**
     * 简单查询
     *
     * @param query         查询条件
     * @param querySettings 查询选项
     * @param keys          为空表示选择所有
     * @return 查询结果,存放的元素是json对象
     * @throws DBException 操作异常
     */
    JSONArray getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException;

    /**
     * 简单查询
     *
     * @param query         查询条件
     * @param querySettings 查询选项
     * @param keys          为空表示选择所有
     * @return 查询结果
     * @throws DBException 操作异常
     */
    DBEnumeration<JSONObject> getDBEnumerations(Condition query, QuerySettings querySettings, String... keys) throws DBException;


    boolean canOpenOrClose();

    /**
     * 查询一条记录
     *
     * @param query 查询条件
     * @param keys  要获取的键
     * @return 查询结果，未找到返回null。
     * @throws DBException 操作异常
     */
    JSONObject getOne(Condition query, String... keys) throws DBException;

    /**
     * 简单查询,得到指定的键或字段值。
     *
     * @param query         查询条件
     * @param querySettings 查询选项
     * @param key           键名
     * @return 返回查询结果,存放的是key对应的对象
     * @throws DBException 操作异常
     */
    JSONArray get(Condition query, QuerySettings querySettings, String key) throws DBException;

    /**
     * 修改数据。修改记录，若未找到匹配的，则什么都不做。
     *
     * @param query      查询条件
     * @param nameValues 用于修改
     * @return 返回影响的条数
     * @throws DBException 操作异常
     */
    int update(Condition query, NameValues nameValues) throws DBException;

    /**
     * 统计记录条数。
     *
     * @param query 查询条件
     * @return 返回存在的记录数。
     * @throws DBException 操作异常
     */
    long exists(Condition query) throws DBException;

    /**
     * 保存二进制数据
     *
     * @param query  判断条件
     * @param name   键名
     * @param data   要保存的数据
     * @param offset 开始索引
     * @param length 数据长度
     * @return 是否保存成功
     * @throws DBException 操作异常
     */
    boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException;

    /**
     * 得到二进制数据
     *
     * @param query 查询条件
     * @param name  键名
     * @return 未找到返回null，找到返回对应数据
     * @throws DBException 操作异常
     */
    byte[] getBinary(Condition query, String name) throws DBException;

    /**
     * 关闭
     *
     * @throws IOException 操作异常
     */
    void close() throws IOException;

    /**
     * 是否支持事务
     *
     * @return 是否支持事物。
     * @throws DBException 操作异常
     */
    boolean supportTransaction() throws DBException;

    /**
     * 是否已经开启了事务
     *
     * @return 是否开启了事物。
     */
    boolean isTransaction();

    /**
     * 开始事务
     *
     * @throws DBException 操作异常
     */
    void startTransaction() throws DBException;

    /**
     * 提交事务
     *
     * @throws DBException 操作异常
     */
    void commitTransaction() throws DBException;

    /**
     * 事务回滚
     *
     * @throws DBException 操作异常
     */
    void rollback() throws DBException;

    /**
     * 用于设置或得到临时对象
     *
     * @param tempObject 要设置的临时对象。
     * @return 返回上次设置的值
     */
    Object tempObject(Object tempObject);

}
