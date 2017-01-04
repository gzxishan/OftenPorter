package cn.xishan.oftenporter.oftendb.data.impl;


import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;


/**
 * @author ZhuiFeng
 */
public class SourceAndPGetterImpl implements SourceAndPGetter
{
    protected DBHandleSource dbHandleSource;
    private Params params;


    /**
     * @param dataClass
     * @param dbHandleSource
     */
    public SourceAndPGetterImpl(Class<? extends Data> dataClass,
            DBHandleSource dbHandleSource)
    {
        this(dataClass == null ? null : new Params(dataClass),
                dbHandleSource);
    }

    public SourceAndPGetterImpl(DataAble dataAble,
            DBHandleSource dbHandleSource)
    {
        this(new Params(dataAble), dbHandleSource);
    }

    public SourceAndPGetterImpl(Params params,
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
    public DBHandle getDbHandle(ParamsGetter paramsGetter, DBHandle dbHandle) throws DBException
    {
        return dbHandleSource.getDbHandle(paramsGetter, dbHandle);
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

}
