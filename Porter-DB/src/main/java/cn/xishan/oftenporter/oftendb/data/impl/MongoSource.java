package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.DBHandleSource;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoCondition;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoQuerySettings;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public abstract class MongoSource implements DBHandleSource
{
    @Override
    public Condition newCondition()
    {
        return new MongoCondition();
    }

    @Override
    public QuerySettings newQuerySettings()
    {
        return new MongoQuerySettings();
    }

    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }
}
