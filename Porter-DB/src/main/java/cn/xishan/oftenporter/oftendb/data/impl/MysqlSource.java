package cn.xishan.oftenporter.oftendb.data.impl;

import cn.xishan.oftenporter.oftendb.data.DBHandleSource;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlCondition;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlQuerySettings;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public abstract class MysqlSource implements DBHandleSource
{
    @Override
    public Condition newCondition()
    {
        return new SqlCondition();
    }

    @Override
    public QuerySettings newQuerySettings()
    {
        return new SqlQuerySettings();
    }

    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }
}
