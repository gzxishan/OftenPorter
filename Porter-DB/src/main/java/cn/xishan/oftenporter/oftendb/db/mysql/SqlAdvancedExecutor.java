package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.DBException;

import java.sql.Connection;

public abstract class SqlAdvancedExecutor extends AdvancedExecutor
{

    protected abstract Object execute(Connection connection, SqlHandle sqlHandle) throws DBException;

    @Override
    public final Object toFinalObject()
    {
        return null;
    }
}
