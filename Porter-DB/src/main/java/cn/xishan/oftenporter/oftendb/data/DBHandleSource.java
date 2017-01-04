package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;

/**
 * 用于获取数据库操作
 */
public interface DBHandleSource
{
    /**
     * 新建一个条件
     *
     * @return Condition
     */
    Condition newCondition();

    /**
     * 新建一个查询设置
     *
     * @return QuerySettings
     */
    QuerySettings newQuerySettings();

    /**
     * @param paramsGetter 用于获取一些参数
     * @param dbHandle     使用已经有的操作
     * @return DBHandle
     * @throws DBException
     */
    DBHandle getDbHandle(ParamsGetter paramsGetter, DBHandle dbHandle) throws DBException;

    void afterClose(DBHandle dbHandle);
}
