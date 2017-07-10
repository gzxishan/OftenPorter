package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.ConfigToDo;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlCondition;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public abstract class AsqlitSource  extends DBSourceImpl
{
    public AsqlitSource(ConfigToDo configToDo)
    {
        super(configToDo);
    }

    @Override
    public Condition newCondition()
    {
        return new SqlCondition();
    }

}
