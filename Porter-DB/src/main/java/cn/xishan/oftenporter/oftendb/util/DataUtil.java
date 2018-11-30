package cn.xishan.oftenporter.oftendb.util;


import cn.xishan.oftenporter.oftendb.annotation.DBField;
import cn.xishan.oftenporter.oftendb.annotation.ExceptDBField;
import cn.xishan.oftenporter.oftendb.db.MultiNameValues;
import cn.xishan.oftenporter.oftendb.db.DBNameValues;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal._NeceUnece;
import cn.xishan.oftenporter.porter.core.annotation.param.*;
import cn.xishan.oftenporter.porter.core.base.FilterEmpty;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUtil.class);
    private static final AnnotationDealt ANNOTATION_DEALT = AnnotationDealt.newInstance(true);
    private static final Map<Class, DataUtilCacheEntity> DATA_UTIL_CACHE_ENTITY_MAP = new ConcurrentHashMap<>();

    /**
     * 见{@linkplain #toNameValues(Object, boolean, boolean, String...)}
     *
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    public static DBNameValues toNameValues(Object object, boolean filterNullAndEmpty)
    {
        return toNameValues(object, filterNullAndEmpty, true);
    }

    public static JSONObject toJSON(Object object, boolean filterNullAndEmpty)
    {
        return toNameValues(object, filterNullAndEmpty, true).toJSON();
    }

    /**
     * @param object             用于提取的实例，见{@linkplain #getTiedName(Field)}、{@linkplain JsonField}、{@linkplain JsonObj}
     * @param filterNullAndEmpty 是否过滤null或空字符串
     * @param isExcept
     * @param keyNames
     * @return
     */
    public static DBNameValues toNameValues(Object object, boolean filterNullAndEmpty, boolean isExcept,
            String... keyNames)
    {
        try
        {
            return _toNameValues(object, filterNullAndEmpty, isExcept, keyNames);
        } catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean seekJsonFieldOrJson(Class realClazz, Field field, DataUtilCacheEntity dataUtilCacheEntity)
    {
        JsonObj jsonObj = AnnoUtil.getAnnotation(field, JsonObj.class);
        if (jsonObj != null)
        {
            String name = jsonObj.value();
            if (name.equals(""))
            {
                name = field.getName();
            }
            Class fieldRealClazz = AnnoUtil.Advance.getRealTypeOfField(realClazz, field);
            dataUtilCacheEntity
                    .addField(new DataUtilCacheEntity.RecursiveEField(field, name, jsonObj.filterNullAndEmpty(),
                            seekCacheEntityField(FilterEmpty.AUTO, fieldRealClazz)));
            return true;
        }

        JsonField jsonField = AnnoUtil.getAnnotation(field, JsonField.class);
        if (jsonField != null)
        {
            String name = jsonField.value();
            if (name.equals(""))
            {
                name = field.getName();
            }
            dataUtilCacheEntity
                    .addField(new DataUtilCacheEntity.EField(field, name, jsonField.filterNullAndEmpty()));
            return true;
        }

        JSONSerialize jsonSerialize = AnnoUtil.getAnnotation(field, JSONSerialize.class);
        if (jsonSerialize != null)
        {
            String name = jsonSerialize.value();
            if (name.equals(""))
            {
                name = field.getName();
            }
            dataUtilCacheEntity
                    .addField(new DataUtilCacheEntity.JsonEField(field, name, jsonSerialize.filterNullAndEmpty()));
            return true;
        }
        return false;
    }

    private static DataUtilCacheEntity seekCacheEntityField(FilterEmpty filterEmpty, Class realClazz)
    {

        DataUtilCacheEntity dataUtilCacheEntity = DATA_UTIL_CACHE_ENTITY_MAP.get(realClazz);
        if (dataUtilCacheEntity != null)
        {
            return dataUtilCacheEntity;
        }

        dataUtilCacheEntity = new DataUtilCacheEntity(filterEmpty);
        DATA_UTIL_CACHE_ENTITY_MAP.put(realClazz, dataUtilCacheEntity);

        Field[] fields = OftenTool.getAllFields(realClazz);

        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            if (seekJsonFieldOrJson(realClazz, field, dataUtilCacheEntity))
            {
                continue;
            }
            String name = getTiedName(field);
            if (name == null)
            {
                continue;
            }
            dataUtilCacheEntity.addField(new DataUtilCacheEntity.EField(field, name, FilterEmpty.AUTO));
        }
        dataUtilCacheEntity.setReady(true);
        return dataUtilCacheEntity;
    }

    private static DBNameValues _toNameValues(Object object, boolean filterNullAndEmpty, boolean isExcept,
            String... keyNames) throws Throwable
    {
        Class clazz = PortUtil.getRealClass(object);
        DataUtilCacheEntity dataUtilCacheEntity = DATA_UTIL_CACHE_ENTITY_MAP.get(clazz);
        if (dataUtilCacheEntity == null || !dataUtilCacheEntity.isReady())
        {
            dataUtilCacheEntity = seekCacheEntityField(FilterEmpty.AUTO, clazz);
        }
        DBNameValues nameValues = dataUtilCacheEntity.toDBNameValues(object, filterNullAndEmpty, isExcept, keyNames);
        return nameValues;
    }

    /**
     * 得到字段的绑定名称,如果含有{@linkplain ExceptDBField}注解则会返回null。
     *
     * @param field 使用{@linkplain Nece}、{@linkplain DBField}或{@linkplain Unece
     *              }注解标注字段，使用{@linkplain DBField}来映射数据库字段名。
     */
    public static String getTiedName(Field field)
    {
        if (field.isAnnotationPresent(ExceptDBField.class))
        {
            return null;
        }
        field.setAccessible(true);

        _NeceUnece neceUnece = null;

        neceUnece = ANNOTATION_DEALT.nece(field);
        if (neceUnece == null)
        {
            neceUnece = ANNOTATION_DEALT.unNece(field);
        }

        if (neceUnece == null)
        {
            DBField dbField = AnnoUtil.getAnnotation(field, DBField.class);
            if (dbField != null)
            {
                String name;
                name = field.getName();
                if (!dbField.value().equals(""))
                {
                    name = dbField.value();
                }
                neceUnece = new _NeceUnece(name);
            }
        }

        String name = null;
        if (neceUnece != null)
        {
            name = InNames.Name.removeConfig(neceUnece.getVarName());
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


    public static DBNameValues toNameValues(JSONObject jsonObject)
    {
        DBNameValues DBNameValues = new DBNameValues(jsonObject.size());
        for (Map.Entry<String, Object> entry : jsonObject.entrySet())
        {
            DBNameValues.append(entry.getKey(), entry.getValue());
        }
        return DBNameValues;
    }

    public static MultiNameValues toMultiNameValues(JSONArray jsonArray)
    {
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String[] names = jsonObject.keySet().toArray(OftenTool.EMPTY_STRING_ARRAY);
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
     * @param oftenObject
     * @param oftenObject
     * @param containsNull 是否包含null值键值对
     * @return
     */
    public static DBNameValues toNameValues(OftenObject oftenObject, boolean containsNull)
    {
        DBNameValues DBNameValues = new DBNameValues();

        try
        {
            InNames.Name[] names = oftenObject._fInNames.nece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._fn[i] == null)
                {
                    continue;
                }
                DBNameValues.append(names[i].varName, oftenObject._fn[i]);
            }
            names = oftenObject._fInNames.unece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._fu[i] == null)
                {
                    continue;
                }
                DBNameValues.append(names[i].varName, oftenObject._fu[i]);
            }
            names = oftenObject._fInNames.inner;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._finner[i] == null)
                {
                    continue;
                }
                DBNameValues.append(names[i].varName, oftenObject._finner[i]);
            }
        } catch (JSONException e)
        {
            LOGGER.warn(e.getMessage(), e);
        }

        return DBNameValues;
    }

    /**
     * 把fns、finner和fus转换成json对象
     *
     * @param oftenObject
     * @param oftenObject
     * @param containsNull 是否包含null值键值对
     * @return
     */
    public static JSONObject toJsonObject(OftenObject oftenObject, boolean containsNull)
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            InNames.Name[] names = oftenObject._fInNames.nece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._fn[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, oftenObject._fn[i]);
            }
            names = oftenObject._fInNames.unece;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._fu[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, oftenObject._fu[i]);
            }
            names = oftenObject._fInNames.inner;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && oftenObject._finner[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i].varName, oftenObject._finner[i]);
            }
        } catch (JSONException e)
        {
            throw e;
        }

        return jsonObject;
    }


//    public JResponse simpleDeal(SimpleDealt simpleDealt, Object... objects)
//    {
//        JResponse jResponse = new JResponse();
//
//        try
//        {
//            simpleDealt.deal(jResponse, objects);
//            jResponse.setCode(ResultCode.SUCCESS);
//        } catch (Exception e)
//        {
//            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
//            jResponse.setDescription(e.toString());
//            simpleDealt.onException(e, jResponse, objects);
//        }
//
//        return jResponse;
//    }
//
//
//    /**
//     * 返回表名，会去掉Porter、Unit、UnitApi、Dao等后缀。
//     *
//     * @param tablePrefix
//     * @param configed
//     * @return
//     */
//    public static String getTableName(String tablePrefix, Configed configed)
//    {
//        String tableName;
//        Object unit = configed.getUnit();
//
//        String name = PortUtil.getRealClass(unit).getSimpleName();
//        int index = name.endsWith("Unit") ? name.lastIndexOf("Unit") : -1;
//        if (index < 0)
//        {
//            index = name.endsWith("Porter") ? name.lastIndexOf("Porter") : -1;
//        }
//
//        if (index < 0)
//        {
//            index = name.endsWith("Dao") ? name.lastIndexOf("Dao") : -1;
//        }
//        if (index < 0)
//        {
//            index = name.endsWith("UnitApi") ? name.lastIndexOf("UnitApi") : -1;
//        }
//        if (index > 0)
//        {
//            tableName = name.substring(0, index);
//        } else
//        {
//            tableName = name;
//        }
//        tableName = tablePrefix + tableName;
//        return tableName;
//    }

}
