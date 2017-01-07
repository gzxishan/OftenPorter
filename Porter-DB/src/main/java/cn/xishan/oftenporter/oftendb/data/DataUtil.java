package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.annotation.Key;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class DataUtil
{
private static final Logger LOGGER = LoggerFactory.getLogger(DataUtil.class);
    /**
     * 得到字段的绑定名称。
     *
     * @param field 使用{@linkplain Key}、{@linkplain PortInObj.Nece}或{@linkplain PortInObj.UnNece}注解标注键。
     */
    public static String getTiedName(Field field)
    {
        field.setAccessible(true);
        String name = null;
        if (field.isAnnotationPresent(Key.class))
        {
            Key key = field.getAnnotation(Key.class);
            name = key.value().equals("") ? field.getName()
                    : key.value();
        } else if (field.isAnnotationPresent(PortInObj.Nece.class))
        {
            name = PortUtil.tied(field.getAnnotation(PortInObj.Nece.class), field, true);
        } else if (field.isAnnotationPresent(PortInObj.UnNece.class))
        {
            name = PortUtil.tied(field.getAnnotation(PortInObj.UnNece.class), field, true);
        }
        return name;
    }

    /**
     * 普通搜索
     *
     * @param array 查找的数组
     * @param obj   待查找的值
     * @return 找到返回对应索引，否则返回-1.
     */
    public static int indexOf(Object[] array, Object obj) throws NullPointerException
    {
        int index = -1;
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(obj))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 构造一个ParamsGeter.
     *
     * @param params
     * @return
     */
    public static ParamsGetter newParamsGetter(final ParamsGetter.Params params)
    {
        ParamsGetter paramsGetter = new ParamsGetter()
        {

            @Override
            public Params getParams()
            {
                return params;
            }
        };

        return paramsGetter;
    }


    public static ParamsGetter newParamsGetter(DataAble dataAble, ParamsGetter.DataInitable dataInitable)
    {
        ParamsGetter.Params params = new ParamsGetter.Params(dataAble,dataInitable);
        return newParamsGetter(params);
    }

    public static ParamsGetter newParamsGetter(Class<? extends DataAble> c, ParamsGetter.DataInitable dataInitable)
    {
        ParamsGetter.Params params = new ParamsGetter.Params(c,dataInitable);
        return newParamsGetter(params);
    }


    /**
     * 从WObject参数中构造一个Data（必须有无参构造函数）
     *
     * @param dataClass
     * @param wObject
     * @return
     * @throws NewDataException
     */
    public <T extends DataAble> T createData(Class<T> dataClass, WObject wObject) throws NewDataException
    {
        try
        {
            T t = dataClass.newInstance();
            t.setParams(wObject.fInNames.nece, wObject.fn, wObject.fInNames.unece, wObject.fu, wObject.fInNames.inner,
                    wObject.finner);
            t.whenSetDataFinished(SetType.CREATE, Data.OPTION_CODE_DEFAULT, wObject, null);
            return t;
        } catch (Exception e)
        {
            throw new NewDataException(e.toString());
        }

    }

    /**
     * @param dataAble 默认情况下，null值的类变量不会被添加,除非Key.nullSetOrAdd==true.
     * @return
     * @throws Exception
     */
    public static NameValues toNameValues(ParamsGetter.Params params,
            DataAble dataAble) throws Exception
    {
        return dataAble.toNameValues(params);
    }

    /**
     * 若结果码为成功，且结果为JSONObject(不为null)时返回true.
     */
    public static boolean isResultJSON(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            Object object = jResponse.getResult();
            if (object != null && (object instanceof JSONObject))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 返回-1:结果码不为成功，0：结果码为成功且结果为null，1：结果码为成功且结果不为null。
     *
     * @param jResponse
     * @return
     */
    public static int checkResult(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            if (jResponse.getResult() == null)
            {
                return 0;
            } else
            {
                return 1;
            }
        } else
        {
            return -1;
        }
    }

    /**
     * 当且仅当结果码为成功，且结果为true时返回真；若结果码为成功而结果不为Boolean型，会出现异常。
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean resultTrue(JResponse jResponse)
    {
        Object rs = jResponse.getResult();
        if (jResponse.getCode() == ResultCode.SUCCESS && (Boolean) rs)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码为成功且结果不为null时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean notNull(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS && jResponse.getResult() != null)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码不为成功时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean notSuccess(JResponse jResponse)
    {
        if (jResponse.getCode() != ResultCode.SUCCESS)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码为成功时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean success(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 通过扫描@key注解，结合KeysSelection，得到选择的字段。
     */
    public static String[] getKeys(KeysSelection keysSelection, ParamsGetter paramsGetter)
    {
        ParamsGetter.Params params = paramsGetter.getParams();
        DataAble dataAble = null;
        try
        {
            dataAble = params.getDataAble();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return dataAble.getFinalKeys(keysSelection, params);
    }

    /**
     * 把cns和cus转换成json对象
     *
     * @param wObject
     * @param wObject
     * @param containsNull 是否包含null值键值对
     * @return
     */
    public static JSONObject toJsonObject(WObject wObject, boolean containsNull)
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            InNames.Name[] names = wObject.fInNames.nece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.fn[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, wObject.fn[i]);
            }
            names = wObject.fInNames.unece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.fu[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, wObject.fu[i]);
            }
        } catch (JSONException e)
        {
            LOGGER.warn(e.getMessage(),e);
        }

        return jsonObject;
    }


    public JResponse simpleDeal(SimpleDealt simpleDealt, Object... objects)
    {
        JResponse jResponse = new JResponse();

        try
        {
            simpleDealt.deal(jResponse, objects);
            jResponse.setCode(ResultCode.SUCCESS);
        } catch (Exception e)
        {
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription(e.toString());
            simpleDealt.onException(e, jResponse, objects);
        }

        return jResponse;
    }


}
