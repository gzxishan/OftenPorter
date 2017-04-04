package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.io.IOException;

/**
 * Created by 刚帅 on 2016/1/19.
 */
public class Common2
{

    public static final Common2 C = new Common2(Common.C);

    Common common;

    private Common2(Common common)
    {
        this.common = common;
    }


    public static void startTransaction(WObject wObject, DBSource dbSource)
    {
        TransactionHandle<Common2> handle = getTransactionHandle(dbSource);
        handle.startTransaction();
        wObject._otherObject = handle;
    }


    public static void commitTransaction(WObject wObject) throws IOException
    {
        TransactionHandle handle = (TransactionHandle) wObject._otherObject;
        handle.commitTransaction();
        handle.close();
        wObject._otherObject = null;
    }


    public static void rollbackTransaction(WObject wObject) throws IOException
    {
        TransactionHandle handle = (TransactionHandle) wObject._otherObject;
        handle.rollback();
        handle.close();
        wObject._otherObject = null;
    }

    public static TransactionHandle<Common2> getTransactionHandle(DBSource dbSource)
    {
        CommonTransactionHandle<Common2> thandle = new CommonTransactionHandle<Common2>(dbSource, dbSource)
        {
            TransactionHandle<Common> transactionHandle = Common
                    .getTransactionHandle(getDBHandleSource(), getParamsGetter());
            Common2 common2 = new Common2(transactionHandle.common());

            @Override
            public Common2 common()
            {
                return common2;
            }

            @Override
            public void startTransaction() throws DBException
            {
                transactionHandle.startTransaction();
            }

            @Override
            public void commitTransaction() throws DBException
            {
                transactionHandle.commitTransaction();
            }

            @Override
            public void rollback() throws DBException
            {
                transactionHandle.rollback();
            }

            @Override
            public void close() throws IOException
            {
                transactionHandle.close();
            }
        };

        return thandle;
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, NameValues, WObject)
     */
    public JResponse addData(DBSource dbSource, boolean responseData,
            NameValues nameValues, WObject wObject)
    {
        return common.addData(dbSource, dbSource, responseData, nameValues, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, WObject)
     */
    public JResponse addData(DBSource dbSource, boolean responseData,
            WObject wObject)
    {
        return common.addData(dbSource, dbSource, responseData, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, MultiNameValues, WObject)
     */
    public JResponse addData(DBSource dbSource,
            final MultiNameValues multiNameValues,
            WObject wObject)
    {
        return common.addData(dbSource, dbSource, multiNameValues, wObject);
    }

    /**
     * @see Common#advancedExecute(DBHandleSource, ParamsGetter, AdvancedExecutor, WObject)
     */
    public JResponse advancedExecute(DBSource dbSource,
            AdvancedExecutor advancedExecutor,
            WObject wObject)
    {
        return common.advancedExecute(dbSource, dbSource, advancedExecutor, wObject);
    }

    /**
     * @see Common#count(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse count(DBSource dbSource, Condition condition,
            WObject wObject)
    {
        return common.count(dbSource, dbSource, condition, wObject);
    }

    /**
     * @see Common#deleteData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse deleteData(DBSource dbSource, Condition condition,
            WObject wObject)
    {
        return common.deleteData(dbSource, dbSource, condition, wObject);
    }


    /**
     * @see Common#deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public JResponse deleteData2(DBSource dbSource,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return common.deleteData2(dbSource, dbSource, paramsSelection, wObject);
    }


    /**
     * @see Common#exists(DBHandleSource, ParamsGetter, String, Object, WObject)
     */
    public JResponse exists(DBSource dbSource, String key,
            Object value, WObject wObject)
    {
        return common.exists(dbSource, dbSource, key, value, wObject);
    }

    /**
     * @see Common#queryAdvanced(DBHandleSource, ParamsGetter, AdvancedQuery, WObject)
     */
    public JResponse queryAdvanced(DBSource dbSource,
            AdvancedQuery advancedQuery, WObject wObject)
    {
        return common.queryAdvanced(dbSource, dbSource, advancedQuery, wObject);
    }

    /**
     * @see Common#queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings, KeysSelection, WObject)
     */
    public JResponse queryData(DBSource dbSource, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return common.queryData(dbSource, dbSource, condition, querySettings, keysSelection, wObject);
    }

    /**
     * @param dbHandleSource
     * @param paramsGetter
     * @param condition
     * @param querySettings
     * @param keysSelection
     * @param wObject
     * @return
     * @see Common#queryEnumeration(DBHandleSource, ParamsGetter, Condition, QuerySettings, KeysSelection, WObject)
     */
    public JResponse queryEnumeration(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return common.queryEnumeration(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings, KeysSelection, WObject)
     */
    public JResponse queryData2(DBSource dbSource,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return common
                .queryData2(dbSource, dbSource, paramsSelection, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection, WObject)
     */
    public JResponse queryOne(DBSource dbSource, Condition condition,
            KeysSelection keysSelection, WObject wObject)
    {
        return common.queryOne(dbSource, dbSource, condition, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection, WObject)
     */
    public JResponse queryOne2(DBSource dbSource,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WObject wObject)
    {
        return common.queryOne2(dbSource, dbSource, paramsSelection, keysSelection, wObject);
    }


    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse replaceData(DBSource dbSource,
            Condition condition,
            WObject wObject)
    {
        return common.replaceData(dbSource, dbSource, condition, wObject);
    }

    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public JResponse replaceData(DBSource dbSource, Condition condition, NameValues nameValues,
            WObject wObject)
    {
        return common.replaceData(dbSource, dbSource, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public JResponse updateData(DBSource dbSource, Condition condition,
            NameValues nameValues, WObject wObject)
    {
        return common.updateData(dbSource, dbSource, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse updateData(DBSource dbSource, Condition condition,
            WObject wObject)
    {
        return common.updateData(dbSource, dbSource, condition, wObject);
    }


    /**
     * @see Common#updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public JResponse updateData2(DBSource dbSource,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return common.updateData2(dbSource, dbSource, paramsSelection, wObject);
    }

}
