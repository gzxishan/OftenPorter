package cn.xishan.oftenporter.oftendb.db.asqlite;

import android.database.sqlite.SQLiteDatabase;
import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.DBException;

/**
 * Created by 宇宙之灵 on 2016/5/3.
 */
public abstract class SqliteAdvancedExecutor extends AdvancedExecutor
{
    protected abstract Object execute(SQLiteDatabase database, SqliteHandle sqliteHandle) throws DBException;

    @Override
    public Object toFinalObject()
    {
        return null;
    }
}
