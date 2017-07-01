package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.ConfigToDo;
import cn.xishan.oftenporter.oftendb.data.SqlSource;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlCondition;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public abstract class MysqlSource extends DBSourceImpl implements SqlSource
{
    public MysqlSource(ConfigToDo configToDo)
    {
        super(configToDo);
    }

    @Override
    public Condition newCondition()
    {
        return new SqlCondition();
    }
}
