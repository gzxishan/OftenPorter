package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * 必须在切面进行了事物控制（{@linkplain cn.xishan.oftenporter.oftendb.data.Common#startTransaction(WObject, DBHandleSource, ParamsGetter)}）
 *
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
@Deprecated
public class Common3
{
    private static void checkTransactionHandle(WObject wObject)
    {
        if (wObject._otherObject == null || !(wObject._otherObject instanceof CommonTransactionHandle))
        {
            throw new RuntimeException(
                    "you should use " + Common.class.getName() + ".startTransaction(...) or " + Common2.class
                            .getName() + ".startTransaction(...)!");
        }
    }

    public static DBHandleSource getDBHandleSource(WObject wObject)
    {
        checkTransactionHandle(wObject);
        CommonTransactionHandle handle = (CommonTransactionHandle) wObject._otherObject;
        return handle.getDBHandleSource();
    }

    public static ParamsGetter getParamsGetter(WObject wObject)
    {
        checkTransactionHandle(wObject);
        CommonTransactionHandle handle = (CommonTransactionHandle) wObject._otherObject;
        return handle.getParamsGetter();
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, NameValues, WObject)
     */
    public static JResponse addData(boolean responseData,
            NameValues nameValues, WObject wObject)
    {

        checkTransactionHandle(wObject);
        return Common.C.addData(null, null, responseData, nameValues, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, WObject)
     */
    public static JResponse addData(boolean responseData,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.addData(null, null, responseData, wObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, MultiNameValues, WObject)
     */
    public static JResponse addData(
            final MultiNameValues multiNameValues,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.addData(null, null, multiNameValues, wObject);
    }

    /**
     * @see Common#advancedExecute(DBHandleSource, ParamsGetter, AdvancedExecutor, WObject)
     */
    public static JResponse advancedExecute(
            AdvancedExecutor advancedExecutor,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.advancedExecute(null, null, advancedExecutor, wObject);
    }

    /**
     * @see Common#count(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public static JResponse count(Condition condition,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.count(null, null, condition, wObject);
    }

    /**
     * @see Common#deleteData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public static JResponse deleteData(Condition condition,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.deleteData(null, null, condition, wObject);
    }


    /**
     * @see Common#deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public static JResponse deleteData2(
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.deleteData2(null, null, paramsSelection, wObject);
    }


    /**
     * @see Common#exists(DBHandleSource, ParamsGetter, String, Object, WObject)
     */
    public static JResponse exists(String key,
            Object value, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.exists(null, null, key, value, wObject);
    }

    /**
     * @see Common#queryAdvanced(DBHandleSource, ParamsGetter, AdvancedQuery, WObject)
     */
    public static JResponse queryAdvanced(
            AdvancedQuery advancedQuery, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.queryAdvanced(null, null, advancedQuery, wObject);
    }

    /**
     * @see Common#queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings, KeysSelection, WObject)
     */
    public static JResponse queryData(Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.queryData(null, null, condition, querySettings, keysSelection, wObject);
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
        checkTransactionHandle(wObject);
        return Common.C.queryEnumeration(null, null, condition, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings, KeysSelection, WObject)
     */
    public static JResponse queryData2(
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.queryData2(null, null, paramsSelection, querySettings, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection, WObject)
     */
    public static JResponse queryOne(Condition condition,
            KeysSelection keysSelection, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.queryOne(null, null, condition, keysSelection, wObject);
    }

    /**
     * @see Common#queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection, WObject)
     */
    public static JResponse queryOne2(
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.queryOne2(null, null, paramsSelection, keysSelection, wObject);
    }


    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public static JResponse replaceData(
            Condition condition,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.replaceData(null, null, condition, wObject);
    }

    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public static JResponse replaceData(Condition condition, NameValues nameValues,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.replaceData(null, null, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject)
     */
    public static JResponse updateData(Condition condition,
            NameValues nameValues, WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.updateData(null, null, condition, nameValues, wObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, WObject)
     */
    public static JResponse updateData(Condition condition,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.updateData(null, null, condition, wObject);
    }


    /**
     * @see Common#updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject)
     */
    public static JResponse updateData2(
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        checkTransactionHandle(wObject);
        return Common.C.updateData2(null, null, paramsSelection, wObject);
    }
}
