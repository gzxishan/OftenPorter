package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;

/**
 * Created by 刚帅 on 2016/1/19.
 */
@AutoSet.AutoSetDefaultDealt(dealt = AutoSetDealtForDBSource.class)
public interface DBSource
{
    DBSource newInstance();
    DBSource newInstance(ConfigToDo configToDo);
    /**
     * 新建一个条件
     *
     * @return Condition
     */
    Condition newCondition();
    void afterClose(DBHandle dbHandle);

    DBHandle getDBHandle() throws DBException;
    Configed getConfiged();
    ConfigToDo getConfigToDo();
}
