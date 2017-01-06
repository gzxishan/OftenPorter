package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

/**
 * 用于获取数据库操作
 */
public interface DBHandleSource {
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
     * @param paramsGetter
     * @param dataAble 事物初始化时，此为null。
     * @param dbHandle
     * @return
     * @throws DBException
     */
    DBHandle getDbHandle(ParamsGetter paramsGetter, @MayNull DataAble dataAble, DBHandle dbHandle) throws DBException;

    void afterClose(DBHandle dbHandle);
}
