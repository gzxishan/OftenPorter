package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.annotation.DBField;
import cn.xishan.oftenporter.oftendb.annotation.ExceptDBField;
import cn.xishan.oftenporter.oftendb.db.MultiNameValues;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

public class DataUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUtil.class);

    /**
     * 得到字段的绑定名称,如果含有{@linkplain ExceptDBField}注解则会返回null。
     *
     * @param field 使用{@linkplain PortInObj.Nece}、{@linkplain DBField}或{@linkplain PortInObj.UnNece
     * }注解标注字段（外面科技），使用{@linkplain DBField}来映射数据库字段名。
     */
    public static String getTiedName(Field field, boolean withKey)
    {
        if(field.isAnnotationPresent(ExceptDBField.class)){
            return null;
        }
        field.setAccessible(true);
        String name = null;
        if (field.isAnnotationPresent(PortInObj.Nece.class))
        {
            name = PortUtil.tied(field.getAnnotation(PortInObj.Nece.class), field, true);
        } else if (field.isAnnotationPresent(PortInObj.UnNece.class))
        {
            name = PortUtil.tied(field.getAnnotation(PortInObj.UnNece.class), field, true);
        } else if (field.isAnnotationPresent(DBField.class))
        {
            name = field.getName();
        }

        if (withKey && name != null && field.isAnnotationPresent(DBField.class))
        {
            DBField DBField = field.getAnnotation(DBField.class);
            name = DBField.value().equals("") ? (name == null ? field.getName() : name)
                    : DBField.value();
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


    public static NameValues toNameValues(JSONObject jsonObject)
    {
        NameValues nameValues = new NameValues(jsonObject.size());
        for (Map.Entry<String, Object> entry : jsonObject.entrySet())
        {
            nameValues.append(entry.getKey(), entry.getValue());
        }
        return nameValues;
    }

    public static MultiNameValues toMultiNameValues(JSONArray jsonArray)
    {
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String[] names = jsonObject.keySet().toArray(new String[0]);
        MultiNameValues multiNameValues = new MultiNameValues();
        multiNameValues.names(names);

        for (int i = 0; i < jsonArray.size(); i++)
        {
            jsonObject = jsonArray.getJSONObject(i);
            Object[] values = new Object[names.length];
            multiNameValues.addValues(values);
            for (int k = 0; k < names.length; k++)
            {
                values[k] = jsonObject.get(names[k]);
            }
        }
        return multiNameValues;
    }

    /**
     * 若结果码为成功，且结果为JSONObject(不为null)时返回true.
     */
    public static boolean resultJSON(JResponse jResponse)
    {
        if (jResponse.isSuccess())
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
        if (jResponse.isSuccess())
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
     * 当且仅当结果码为成功，且结果为true时返回真；否则返回false。
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean resultTrue(JResponse jResponse)
    {
        Object rs = jResponse.getResult();

        if (jResponse.isSuccess() && rs != null && (rs instanceof Boolean) && (Boolean) rs)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码为成功、且结果为int或long、且值大于0返回true
     *
     * @param jResponse
     * @return
     */
    public static boolean resultIntOrLongGtZero(JResponse jResponse)
    {
        if (jResponse.isNotSuccess())
        {
            return false;
        }
        Object rs = jResponse.getResult();
        if (rs == null)
        {
            return false;
        }
        if (rs instanceof Integer)
        {
            int n = (int) rs;
            return n > 0;
        } else if (rs instanceof Long)
        {
            long n = (long) rs;
            return n > 0;
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
    public static boolean resultNotNull(JResponse jResponse)
    {
        if (jResponse.isSuccess() && jResponse.getResult() != null)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 把fns、finner和fus转换成NameValues对象
     *
     * @param wObject
     * @param wObject
     * @param containsNull 是否包含null值键值对
     * @return
     */
    public static NameValues toNameValues(WObject wObject, boolean containsNull)
    {
        NameValues nameValues = new NameValues();

        try
        {
            InNames.Name[] names = wObject.fInNames.nece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.fn[i] == null)
                {
                    continue;
                }
                nameValues.append(names[i].varName, wObject.fn[i]);
            }
            names = wObject.fInNames.unece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.fu[i] == null)
                {
                    continue;
                }
                nameValues.append(names[i].varName, wObject.fu[i]);
            }
            names = wObject.fInNames.inner;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.finner[i] == null)
                {
                    continue;
                }
                nameValues.append(names[i].varName, wObject.finner[i]);
            }
        } catch (JSONException e)
        {
            LOGGER.warn(e.getMessage(), e);
        }

        return nameValues;
    }

    /**
     * 把fns、finner和fus转换成json对象
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
            names = wObject.fInNames.inner;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wObject.finner[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, wObject.finner[i]);
            }
        } catch (JSONException e)
        {
            throw e;
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
