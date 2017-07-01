package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.DBHandle;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
abstract class CommonTransactionHandle<T> implements TransactionHandle<T>
{
    private DBSource dbSource;
    Exception ex;

    public CommonTransactionHandle(DBSource dbSource)
    {
        this.dbSource = dbSource;
    }

    public void check()
    {
        if (ex != null)
        {
            throw new RuntimeException("last exception:" + ex.getMessage(), ex);
        }
    }

    public DBSource getDBSource()
    {
        return dbSource;
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
