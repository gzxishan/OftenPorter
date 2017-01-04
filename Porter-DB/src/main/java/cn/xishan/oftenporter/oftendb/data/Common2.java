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

    private Common common;

    private Common2(Common common)
    {
        this.common = common;
    }


    public static TransactionHandle<Common2> getTransactionHandle(final SourceAndPGetter sourceAndPGeter)
    {
        TransactionHandle<Common2> thandle = new TransactionHandle<Common2>()
        {
            TransactionHandle<Common> transactionHandle = Common.getTransactionHandle(sourceAndPGeter, sourceAndPGeter);
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
    public JResponse addData(SourceAndPGetter sourceAndPGeter, boolean responseData,
            NameValues nameValues, WObject wObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, responseData, nameValues, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, WObject)
     */
    public JResponse addData(SourceAndPGetter sourceAndPGeter, boolean responseData,
            WObject wObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, responseData, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, MultiNameValues, WObject)
     */
    public JResponse addData(SourceAndPGetter sourceAndPGeter,
            final MultiNameValues multiNameValues,
            WObject wObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, multiNameValues, wObject);
    }

    /**
     * @see Common#advancedExecute(DBHandleSource, ParamsGetter, AdvancedExecutor, WObject)
     */
    public JResponse advancedExecute(SourceAndPGetter sourceAndPGeter,
            AdvancedExecutor advancedExecutor,
            WObject wObject)
    {
        return common.advancedExecute(sourceAndPGeter, sourceAndPGeter, advancedExecutor, wObject);
    }

    /**
     * @see Common#count(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse count(SourceAndPGetter sourceAndPGeter, Condition condition,
            WObject wObject)
    {
        return common.count(sourceAndPGeter, sourceAndPGeter, condition, wObject);
    }

    /**
     * @see Common#deleteData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse deleteData(SourceAndPGetter sourceAndPGeter, Condition condition,
            WObject wObject)
    {
        return common.deleteData(sourceAndPGeter, sourceAndPGeter, condition, wObject);
    }


    /**
     * @see Common#deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public JResponse deleteData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return common.deleteData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, wObject);
    }


    /**
     * @see Common#exists(DBHandleSource, ParamsGetter, String, Object, WObject)
     */
    public JResponse exists(SourceAndPGetter sourceAndPGeter, String key,
            Object value, WObject wObject)
    {
        return common.exists(sourceAndPGeter, sourceAndPGeter, key, value, wObject);
    }

    /**
     * @see Common#queryAdvanced(DBHandleSource, ParamsGetter, AdvancedQuery, WObject)
     */
    public JResponse queryAdvanced(SourceAndPGetter sourceAndPGeter,
            AdvancedQuery advancedQuery, WObject wObject)
    {
        return common.queryAdvanced(sourceAndPGeter, sourceAndPGeter, advancedQuery, wObject);
    }

    /**
     * @see Common#queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings, KeysSelection, WObject)
     */
    public JResponse queryData(SourceAndPGetter sourceAndPGeter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return common.queryData(sourceAndPGeter, sourceAndPGeter, condition, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings, KeysSelection, WObject)
     */
    public JResponse queryData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return common
                .queryData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection, WObject)
     */
    public JResponse queryOne(SourceAndPGetter sourceAndPGeter, Condition condition,
            KeysSelection keysSelection, WObject wObject)
    {
        return common.queryOne(sourceAndPGeter, sourceAndPGeter, condition, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection, WObject)
     */
    public JResponse queryOne2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WObject wObject)
    {
        return common.queryOne2(sourceAndPGeter, sourceAndPGeter, paramsSelection, keysSelection, wObject);
    }


    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse replaceData(SourceAndPGetter sourceAndPGeter,
            Condition condition,
            WObject wObject)
    {
        return common.replaceData(sourceAndPGeter, sourceAndPGeter, condition, wObject);
    }

    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public JResponse replaceData(SourceAndPGetter sourceAndPGeter, Condition condition, NameValues nameValues,
            WObject wObject)
    {
        return common.replaceData(sourceAndPGeter, sourceAndPGeter, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public JResponse updateData(SourceAndPGetter sourceAndPGeter, Condition condition,
            NameValues nameValues, WObject wObject)
    {
        return common.updateData(sourceAndPGeter, sourceAndPGeter, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public JResponse updateData(SourceAndPGetter sourceAndPGeter, Condition condition,
            WObject wObject)
    {
        return common.updateData(sourceAndPGeter, sourceAndPGeter, condition, wObject);
    }


    /**
     * @see Common#updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public JResponse updateData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return common.updateData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, wObject);
    }

}
