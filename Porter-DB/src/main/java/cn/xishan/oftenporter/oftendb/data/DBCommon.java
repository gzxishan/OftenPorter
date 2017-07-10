package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.oftendb.db.exception.CannotOpenOrCloseException;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/7/1.
 */
public class DBCommon
{
    /**
     * 默认的
     */
    public static final DBCommon C = new DBCommon(null);
    private final DBHandle _dbHandle;

    private DBCommon(DBHandle dbHandle)
    {
        this._dbHandle = dbHandle;
    }

    private interface Dealt
    {
        void deal(JResponse jResponse, DBHandle dbHandle, DBSource dbSource) throws Exception;
    }


    private JResponse commonDealt(WObject wObject, Dealt dealt, DBSource dbSource)
    {
        DBCommon common;
        if (wObject._otherObject != null && wObject._otherObject instanceof TransactionHandle)
        {
            CommonTransactionHandle<DBCommon> handle = (CommonTransactionHandle) wObject._otherObject;
            handle.check();
            common = handle.common();
        } else
        {
            common = this;
        }
        return common._commonDealt(wObject, dealt, dbSource);
    }


    private JResponse _commonDealt(WObject wObject, Dealt dealt, DBSource dbSource)
    {
        JResponse jResponse = new JResponse();
        DBHandle dbHandle = null;
        try
        {
            ConfigToDo configToDo = dbSource.getConfigToDo();
            if (this._dbHandle != null)
            {
                dbHandle = this._dbHandle;
            } else
            {
                dbHandle = dbSource.getDBHandle();
            }
            configToDo.atLeastCollectionName(wObject, dbSource.getConfiged(), dbHandle);
            dbHandle.setLogger(LogUtil.logger(wObject, dbHandle.getClass()));

            if (!dbHandle.canOpenOrClose())
            {
                throw new CannotOpenOrCloseException();
            }
            dealt.deal(jResponse, dbHandle, dbSource);

        } catch (DBException e)
        {
            mayTransactionEx(wObject, e);
            jResponse.setCode(ResultCode.DB_EXCEPTION);
            jResponse.setDescription(e.toString());
            jResponse.setExCause(e);
        } catch (Exception e)
        {
            mayTransactionEx(wObject, e);
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription("On OftenDB:" + e.toString());
            jResponse.setExCause(e);
        } finally
        {
            if (dbHandle != null && !dbHandle.isTransaction() && dbHandle.canOpenOrClose())
            {
                WPTool.close(dbHandle);
                dbSource.afterClose(dbHandle);
            }
        }

        return jResponse;
    }

    private void mayTransactionEx(WObject wObject, Exception e)
    {
        if (wObject._otherObject != null && wObject._otherObject instanceof TransactionHandle)
        {
            CommonTransactionHandle handle = (CommonTransactionHandle) wObject._otherObject;
            handle.ex = e;
        }
    }


    private JResponse _addData(WObject wObject, DBSource dbSource, final NameValues nameValues)
    {

        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            boolean success = dbHandle.add(nameValues);
            if (success)
            {
                jResponse.setCode(ResultCode.SUCCESS);
            } else
            {
                jResponse.setCode(ResultCode.OK_BUT_FAILED);
                jResponse.setDescription("add to db failed!");
            }

        };
        return commonDealt(wObject, dealt, dbSource);
    }

    private JResponse _addData(WObject wObject, DBSource dbSource, boolean containsNull)
    {

        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {

            NameValues nameValues = DataUtil.toNameValues(wObject, containsNull);
            boolean success = dbHandle.add(nameValues);
            if (success)
            {
                jResponse.setCode(ResultCode.SUCCESS);
            } else
            {
                jResponse.setCode(ResultCode.OK_BUT_FAILED);
                jResponse.setDescription("add to db failed!");
            }

        };

        return commonDealt(wObject, dealt, dbSource);

    }

    /**
     * 结果码为{@linkplain ResultCode#SUCCESS}表示成功。
     *
     * @see #addData(WObject, DBSource, boolean)
     */
    public JResponse addData(WObject wObject, DBSource dbSource, NameValues nameValues)
    {
        return _addData(wObject, dbSource, nameValues);
    }

    /**
     * 结果码为{@linkplain ResultCode#SUCCESS}表示成功。
     *
     * @see #addData(WObject, DBSource, NameValues)
     */
    public JResponse addData(WObject wObject, DBSource dbSource, boolean containsNull)
    {
        return _addData(wObject, dbSource, containsNull);
    }

    /**
     * 结果码为{@linkplain ResultCode#SUCCESS}表示成功,且结果为json数组，含义见{@linkplain DBHandle#add(MultiNameValues)}返回值。
     *
     * @param wObject
     * @param dbSource
     * @param multiNameValues
     * @return
     */
    public JResponse addData(WObject wObject, DBSource dbSource, MultiNameValues multiNameValues)
    {
        Dealt dealt = new Dealt()
        {
            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DBSource dbSource) throws Exception
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
        };
        return commonDealt(wObject, dealt, dbSource);
    }


    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为
     * {@linkplain DBHandle#advancedExecute(AdvancedExecutor)}的结果
     *
     * @return 操作结果
     */

    public JResponse advancedExecute(WObject wObject, DBSource dbSource, AdvancedExecutor advancedExecutor)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            Object object = dbHandle.advancedExecute(advancedExecutor);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(object);
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为json数组。
     *
     * @param advancedQuery
     * @param wObject
     * @return 操作结果
     */
    public JResponse advancedQuery(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery,
            QuerySettings querySettings)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            JSONArray array = dbHandle.advancedQuery(advancedQuery, querySettings);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(array);
        };
        return commonDealt(wObject, dealt, dbSource);

    }

    /**
     * 查询数据是否存在.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @return
     */
    public JResponse count(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            long n = dbHandle.exists(advancedQuery);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(n);
        };
        return commonDealt(wObject, dealt, dbSource);
    }


    /**
     * 统计数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @return 操作结果
     */
    public JResponse count(WObject wObject, DBSource dbSource, Condition condition)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            long n = dbHandle.exists(condition);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(n);
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * @see #count(WObject, DBSource, Condition)
     */
    public JResponse count(WObject wObject, DBSource dbSource, String key, Object value)
    {
        Condition condition = dbSource.newCondition();
        condition.append(Condition.EQ, new CUnit(key, value));
        return count(wObject, dbSource, condition);
    }


    /**
     * 删除数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为删除的记录个数（int,可能为0）.
     */
    public JResponse deleteData(WObject wObject, DBSource dbSource, Condition condition)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            int n = dbHandle.del(condition);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(n);
        };
        return commonDealt(wObject, dealt, dbSource);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONArray,array里的元素是JSONObject.
     */
    public JResponse queryData(WObject wObject, DBSource dbSource,
            Condition condition, QuerySettings querySettings, KeysSelection keysSelection)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            String[] keys = keysSelection == null ? null : keysSelection.getKeys();
            JSONArray array = dbHandle.getJSONs(condition, querySettings, keys);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(array);
        };
        return commonDealt(wObject, dealt, dbSource);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为{@linkplain DBEnumeration< JSONObject >}.
     */
    public JResponse queryEnumeration(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery,
            QuerySettings querySettings)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            DBEnumeration<JSONObject> enumeration = dbHandle.getDBEnumerations(advancedQuery, querySettings);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(enumeration);
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为{@linkplain DBEnumeration<JSONObject>}.
     */
    public JResponse queryEnumeration(WObject wObject, DBSource dbSource, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            String[] keys = keysSelection == null ? null : keysSelection.getKeys();
            DBEnumeration<JSONObject> enumeration = dbHandle.getDBEnumerations(condition, querySettings, keys);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(enumeration);
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONObject或null.
     */
    public JResponse queryOne(WObject wObject, DBSource dbSource, Condition condition, KeysSelection keysSelection)
    {

        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            String[] keys = keysSelection == null ? null : keysSelection.getKeys();
            JSONObject jsonObject = dbHandle.getOne(condition, keys);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(jsonObject);
        };
        return commonDealt(wObject, dealt, dbSource);
    }


    /**
     * 查询成功时，结果为null或json。
     */
    public JResponse queryOne(WObject wObject, DBSource dbSource, AdvancedQuery advancedQuery)
    {
        QuerySettings querySettings = new QuerySettings();
        querySettings.setLimit(1).setSkip(0);
        JResponse jResponse = advancedQuery(wObject, dbSource, advancedQuery, querySettings);
        if (jResponse.isSuccess())
        {
            JSONArray array = jResponse.getResult();
            JSONObject jsonObject = array.size() > 0 ? array.getJSONObject(0) : null;
            jResponse.setResult(jsonObject);
        }
        return jResponse;
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     */
    public JResponse replaceData(WObject wObject, DBSource dbSource, Condition condition, NameValues nameValues)
    {

        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {

            boolean success = dbHandle.replace(condition, nameValues);
            if (success)
            {
                jResponse.setCode(ResultCode.SUCCESS);
            } else
            {
                jResponse.setCode(ResultCode.OK_BUT_FAILED);
            }
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     */
    public JResponse replaceData(WObject wObject, DBSource dbSource, Condition condition, boolean containsNull)
    {
        NameValues nameValues = DataUtil.toNameValues(wObject, containsNull);
        return replaceData(wObject, dbSource, condition, nameValues);
    }

    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数(int)。
     */
    public JResponse updateData(WObject wObject, DBSource dbSource, Condition condition, NameValues nameValues)
    {
        Dealt dealt = (jResponse, dbHandle, dbSource1) ->
        {
            int n = dbHandle.update(condition, nameValues);
            jResponse.setCode(ResultCode.SUCCESS);
            jResponse.setResult(n);
        };
        return commonDealt(wObject, dealt, dbSource);
    }

    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数(int)。
     */
    public JResponse updateData(WObject wObject, DBSource dbSource, Condition condition, boolean containsNull)
    {
        NameValues nameValues = DataUtil.toNameValues(wObject, containsNull);
        return updateData(wObject, dbSource, condition, nameValues);
    }

    public static SqlSource getSqlSource(WObject wObject)
    {
        SqlSource sqlSource = null;
        if (wObject._otherObject != null && wObject._otherObject instanceof TransactionHandle)
        {
            CommonTransactionHandle handle = (CommonTransactionHandle) wObject._otherObject;
            handle.check();
            Object comm = handle.common();
            DBCommon common = (DBCommon) comm;
            sqlSource = (SqlSource) common._dbHandle;
        }
        return sqlSource;
    }
    ///////////////////////////////////

    private static TransactionHandle getTransactionHandle(WObject wObject)
    {
        if (wObject._otherObject == null || !(wObject._otherObject instanceof TransactionHandle))
        {
            return null;
        }
        TransactionHandle handle = (TransactionHandle) wObject._otherObject;
        return handle;
    }

    public static CheckPassable autoTransaction(TransactionConfirm confirm)
    {
        AutoTransactionCheckPassable checkPassable = new AutoTransactionCheckPassable(confirm);
        return checkPassable;
    }

    /**
     * @param wObject
     * @return 进行了关闭返回true。
     */
    public static boolean closeTransaction(WObject wObject)
    {
        try
        {
            TransactionHandle handle = getTransactionHandle(wObject);
            if (handle == null)
            {
                return false;
            }
            handle.close();
            wObject._otherObject = null;
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    /**
     * 提交事务.<strong>注意见：</strong>{@linkplain TransactionHandle#commitTransaction()}
     *
     * @param wObject
     * @return 进行了提交，则返回true。
     */
    public static boolean commitTransaction(WObject wObject)
    {
        TransactionHandle handle = getTransactionHandle(wObject);
        if (handle == null)
        {
            return false;
        }
        handle.commitTransaction();
        return true;
    }

    /**
     * 回滚事务。
     *
     * @param wObject
     * @return 进行了回滚返回true。
     */
    public static boolean rollbackTransaction(WObject wObject)
    {
        TransactionHandle handle = getTransactionHandle(wObject);
        if (handle == null)
        {
            return false;
        }
        handle.rollback();
        return true;
    }

    /**
     * 开启事务，操作对象被保存在{@linkplain WObject#_otherObject}
     */
    public static void startTransaction(WObject wObject, DBSource dbSource, TransactionConfig config)
    {
        TransactionHandle<DBCommon> handle = getTransactionHandle(wObject, dbSource);
        wObject._otherObject = handle;
        handle.startTransaction(config);
    }

    /**
     * 得到事务操作
     */
    public static TransactionHandle<DBCommon> getTransactionHandle(WObject wObject, DBSource dbSource)
    {
        if (dbSource == null)
        {
            throw new NullPointerException();
        }
        TransactionHandle<DBCommon> transactionHandle;

        TransactionHandle lastHandle = getTransactionHandle(wObject);
        if (lastHandle != null)
        {
            transactionHandle = lastHandle;
        } else
        {
            transactionHandle = new CommonTransactionHandle<DBCommon>(dbSource)
            {
                DBCommon common = initCommon();

                @Override
                public void startTransaction(TransactionConfig transactionConfig) throws DBException
                {
                    if (common._dbHandle.isTransaction())
                    {
                        return;
                    }
                    common._dbHandle.startTransaction(transactionConfig);
                }

                private DBCommon initCommon()
                {
                    DBHandle _dDbHandle_ = getDBSource().getDBHandle();
                    DBCommon common = new DBCommon(_dDbHandle_);

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
                    commitTransaction(common._dbHandle);
                }

                @Override
                public DBCommon common()
                {
                    return common;
                }

                @Override
                public void close() throws IOException
                {
                    common._dbHandle.close();
                    getDBSource().afterClose(common._dbHandle);
                }

                @Override
                public void rollback() throws DBException
                {
                    common._dbHandle.rollback();
                }
            };
        }


        return transactionHandle;
    }


}
