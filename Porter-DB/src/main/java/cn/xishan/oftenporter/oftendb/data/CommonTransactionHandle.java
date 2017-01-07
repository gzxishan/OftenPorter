package cn.xishan.oftenporter.oftendb.data;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
abstract class CommonTransactionHandle<T> implements TransactionHandle<T>
{
    private DBHandleSource dbHandleSource;
    private ParamsGetter paramsGetter;

    public CommonTransactionHandle(DBHandleSource dbHandleSource,ParamsGetter paramsGetter)
    {
        this.dbHandleSource = dbHandleSource;
        this.paramsGetter=paramsGetter;
    }

    public DBHandleSource getDBHandleSource()
    {
        return dbHandleSource;
    }

    public ParamsGetter getParamsGetter()
    {
        return paramsGetter;
    }
}
