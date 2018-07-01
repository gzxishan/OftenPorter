package cn.xishan.oftenporter.oftendb.data;


import cn.xishan.oftenporter.oftendb.annotation.AnnotationSearch;
import cn.xishan.oftenporter.oftendb.annotation.DBField;
import cn.xishan.oftenporter.oftendb.annotation.MyConsumer;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MyJsonTool
{


    public interface Callback<T>
    {
        /**
         * 转换成功的回调函数
         *
         * @param t
         */
        public void success(T t);

        /**
         * 转换失败的回调函数
         *
         * @param e
         */
        public void error(Exception e);
    }

    /**
     * 依据@Key注解，将对象转换为json对象,设置函数为jsonObject.put(field.getName(),
     * field.get(object))
     *
     * @param object
     * @param exceptFieldsName 排除的字段
     * @return
     * @throws Exception
     */

    public static JSONObject toJsonObject(Object object, String[] exceptFieldsName) throws Exception
    {
        return _toJson(object, exceptFieldsName);
    }

    public static JSONObject toJsonObject(WObject wObject)
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            add(jsonObject, wObject.fInNames.nece, wObject.fn);
            add(jsonObject, wObject.fInNames.unece, wObject.fu);
            add(jsonObject, wObject.cInNames.nece, wObject.cn);
            add(jsonObject, wObject.cInNames.unece, wObject.cu);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static void add(JSONObject jsonObject, InNames.Name[] names, Object[] values) throws JSONException
    {
        if (names != null)
        {
            for (int i = 0; i < names.length; i++)
            {
                jsonObject.put(names[i].varName, values[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject _toJson(final Object object, String[] exceptFieldsName) throws Exception
    {
        final JSONObject jsonObject = new JSONObject();

        AnnotationSearch.searchPublicFields(field -> {
            field.setAccessible(true);
            DBField DBField = AnnoUtil.getAnnotation(field,DBField.class);
            jsonObject.put(DBField.value().equals("") ? field.getName() : DBField.value(), field.get(object));

        },PortUtil.getRealClass( object), exceptFieldsName, DBField.class);

        return jsonObject;
    }

    /**
     * 依据指定的字段注解，将对象转换为json对象,设置函数为jsonObject.put(field.getName(),
     * field.get(object))
     *
     * @param object
     * @param exceptFieldsName 排除的字段
     * @param annotations
     * @return
     * @throws Exception
     */
    public static JSONObject toJsonObject(final Object object, String[] exceptFieldsName,
            Class<? extends Annotation>... annotations) throws Exception
    {
        final JSONObject jsonObject = new JSONObject();

        AnnotationSearch.searchPublicFields(new MyConsumer<Field, Exception>()
        {

            @Override
            public void accept(Field field) throws Exception
            {
                field.setAccessible(true);
                jsonObject.put(field.getName(), field.get(object));

            }
        }, PortUtil.getRealClass(object), exceptFieldsName, annotations);

        return jsonObject;
    }

    /**
     * 依据指定的字段注解，将json对象转换为指定类型的对象（调用该类的无参构造函数实例对象）. <br>
     * 设置变量值通过：field.set(t, jsonObject.get(field.getName())); <br>
     * 支持ArrayList&#60;String&#62;,String[],这些类型特殊处理,把json数组转换过来，如果不为数组，则添加该值。
     *
     * @param returnType
     * @param jsonObject
     * @param ignoreUnfoundField 是否忽略不存在的字段，若为true，则不会设置相应的值，否则抛出异常。
     * @param exceptFieldsName   排除的字段名
     * @param annotations
     * @return
     * @throws Exception
     */
    public static <T> T toObject(Class<T> returnType, final JSONObject jsonObject, boolean ignoreUnfoundField,
            String[] exceptFieldsName, Class<? extends Annotation>... annotations) throws Exception
    {
        final T t;
        try
        {
            t = returnType.newInstance();
        } catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
        initObject(t, jsonObject, ignoreUnfoundField, exceptFieldsName, annotations);

        return t;
    }

    /**
     * 依据指定的字段注解，将json对象的值赋值给指定对象（调用该类的无参构造函数实例对象）. <br>
     * 设置变量值通过：field.set(t, jsonObject.get(field.getName())); <br>
     * 支持ArrayList&#60;String&#62;,String[],这些类型特殊处理,把json数组转换过来，如果不为数组，则添加该值。
     *
     * @param t
     * @param jsonObject
     * @param ignoreUnfoundField 是否忽略不存在的字段，若为true，则不会设置相应的值，否则抛出异常。
     * @param exceptFieldsName   排除的字段名
     * @param annotations
     * @param <T>
     * @throws Exception
     */
    public static <T> void initObject(final T t, final JSONObject jsonObject, final boolean ignoreUnfoundField,
            String[] exceptFieldsName, Class<? extends Annotation>... annotations) throws Exception
    {

        AnnotationSearch.searchPublicFields(new MyConsumer<Field, Exception>()
        {

            @Override
            public void accept(Field field) throws Exception
            {
                if (jsonObject.containsKey(field.getName()))
                {
                    field.setAccessible(true);
                    Object objectValue = jsonObject.get(field.getName());

                    if (objectValue != null && (field.getType().equals(ArrayList.class) || field.getType()
                            .equals(String[].class)))
                    {
                        setOtherField(field, objectValue);
                    } else if (field.getType().toString().startsWith("class"))
                    {
                        field.set(t, objectValue);
                    } else if (objectValue != null)
                    {
                        field.set(t, objectValue);
                    }

                } else if (!ignoreUnfoundField)
                {
                    throw new RuntimeException("field '" + field.getName() + "' in json string is not contained!");
                }

            }

            private void setOtherField(Field field, Object object) throws Exception
            {
                Class<?> type = field.getType();
                ArrayList<String> list = new ArrayList<String>();
                if (object.equals(JSONArray.class))
                {
                    JSONArray array = (JSONArray) object;
                    for (int i = 0; i < array.size(); i++)
                    {
                        list.add(array.getString(i));
                    }
                } else
                {
                    list.add(object.toString());
                }

                if (type.equals(ArrayList.class))
                {
                    field.set(t, list);
                } else if (type.equals(String[].class))
                {
                    String[] strs = list.toArray(new String[0]);
                    field.set(t, strs);
                } else
                {
                    throw new JSONException("not surportted type:" + type);
                }
            }
        }, PortUtil.getRealClass(t), exceptFieldsName, annotations);

    }

    /**
     * @param jsonArrayString
     * @return 转换失败则返回null
     */
    public static JSONArray toJsonArray(String jsonArrayString)
    {
        JSONArray array = null;
        try
        {
            array = JSON.parseArray(jsonArrayString);
        } catch (JSONException e)
        {

        }
        return array;
    }

    /**
     * @param jsonString
     * @return
     */
    public static JSONObject toJsonObject(String jsonString)
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = JSON.parseObject(jsonString);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 依据指定的字段注解，将对象转换为json对象,设置函数为jsonObject.put(field.getName(),
     * field.get(object))
     *
     * @param object
     * @param annotations
     * @param exceptFieldsName 排除的字段名
     */
    public static void toJsonObject(Callback<JSONObject> callback, final Object object, String[] exceptFieldsName,
            Class<? extends Annotation>... annotations)
    {
        try
        {
            final JSONObject jsonObject = new JSONObject();
            MyConsumer<Field, Exception> consumer = new MyConsumer<Field, Exception>()
            {

                @Override
                public void accept(Field field) throws Exception
                {
                    field.setAccessible(true);
                    jsonObject.put(field.getName(), field.get(object));

                }
            };

            AnnotationSearch.searchPublicFields(consumer,PortUtil.getRealClass( object), exceptFieldsName, annotations);
            callback.success(jsonObject);
        } catch (Exception e)
        {
            callback.error(e);
        }

    }

    /**
     * 依据指定的字段注解，将json对象转换为指定类型的对象（调用该类的无参构造函数实例对象）. <br>
     * 设置变量值通过：field.set(t, jsonObject.get(field.getName())); <br>
     * 支持ArrayList&#60;String&#62;,String[],这些类型特殊处理,把json数组转换过来，如果不为数组，则添加该值。
     *
     * @param returnType
     * @param jsonObject
     * @param ignoreUnfoundField 是否忽略不存在的字段，若为true，则不会设置相应的值，否则抛出异常。
     * @param annotations
     * @param exceptFieldsName   排除的字段名
     */
    public static <T> void toObject(Callback<T> callback, Class<T> returnType, final JSONObject jsonObject, boolean
            ignoreUnfoundField, String[] exceptFieldsName, Class<? extends Annotation>... annotations)
    {
        final T t;
        try
        {
            t = returnType.newInstance();
        } catch (Exception e)
        {
            callback.error(e);
            return;
        }
        initObject(callback, t, jsonObject, ignoreUnfoundField, exceptFieldsName, annotations);

    }

    /**
     * 依据指定的字段注解，将json对象的值赋值给指定对象（调用该类的无参构造函数实例对象）. <br>
     * 设置变量值通过：field.set(t, jsonObject.get(field.getName())); <br>
     * 支持ArrayList&#60;String&#62;,String[],这些类型特殊处理,把json数组转换过来，如果不为数组，则添加该值。
     *
     * @param callback
     * @param t
     * @param jsonObject
     * @param ignoreUnfoundField 是否忽略不存在的字段，若为true，则不会设置相应的值，否则抛出异常。
     * @param exceptFieldsName   排除的字段名
     * @param annotations
     * @param <T>
     */
    public static <T> void initObject(Callback<T> callback, final T t, final JSONObject jsonObject, final boolean
            ignoreUnfoundField, String[] exceptFieldsName, Class<? extends Annotation>... annotations)
    {
        try
        {
            MyConsumer<Field, Exception> consumer = new MyConsumer<Field, Exception>()
            {

                @Override
                public void accept(Field field) throws Exception
                {
                    if (jsonObject.containsKey(field.getName()))
                    {
                        Object objectValue = jsonObject.get(field.getName());
//                        if (JSONObject.NULL == objectValue)
//                        {
//                            objectValue = null;
//                        }
                        field.setAccessible(true);
                        if (objectValue != null && (field.getType().equals(ArrayList.class) || field.getType()
                                .equals(String[].class)))
                        {
                            setOtherField(field, objectValue);
                        } else if (field.getType().toString().startsWith("class"))
                        {
                            field.set(t, objectValue);
                        } else if (objectValue != null)
                        {
                            field.set(t, objectValue);
                        }

                    } else if (!ignoreUnfoundField)
                    {
                        throw new RuntimeException("field '" + field.getName() + "' in json string is not contained!");
                    }

                }

                private void setOtherField(Field field, Object object) throws Exception
                {
                    Class<?> type = field.getType();
                    ArrayList<String> list = new ArrayList<String>();
                    if (object.equals(JSONArray.class))
                    {
                        JSONArray array = (JSONArray) object;
                        for (int i = 0; i < array.size(); i++)
                        {
                            list.add(array.getString(i));
                        }
                    } else
                    {
                        list.add(object.toString());
                    }

                    if (type.equals(ArrayList.class))
                    {
                        field.set(t, list);
                    } else if (type.equals(String[].class))
                    {
                        String[] strs = list.toArray(new String[0]);
                        field.set(t, strs);
                    } else
                    {
                        throw new JSONException("not surportted type:" + type);
                    }
                }
            };

            AnnotationSearch.searchPublicFields(consumer, PortUtil.getRealClass(t), exceptFieldsName, annotations);
            callback.success(t);
        } catch (Exception e)
        {
            callback.error(e);
        }

    }

    /**
     * @param jsonArrayString
     */
    public static void toJsonArray(Callback<JSONArray> callback, String jsonArrayString)
    {

        try
        {
            JSONArray array = JSON.parseArray(jsonArrayString);
            callback.success(array);
        } catch (Exception e)
        {
            callback.error(e);

        }

    }

    /**
     * @param jsonString
     */
    public static void toJsonObject(Callback<JSONObject> callback, String jsonString)
    {
        try
        {
            JSONObject jsonObject = JSON.parseObject(jsonString);
            callback.success(jsonObject);
        } catch (Exception e)
        {
            callback.error(e);

        }

    }

}
