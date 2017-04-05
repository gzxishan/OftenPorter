package cn.xishan.oftenporter.oftendb.data.impl;


import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;


/**
 * @author ZhuiFeng
 */
public class DBSourceImpl implements DBSource
{
    protected DBHandleSource dbHandleSource;
    private Params params;


    /**
     * @param dataClass
     * @param dbHandleSource
     */
    public DBSourceImpl(Class<? extends Data> dataClass, ParamsGetter.DataInitable dataInitable,
            DBHandleSource dbHandleSource)
    {
        this(dataClass == null ? null : new Params(dataClass, dataInitable),
                dbHandleSource);
    }

    public DBSourceImpl(DataAble dataAble, ParamsGetter.DataInitable dataInitable,
            DBHandleSource dbHandleSource)
    {
        this(new Params(dataAble, dataInitable), dbHandleSource);
    }

    public DBSourceImpl(Params params,
            DBHandleSource dbHandleSource)
    {
        this.params = params;
        this.dbHandleSource = dbHandleSource;
    }

    /**
     * 构造一个Condition
     *
     * @return
     */
    public Condition newCondition()
    {
        return dbHandleSource.newCondition();
    }

    public QuerySettings newQuerySettings()
    {
        return dbHandleSource.newQuerySettings();
    }

    @Override
    public DBHandle getDbHandle(ParamsGetter paramsGetter, @MayNull DataAble dataAble,
            DBHandle dbHandle) throws DBException
    {
        return dbHandleSource.getDbHandle(paramsGetter, dataAble, dbHandle);
    }

    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }

    @Override
    public Params getParams()
    {
        return params;
    }

    @Override
    public DBSource withAnotherData(Class<? extends Data> clazz)
    {
        DBSource dbSource = new DBSourceImpl(clazz, params.getDataInitable(), dbHandleSource);
        return dbSource;
    }
}
