package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

/**
 */
public class DBHandleAccess implements DBHandleSource
{
    private DBHandleSource dbHandleSource;
    private DBHandle dbHandle;


    DBHandleAccess(DBHandleSource dbHandleSource, DBHandle dbHandle)
    {
        this.dbHandleSource = dbHandleSource;
        this.dbHandle = dbHandle;
    }

    /**
     * 得到当前的(与common当前的为同一个)
     *
     * @return
     */
    public DBHandle getCurrentDBHandle()
    {
        return dbHandle;
    }

    @Override
    public Condition newCondition()
    {
        return dbHandleSource.newCondition();
    }

    @Override
    public QuerySettings newQuerySettings()
    {
        return dbHandleSource.newQuerySettings();
    }

    @Override
    public DBHandle getDbHandle(ParamsGetter paramsGetter, @MayNull DataAble dataAble, DBHandle dbHandle) throws DBException
    {
        return dbHandleSource.getDbHandle(paramsGetter,dataAble, dbHandle);
    }

    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }
}
