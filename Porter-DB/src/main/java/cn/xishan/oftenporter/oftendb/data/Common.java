package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.oftendb.data.ParamsGetter.Params;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * 简化一些数据库常用的操作（并不是全部）
 */
public class Common
{

    private interface Dealt
    {
        /**
         * 如果{@linkplain DataAble#forQuery()}为null，才会调用此函数。
         *
         * @return 条件
         */
        Condition getCondition();

        void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                Condition _condition, Object[] otherParams) throws Exception;
    }

    /**
     * 默认的
     */
    public static final Common C = new Common(null);
    private DBHandle _dbHandle;
    private static final Logger LOGGER = LoggerFactory.getLogger(Common.class);

    private Common(DBHandle dbHandle)
    {
        this._dbHandle = dbHandle;
    }


    private JResponse commonDealt(Dealt dealt, boolean willSetParams, DBHandleSource dbHandleSource,
            ParamsGetter paramsGetter,
            WObject wObject, SetType setType, int optionCode, Object... otherParams)
    {
        Common common;
        if (wObject._otherObject != null && wObject._otherObject instanceof TransactionHandle)
        {
            CommonTransactionHandle handle = (CommonTransactionHandle) wObject._otherObject;
            Object comm = handle.common();
            if (comm instanceof Common2)
            {
                comm = ((Common2) comm).common;
            }
            common = (Common) comm;
            dbHandleSource = handle.getDBHandleSource();
            paramsGetter = handle.getParamsGetter() != null ? handle.getParamsGetter() : paramsGetter;
        } else
        {
            common = this;
        }
        return common._commonDealt(dealt, willSetParams, dbHandleSource, paramsGetter, wObject, setType, optionCode,
                otherParams);
    }

    /**
     * @param willSetParams
     * 是否会调用{@linkplain DataAble#setParams(InNames.Name[], Object[], InNames.Name[], Object[], InNames.Name[], Object[])}
     * @param dbHandleSource
     * @param setType        不为空，则会进行
     *                       {@linkplain #setDataFields(DataAble, boolean, WObject, SetType, int, DBHandleAccess)}
     */
    private JResponse _commonDealt(Dealt dealt, boolean willSetParams, DBHandleSource dbHandleSource,
            ParamsGetter paramsGetter,
            WObject wObject, SetType setType, int optionCode, Object... otherParams)
    {


        JResponse jResponse = new JResponse();
        DBHandle dbHandle = null;
        try
        {
            DataAble data;
            Condition condition;
            String rs = null;
            Params params = paramsGetter.getParams();


            data = params.newData(wObject);
            if (setType != null)
            {

                if (dbHandle == null)
                {
                    dbHandle = dbHandleSource.getDbHandle(paramsGetter, data, this._dbHandle);
                }
                rs = setDataFields(data, willSetParams, wObject, setType, optionCode,
                        new DBHandleAccess(dbHandleSource, dbHandle));
            }
            condition = data.forQuery();
            if (condition == null)
            {
                condition = dealt.getCondition();
            }


            if (rs == null)
            {
                if (dbHandle == null)
                {
                    dbHandle = dbHandleSource.getDbHandle(paramsGetter, data, this._dbHandle);
                }

                if (condition != null)
                {
                    data.dealNames(condition);
                }
                dealt.deal(jResponse, dbHandle, paramsGetter, data, condition, otherParams);

            } else
            {
                jResponse.setCode(ResultCode.OK_BUT_FAILED);
                jResponse.setDescription(rs);
            }
        } catch (DBException e)
        {
            jResponse.setCode(ResultCode.DB_EXCEPTION);
            jResponse.setDescription(e.toString());
            jResponse.setExCause(e);

            LOGGER.warn(e.getMessage(), e);

        } catch (Exception e)
        {
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription("On OftenDB:" + e.toString());
            jResponse.setExCause(e);
            LOGGER.warn(e.getMessage(), e);
        } finally
        {
            if (dbHandle != null && !dbHandle.isTransaction())
            {
                WPTool.close(dbHandle);
                dbHandleSource.afterClose(dbHandle);
            }

        }

        return jResponse;
    }


    private Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection paramsSelection, WObject wObject,
            Params params) throws WCallException
    {
        try
        {

            DataAble dataAble = params.getDataAble();

            return dataAble.getQuery(dbHandleSource, paramsSelection, wObject, params);
        } catch (Exception e)
        {
            JResponse jResponse = new JResponse();
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription("On OftenDB:" + e.toString());
            jResponse.setExCause(e);
            LOGGER.warn(e.getMessage(), e);
            WCallException callException = new WCallException(jResponse);

            throw callException;
        }
    }


    /**
     * 开启事务，操作对象被保存在{@linkplain WObject#_otherObject}
     *
     * @param wObject
     * @param dbHandleSource
     * @param paramsGetter
     */
    public static void startTransaction(WObject wObject, DBHandleSource dbHandleSource, ParamsGetter paramsGetter)
    {
        TransactionHandle<Common> handle = getTransactionHandle(dbHandleSource, paramsGetter);
        handle.startTransaction();
        wObject._otherObject = handle;
    }

    /**
     * 提交事务.
     *
     * @param wObject
     * @throws IOException
     */
    public static void commitTransaction(WObject wObject) throws IOException
    {
        TransactionHandle handle = (TransactionHandle) wObject._otherObject;
        handle.commitTransaction();
        handle.close();
        wObject._otherObject = null;
    }

    /**
     * 回滚事务。
     *
     * @param wObject
     * @throws IOException
     */
    public static void rollbackTransaction(WObject wObject) throws IOException
    {
        TransactionHandle handle = (TransactionHandle) wObject._otherObject;
        handle.rollback();
        handle.close();
        wObject._otherObject = null;
    }

    /**
     * 得到事务操作
     */
    public static TransactionHandle<Common> getTransactionHandle(DBHandleSource dbHandleSource,
            ParamsGetter paramsGetter)
    {
        if (dbHandleSource == null || paramsGetter == null)
        {
            throw new NullPointerException();
        }
        CommonTransactionHandle<Common> transactionHandle = new CommonTransactionHandle<Common>(dbHandleSource,
                paramsGetter)
        {
            Common common = initCommon();

            @Override
            public void startTransaction() throws DBException
            {
                common._dbHandle.startTransaction();
            }

            private Common initCommon()
            {
                DBHandle _dDbHandle_ = getDBHandleSource().getDbHandle(getParamsGetter(), null, null);
                Common common = new Common(_dDbHandle_);

                if (!common._dbHandle.supportTransaction())
                {
                    throw new DBException("the dbhandle '" + common._dbHandle.getClass()
                            + "' not support transaction");
                }

                return common;
            }

            @Override
            public void commitTransaction() throws DBException
            {
                common._dbHandle.commitTransaction();
            }

            @Override
            public Common common()
            {
                return common;
            }

            @Override
            public void close() throws IOException
            {
                common._dbHandle.close();
                getDBHandleSource().afterClose(common._dbHandle);
            }

            @Override
            public void rollback() throws DBException
            {
                common._dbHandle.rollback();
            }
        };

        return transactionHandle;
    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, boolean, WObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            WObject wObject)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, wObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, boolean, NameValues, WObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            NameValues nameValues, WObject wObject)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, nameValues, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 添加单条数据.若成功，返回结果码为ResultCode.SUCCESS，若此时响应数据，则结果为JSONObject.
     *
     * @param responseData 是否在添加成功时，返回添加的对象。
     * @param nameValues
     * @param wObject
     * @param optionCode
     * @return 操作结果
     */

    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            final NameValues nameValues, WObject wObject, int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, nameValues, wObject, optionCode);
    }


    private JResponse _addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            final NameValues nameValues, WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                boolean success = dbHandle.add(nameValues);
                if (success)
                {
                    boolean responseData = (Boolean) otherParams[0];
                    jResponse.setCode(ResultCode.SUCCESS);
                    jResponse.setResult(responseData ? data.toJsonObject()
                            : null);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                    jResponse.setDescription("add to db failed!");
                }

            }

            @Override
            public Condition getCondition()
            {
                return null;
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.ADD, optionCode,
                responseData);

    }


    /**
     * 添加单条数据.若成功，返回结果码为ResultCode.SUCCESS，若此时响应数据，则结果为JSONObject.
     *
     * @param responseData 是否在添加成功时，返回添加的对象。
     * @param wObject
     * @param optionCode
     * @return 操作结果
     */

    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            WObject wObject,
            int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, wObject, optionCode);
    }

    private JResponse _addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            WObject wObject,
            int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition condition, Object[] otherParams) throws Exception
            {

                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);
                boolean success = dbHandle.add(nameValues);
                if (success)
                {
                    boolean responseData = (Boolean) otherParams[0];
                    jResponse.setCode(ResultCode.SUCCESS);
                    jResponse.setResult(responseData ? data.toJsonObject()
                            : null);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                    jResponse.setDescription("add to db failed!");
                }

            }

            @Override
            public Condition getCondition()
            {
                return null;
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wObject, SetType.ADD, optionCode,
                responseData);

    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, MultiNameValues, WObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WObject wObject)
    {
        return _addData(dbHandleSource, paramsGetter, multiNameValues, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 批量添加.返回结果码为ResultCode.SUCCESS时，若结果为null不明确;为json数组，里面放的是整型.
     *
     * @param multiNameValues
     * @param wObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WObject wObject, int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, multiNameValues, wObject, optionCode);
    }

    private JResponse _addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                int[] rs = dbHandle.add(multiNameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(toJArray(rs));
            }

            private Object toJArray(int[] rs)
            {
                JSONArray array = null;
                if (rs != null)
                {
                    array = new JSONArray(rs.length);
                    for (int i : rs)
                    {
                        array.add(i);
                    }
                }
                return array;
            }

            @Override
            public Condition getCondition()
            {
                return null;
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.ADD, optionCode);

    }


    /**
     * @see #replaceData(DBHandleSource, ParamsGetter, Condition, WObject, int)
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            Condition condition,
            WObject wObject)
    {
        return replaceData(dbHandleSource, paramsGetter, condition, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     *
     * @param condition
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final Condition condition,
            WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);

                boolean success = dbHandle.replace(_condition, nameValues);
                if (success)
                {
                    jResponse.setCode(ResultCode.SUCCESS);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                }

            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wObject, SetType.REPLACE, optionCode);

    }


    /**
     * @see #replaceData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject, int)
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            NameValues nameValues,
            WObject wObject)
    {
        return replaceData(dbHandleSource, paramsGetter, condition, nameValues, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     *
     * @param condition
     * @param wObject
     * @param nameValues
     * @return
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            final NameValues nameValues,
            WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                boolean success = dbHandle.replace(_condition, nameValues);
                if (success)
                {
                    jResponse.setCode(ResultCode.SUCCESS);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                }

            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.REPLACE, optionCode);

    }


    /**
     * 删除数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为删除的记录个数（可能为0）.
     *
     * @param paramsSelection 用于生成查询条件,不为null才会设置查询条件.
     * @param wObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse deleteData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WObject wObject, int optionCode)
    {
        Condition condition;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wObject,
                    paramsGetter.getParams());
        } catch (WCallException e)
        {
            return e.getJResponse();
        }
        return _deleteData(dbHandleSource, paramsGetter, condition, wObject, optionCode);
    }


    ///////////////////////

    /**
     * @see #deleteData(DBHandleSource, ParamsGetter, Condition, WObject, int)
     */
    public JResponse deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            WObject wObject)
    {
        return _deleteData(dbHandleSource, paramsGetter, condition, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * @see #deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject,
     * int)
     */
    public JResponse deleteData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return deleteData2(dbHandleSource, paramsGetter, paramsSelection, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 删除数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为删除的记录个数（可能为0）.
     *
     * @param condition
     * @param wObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            WObject wObject, int optionCode)
    {
        return _deleteData(dbHandleSource, paramsGetter, condition, wObject, optionCode);
    }


    //////////////////////

    private JResponse _deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                int n = dbHandle.del(_condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };
        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wObject, SetType.DELETE,
                optionCode);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONObject或null.
     *
     * @param condition
     * @param keysSelection
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse queryOne(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            KeysSelection keysSelection, WObject wObject, int optionCode)
    {
        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wObject, optionCode);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONObject或null.
     *
     * @param paramsSelection
     * @param keysSelection
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse queryOne2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WObject wObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wObject,
                    paramsGetter.getParams());
        } catch (WCallException e)
        {
            return e.getJResponse();
        }

        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wObject, optionCode);
    }


    private JResponse _queryOne(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final Condition condition,
            final KeysSelection _keysSelection, WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Params params = paramsGetter.getParams();
                KeysSelection keysSelection = data.keys();
                if (keysSelection == null)
                {
                    keysSelection = _keysSelection;
                }
                String[] keys = data.getFinalKeys(keysSelection,
                        params);// getKeys(data, params.getDataClass(), params.getKeyClass(), _keysSelection,
                // paramsGetter);

                JSONObject jsonObject = dbHandle.getOne(_condition, keys);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(jsonObject);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.QUERY,
                optionCode);

    }

    // /////////////////


    /**
     * @see #queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings,
     * KeysSelection, WObject, int)
     */
    public JResponse queryData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wObject,
                DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * @see #queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings,
     * KeysSelection, WObject, int)
     */
    public JResponse queryData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject)
    {
        return queryData2(dbHandleSource, paramsGetter, paramsSelection, querySettings, keysSelection, wObject,
                DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection,
     * WObject, int)
     */
    public JResponse queryOne(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            KeysSelection keysSelection, WObject wObject)
    {
        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wObject,
                DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection,
     * WObject, int)
     */
    public JResponse queryOne2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WObject wObject)
    {
        return queryOne2(dbHandleSource, paramsGetter, paramsSelection, keysSelection, wObject,
                DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONArray,array里的元素是JSONObject.
     *
     * @param condition
     * @param querySettings
     * @param keysSelection
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse queryData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject, int optionCode)
    {
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wObject, optionCode);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONArray,array里的元素是JSONObject.
     *
     * @param paramsSelection
     * @param querySettings
     * @param keysSelection
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse queryData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WObject wObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wObject,
                    paramsGetter.getParams());
        } catch (WCallException e)
        {
            return e.getJResponse();
        }
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wObject, optionCode);
    }

    private JResponse _queryData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final Condition condition,
            final QuerySettings querySettings, final KeysSelection _keysSelection, WObject wObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Params params = paramsGetter.getParams();
                KeysSelection keysSelection = data.keys();
                if (keysSelection == null)
                {
                    keysSelection = _keysSelection;
                }
                String[] keys = data.getFinalKeys(keysSelection,
                        params);//getKeys(data, params.getDataClass(), params.getKeyClass(), _keysSelection,
                // paramsGetter);
                if (querySettings != null)
                {
                    data.dealNames(querySettings);
                }

                JSONArray array = dbHandle.getJSONs(_condition, querySettings, keys);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(array);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.QUERY,
                optionCode);

    }


    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为json数组。
     *
     * @param advancedQuery
     * @param wObject
     * @return 操作结果
     */
    public JResponse queryAdvanced(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final AdvancedQuery advancedQuery, WObject wObject)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                JSONArray array = dbHandle.advancedQuery(advancedQuery);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(array);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, null, 0);

    }


    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为
     * {@linkplain DBHandle#advancedExecute(AdvancedExecutor)}的结果
     *
     * @param advancedExecutor
     * @param wObject
     * @return 操作结果
     */

    public JResponse advancedExecute(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final AdvancedExecutor advancedExecutor,
            WObject wObject)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Object object = dbHandle.advancedExecute(advancedExecutor);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(object);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, null, 0);

    }


    /**
     * 统计数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @param condition
     * @param wObject
     * @return 操作结果
     */
    public JResponse count(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WObject wObject)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {

                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                long n = dbHandle.exists(_condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.QUERY,
                DataAble.OPTION_CODE_EXISTS);

    }


    /**
     * 查询数据是否存在.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @param key     键名 会进行@Key处理，以替换成数据库对应的名称。
     * @param value   键值
     * @param wObject
     * @return 操作结果
     */
    public JResponse exists(final DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final String key,
            final Object value, WObject wObject)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {

                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Condition condition = dbHandleSource.newCondition();
                Params params = paramsGetter.getParams();
                condition.put(Condition.EQ, new CUnit(key, value));
                long n = dbHandle.exists(condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.QUERY,
                DataAble.OPTION_CODE_EXISTS);

    }


    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param paramsSelection
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse updateData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WObject wObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wObject,
                    paramsGetter.getParams());
        } catch (WCallException e)
        {
            return e.getJResponse();
        }
        return updateData(dbHandleSource, paramsGetter, condition, wObject, optionCode);
    }


    /**
     * @see #updateData(DBHandleSource, ParamsGetter, Condition, WObject, int)
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WObject wObject)
    {
        return updateData(dbHandleSource, paramsGetter, condition, wObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WObject, int)
     */
    public JResponse updateData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WObject wObject)
    {
        return updateData2(dbHandleSource, paramsGetter, paramsSelection, wObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param condition
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final Condition condition,
            WObject wObject, int optionCode)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);
                int n = dbHandle.update(_condition, nameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wObject, SetType.UPDATE,
                optionCode);

    }

    /**
     * @see #updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WObject, int)
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            NameValues nameValues, WObject wObject)
    {
        return updateData(dbHandleSource, paramsGetter, condition, nameValues, wObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param condition
     * @param nameValues
     * @param wObject
     * @param optionCode
     * @return
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            final NameValues nameValues, WObject wObject, int optionCode)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, ParamsGetter paramsGetter, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                int n = dbHandle.update(_condition, nameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wObject, SetType.UPDATE, optionCode);

    }


    /**
     * 设置Data对象的类变量值
     */
    private String setDataFields(DataAble data, boolean willSetParams, WObject wObject, SetType setType,
            int optionCode,
            DBHandleAccess dbHandleAccess)
    {
        String rs = null;
        try
        {
            switch (setType)
            {
                case ADD:
                case REPLACE:
                case UPDATE:
                    if (wObject != null && willSetParams)
                    {
                        InNames inNames = wObject.cInNames;
                        data.setParams(inNames.nece, wObject.cn, inNames.unece, wObject.cu, inNames.inner,
                                wObject.cinner);
                        inNames = wObject.fInNames;
                        data.setParams(inNames.nece, wObject.fn, inNames.unece, wObject.fu, inNames.inner,
                                wObject.finner);
                    }
                    break;
                default:
                    break;

            }
            data.whenSetDataFinished(setType, optionCode, wObject, dbHandleAccess);
        } catch (Exception e)
        {
            e.printStackTrace();
            rs = e.toString();
        }
        return rs;
    }


}
