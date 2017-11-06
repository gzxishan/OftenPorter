package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * @author Created by https://github.com/CLovinr on 2017/7/1.
 */
public class common
{
    /**
     * @see DBCommon#addObjectData(WObject, DBSource, Object, boolean)
     */
    public static JResponse addObjectData(WObject wObject, DBSource dbSource, Object object, boolean filterNullAndEmpty)
    {
        return DBCommon.C.addObjectData(wObject, dbSource, object, filterNullAndEmpty);
    }

    /**
     * @see DBCommon#addData(WObject, DBSource, NameValues)
     */
    public static JResponse addData(WObject wObject, DBSource dbSource, NameValues nameValues)
    {
        return DBCommon.C.addData(wObject, dbSource, nameValues);
    }

    /**
     * @see DBCommon#addData(WObject, DBSource, boolean)
     */
    public static JResponse addData(WObject wObject, DBSource dbSource, boolean containsNull)
    {
        return DBCommon.C.addData(wObject, dbSource, containsNull);
    }

    /**
     * @see DBCommon#addData(WObject, DBSource, MultiNameValues)
     */
    public static JResponse addData(WObject wObject, DBSource dbSource, MultiNameValues multiNameValues)
    {
        return DBCommon.C.addData(wObject, dbSource, multiNameValues);
    }


    /**
     * @see DBCommon#advancedExecute(WObject, DBSource, AdvancedExecutor)
     */

    public static JResponse advancedExecute(WObject wObject, DBSource dbSource, AdvancedExecutor advancedExecutor)
    {
        return DBCommon.C.advancedExecute(wObject, dbSource, advancedExecutor);
    }

    /**
     * @see DBCommon#advancedQuery(WObject, DBSource, AdvancedQuery, QuerySettings)
     */
    public static JResponse advancedQuery(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery,
            QuerySettings querySettings)
    {
        return DBCommon.C.advancedQuery(wObject, dbSource, advancedQuery, querySettings);
    }

    /**
     * @see DBCommon#count(WObject, DBSource, AdvancedQuery)
     */
    public static JResponse count(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery)
    {
        return DBCommon.C.count(wObject, dbSource, advancedQuery);
    }


    /**
     * @see DBCommon#count(WObject, DBSource, Condition)
     */
    public static JResponse count(WObject wObject, DBSource dbSource, Condition condition)
    {
        return DBCommon.C.count(wObject, dbSource, condition);
    }

    /**
     * @see DBCommon#count(WObject, DBSource, String, Object)
     */
    public static JResponse count(WObject wObject, DBSource dbSource, String key, Object value)
    {
        return DBCommon.C.count(wObject, dbSource, key, value);
    }


    /**
     * @see DBCommon#deleteData(WObject, DBSource, Condition)
     */
    public static JResponse deleteData(WObject wObject, DBSource dbSource, Condition condition)
    {
        return DBCommon.C.deleteData(wObject, dbSource, condition);
    }


    /**
     * @see DBCommon#queryData(WObject, DBSource, Condition, QuerySettings, KeysSelection)
     */
    public static JResponse queryData(WObject wObject, DBSource dbSource,
            Condition condition, QuerySettings querySettings, KeysSelection keysSelection)
    {
        return DBCommon.C.queryData(wObject, dbSource, condition, querySettings, keysSelection);
    }


    /**
     * @see DBCommon#queryEnumeration(WObject, DBSource, AdvancedQuery, QuerySettings)
     */
    public static JResponse queryEnumeration(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery,
            QuerySettings querySettings)
    {
        return DBCommon.C.queryEnumeration(wObject, dbSource, advancedQuery, querySettings);
    }

    /**
     * @see DBCommon#queryEnumeration(WObject, DBSource, Condition, QuerySettings, KeysSelection)
     */
    public static JResponse queryEnumeration(WObject wObject, DBSource dbSource, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection)
    {
        return DBCommon.C.queryEnumeration(wObject, dbSource, condition, querySettings, keysSelection);
    }

    /**
     * @see DBCommon#queryOne(WObject, DBSource, Condition, KeysSelection)
     */
    public static JResponse queryOne(WObject wObject, DBSource dbSource, Condition condition,
            KeysSelection keysSelection)
    {
        return DBCommon.C.queryOne(wObject, dbSource, condition, keysSelection);
    }


    /**
     * @see DBCommon#queryOne(WObject, DBSource, AdvancedQuery)
     */
    public static JResponse queryOne(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery)
    {
        return DBCommon.C.queryOne(wObject, dbSource, advancedQuery);
    }

    /**
     * @see DBCommon#replaceData(WObject, DBSource, Condition, NameValues)
     */
    public static JResponse replaceData(WObject wObject, DBSource dbSource, Condition condition, NameValues nameValues)
    {
        return DBCommon.C.replaceData(wObject, dbSource, condition, nameValues);
    }

    /**
     * @see DBCommon#replaceData(WObject, DBSource, Condition, boolean)
     */
    public static JResponse replaceData(WObject wObject, DBSource dbSource, Condition condition, boolean containsNull)
    {
        return DBCommon.C.replaceData(wObject, dbSource, condition, containsNull);
    }

    /**
     * @see DBCommon#updateData(WObject, DBSource, Condition, NameValues)
     */
    public static JResponse updateData(WObject wObject, DBSource dbSource, Condition condition, NameValues nameValues)
    {
        return DBCommon.C.updateData(wObject, dbSource, condition, nameValues);
    }

    /**
     * @see DBCommon#updateObjectData(WObject, DBSource, Condition, Object, boolean, String[])
     */
    public static JResponse updateObjectData(WObject wObject, DBSource dbSource, Condition condition, Object object,
            boolean filterNullAndEmpty, String... excepts)
    {
        return DBCommon.C.updateObjectData(wObject, dbSource, condition, object, filterNullAndEmpty, excepts);
    }

    /**
     * @see DBCommon#updateData(WObject, DBSource, Condition, boolean)
     */
    public static JResponse updateData(WObject wObject, DBSource dbSource, Condition condition, boolean containsNull)
    {
        return DBCommon.C.updateData(wObject, dbSource, condition, containsNull);
    }

}
