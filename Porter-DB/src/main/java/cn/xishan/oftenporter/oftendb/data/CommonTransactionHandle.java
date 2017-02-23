package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
abstract class CommonTransactionHandle<T> implements TransactionHandle<T>
{
    private DBHandleSource dbHandleSource;
    private ParamsGetter paramsGetter;
    Exception ex;

    public CommonTransactionHandle(DBHandleSource dbHandleSource, ParamsGetter paramsGetter)
    {
        this.dbHandleSource = dbHandleSource;
        this.paramsGetter = paramsGetter;
    }

    public void check()
    {
        if (ex != null)
        {
            throw new RuntimeException("last exception:" + ex.getMessage(), ex);
        }
    }

    public DBHandleSource getDBHandleSource()
    {
        return dbHandleSource;
    }

    public ParamsGetter getParamsGetter()
    {
        return paramsGetter;
    }

    protected void commitTransaction(DBHandle dbHandle) throws DBException
    {
        if(ex==null){
            dbHandle.commitTransaction();
        }else{
            rollback();
            throw new DBException(ex);
        }
    }
}
