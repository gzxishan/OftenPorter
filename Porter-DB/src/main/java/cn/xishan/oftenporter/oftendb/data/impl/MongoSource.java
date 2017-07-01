package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.ConfigToDo;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoCondition;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public abstract class MongoSource extends DBSourceImpl
{

    public MongoSource(ConfigToDo configToDo)
    {
        super(configToDo);
    }

    @Override
    public Condition newCondition()
    {
        return new MongoCondition();
    }


}
