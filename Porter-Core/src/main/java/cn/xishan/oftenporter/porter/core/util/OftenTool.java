package cn.xishan.oftenporter.porter.core.util;


import cn.xishan.oftenporter.porter.simple.DefaultNameValues;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class OftenTool
{
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final Logger LOGGER = LoggerFactory.getLogger(OftenTool.class);

    /**
     * 判断source是否equals给定的targets中的一个
     *
     * @param source  如果为null，直接返回false。
     * @param targets
     * @return
     */
    public static boolean equalsOneOf(Object source, Object... targets)
    {
        boolean eq = false;
        if (source != null)
        {
            for (Object target : targets)
            {
                if (source.equals(target))
                {
                    eq = true;
                    break;
                }
            }
        }
        return eq;
    }

    /**
     * 判断是否为null(对于{@linkplain CharSequence}会判断是否为"").
     * 返回false的情况：
     * <ol>
     * <li>
     * object为null。
     * </li>
     * <li>
     * object为{@linkplain CharSequence},且为""(空字符串)。
     * </li>
     * </ol>
     *
     * @param object
     * @return
     */
    public static boolean isNullOrEmptyCharSequence(Object object)
    {
        boolean is = false;
        if (object == null)
        {
            is = true;
        } else if (object instanceof CharSequence)
        {
            is = "".equals(String.valueOf(object));
        }
        return is;
    }

    public static boolean notNullAndEmptyCharSequence(Object object)
    {
        return !isNullOrEmptyCharSequence(object);
    }

    public static boolean isEmpty(CharSequence object)
    {
        boolean is = false;
        if (object == null)
        {
            is = true;
        } else //if (object instanceof CharSequence)
        {
            is = "".equals(String.valueOf(object));
        }
//        else
//        {
//            if (LOGGER.isWarnEnabled() && (object instanceof Collection || object instanceof Map
//                    || object.getClass().isArray()))
//            {
//                LOGGER.warn("you may have used the wrong way of isEmpty:object={}", object.getClass());
//                StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
//                StringBuilder builder = new StringBuilder();
//                for (int i = 2; i <= 5 && i < stacks.length; i++)
//                {
//                    builder.append("\n\t\t").append(LogUtil.toString(stacks[i]));
//                }
//                LOGGER.warn("{}", builder);
//            }
//        }

        return is;
    }

    /**
     * 判断数组是否为null或者空
     *
     * @param array
     * @return
     */
    public static boolean isEmptyOf(Object[] array)
    {
        return array == null || array.length == 0;
    }

    public static boolean notEmptyOf(Object[] array)
    {
        return !isEmptyOf(array);
    }

    public static boolean notEmptyForAllOf(Object[]... arrays)
    {
        for (Object[] arr : arrays)
        {
            if (isEmptyOf(arr))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断所有是否为空或null。
     *
     * @param arrays
     * @return
     */
    public static boolean isAllEmptyOf(Object[]... arrays)
    {
        for (Object[] arr : arrays)
        {
            if (!isEmptyOf(arr))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean existsNotEmptyOf(Object[]... arrays)
    {
        return !isAllEmptyOf(arrays);
    }

    /**
     * 判断集合是否为null或者空
     *
     * @param collection
     * @return
     */
    public static boolean isEmptyOf(Collection collection)
    {
        return collection == null || collection.isEmpty();
    }

    public static boolean notEmptyOf(Collection collection)
    {
        return !isEmptyOf(collection);
    }

    /**
     * 判断所有是否为空或null。
     *
     * @param collections
     * @return
     */
    public static boolean isAllEmptyOf(Collection... collections)
    {
        for (Collection collection : collections)
        {
            if (!isEmptyOf(collection))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean notEmptyForAllOf(Collection... collections)
    {
        for (Collection collection : collections)
        {
            if (isEmptyOf(collection))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean existsNotEmptyOf(Collection... collections)
    {
        return !isAllEmptyOf(collections);
    }

    /**
     * 判断map是否为null或者空
     *
     * @param map
     * @return
     */
    public static boolean isEmptyOf(Map map)
    {
        return map == null || map.isEmpty();
    }

    public static boolean notEmptyOf(Map map)
    {
        return !isEmptyOf(map);
    }

    /**
     * 判断所有是否为空或null。
     *
     * @param maps
     * @return
     */
    public static boolean isAllEmptyOf(Map... maps)
    {
        for (Map map : maps)
        {
            if (!isEmptyOf(map))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean notEmptyForAllOf(Map... maps)
    {
        for (Map map : maps)
        {
            if (isEmptyOf(map))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean existsNotEmptyOf(Map... maps)
    {
        return !isAllEmptyOf(maps);
    }

    /**
     * 存在空的对象，见{@linkplain #isEmpty(CharSequence)}
     *
     * @param objects
     * @return
     */
    public static boolean existsEmpty(CharSequence... objects)
    {
        return !notEmptyForAll(objects);
    }

    /**
     * 存在非空的对象，见{@linkplain #isEmpty(CharSequence)}
     *
     * @param objects
     * @return
     */
    public static boolean existsNotEmpty(CharSequence... objects)
    {
        return !isEmptyOfAll(objects);
    }

    public static boolean isEmptyOfAll(CharSequence... objects)
    {
        for (CharSequence obj : objects)
        {
            if (notEmpty(obj))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 用{@linkplain #notEmpty(CharSequence)}
     *
     * @param object
     * @return
     */
    @Deprecated
    public static boolean notNullAndEmpty(CharSequence object)
    {
        return notEmpty(object);
    }

    /**
     * 是否不为空，见{@linkplain #isEmpty(CharSequence)}.
     *
     * @param object
     * @return
     */
    public static boolean notEmpty(CharSequence object)
    {
        return !isEmpty(object);
    }


    /**
     * 用{@linkplain #notEmptyForAll(CharSequence...)}
     *
     * @param objects
     * @return
     */
    @Deprecated
    public static boolean notNullAndEmptyForAll(CharSequence... objects)
    {
        return notEmptyForAll(objects);
    }


    /**
     * 判断是否全部都不为空，见{@linkplain #isEmpty(CharSequence)}.
     *
     * @param objects
     * @return
     */
    public static boolean notEmptyForAll(CharSequence... objects)
    {
        for (CharSequence object : objects)
        {
            if (isEmpty(object))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断child是否等于theSuper或者是theSuper的子类、子接口或接口实现者。
     *
     * @param child
     * @param theSuper
     * @return
     */
    public static boolean isAssignable(Class<?> child, Class<?> theSuper)
    {
        return theSuper.isAssignableFrom(child);
    }

    /**
     * 判断child是否等于theSupers中的一个或者是theSupers中某个类的子类、子接口或接口实现者。
     *
     * @param child
     * @param theSupers
     * @return
     */
    public static boolean isAssignableForOneOf(Class child, Class... theSupers)
    {
        for (Class theSuper : theSupers)
        {
            if (isAssignable(child, theSuper))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断childObj是否是theSuper的实例。
     *
     * @param childObj
     * @param theSuper
     * @return
     */
    public static boolean isAssignable(Object childObj, Class<?> theSuper)
    {
        return childObj != null && isAssignable(childObj.getClass(), theSuper);
    }

    /**
     * 判断类是否为接口或抽象类。
     *
     * @param clazz
     * @return
     */
    public static boolean isInterfaceOrAbstract(Class<?> clazz)
    {
        boolean is = clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
        return is;
    }


    /**
     * 获取访问类型：private、默认、protected、public分别返回:0,1,2,3
     *
     * @param method
     * @return
     */
    public static int getAccessType(Method method)
    {
        int mod = method.getModifiers();
        if (Modifier.isPublic(mod))
        {
            return 3;
        } else if (Modifier.isProtected(mod))
        {
            return 2;
        } else if (Modifier.isPrivate(mod))
        {
            return 0;
        } else
        {
            return 1;
        }
    }

    /**
     * 返回c1是c2的第几代子类。
     * -1表示不是子类，0表示是本身，1表示是第一代子类...
     *
     * @param c1 若为null,则会返回-1
     * @param c2
     * @return
     */
    public static int subclassOf(Class<?> c1, Class<?> c2)
    {
        if (c1 == null)
        {
            return -1;
        } else if (c1.getName().equals(c2.getName()))
        {
            return 0;
        } else
        {
            int n = subclassOf(c1.getSuperclass(), c2);
            return n == -1 ? -1 : n + 1;
        }
    }

    /**
     * 将ts的所有元素添加到list中。
     *
     * @param list
     * @param ts
     * @param <T>
     */
    public static final <T> List<T> addAll(List<T> list, T... ts)
    {
        for (T t : ts)
        {
            list.add(t);
        }
        return list;
    }

    public static final <T> Set<T> addAll(Set<T> set, T... ts)
    {
        for (T t : ts)
        {
            set.add(t);
        }
        return set;
    }

    public static boolean includes(Object[] array, Object find)
    {
        for (Object obj : array)
        {
            if (Objects.equals(find, obj))
            {
                return true;
            }
        }
        return false;
    }

    public static String join(String separator, String... strs)
    {
        return OftenStrUtil.join(separator, strs);
    }

    /**
     * 连接成字符串。
     *
     * @param separator 分隔字符串
     * @param args
     * @return
     */
    public static String join(String separator, Object... args)
    {
        return OftenStrUtil.join(separator, args);
    }

    /**
     * 连接成字符串。
     *
     * @param separator  分隔字符串
     * @param collection
     * @return
     */
    public static String join(String separator, Collection<?> collection)
    {
        return OftenStrUtil.join(separator, collection);
    }


    /**
     * 见{@linkplain #isEqual(Object, Object)}
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean notEqual(Object obj1, Object obj2)
    {
        return !isEqual(obj1, obj2);
    }

    /**
     * obj1和obj2都为null或者obj1不为null且obj1.equals(obj2)返回true时，结果为true；否则返回false。
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean isEqual(Object obj1, Object obj2)
    {
        if (obj1 == null)
        {
            return obj2 == null;
        } else
        {
            return obj1.equals(obj2);
        }
    }

    public static void close(PreparedStatement ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            } catch (SQLException e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * 通过反射构建一个实例，必须含有无参构造函数。
     *
     * @param className
     * @param <T>
     * @return
     */
    public static <T> T newObject(String className) throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Class<T> clazz = (Class<T>) PackageUtil.newClass(className, null);
        return newObject(clazz);
    }

    /**
     * 通过反射构建一个实例，必须含有无参构造函数。
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T newObject(
            Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException
    {
        Constructor<T> c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    /**
     * 通过反射构建一个实例，若没有无参构造函数则返回null。
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T newObjectMayNull(
            Class<T> clazz) throws IllegalAccessException, InvocationTargetException,
            InstantiationException
    {
        try
        {
            T t = newObject(clazz);
            return t;
        } catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    /**
     * 获取异常描述。
     *
     * @param throwable
     * @return
     */
    public static String getMessage(Throwable throwable)
    {
        Throwable cause = getCause(throwable);
        if (cause == null)
        {
            cause = throwable;
        }
        String msg = cause.getMessage();
        if (msg == null)
        {
            msg = cause.toString();
        }
        StackTraceElement[] elements = cause.getStackTrace();
        return msg + " " + LogUtil.toString(elements.length > 0 ? elements[0] : null);
    }

    public static Throwable getCause(Throwable throwable)
    {
        if (throwable == null)
        {
            return null;
        }
        throwable = unwrapThrowable(throwable);
        Throwable cause = throwable.getCause();
        if (cause == null)
        {
            cause = throwable;
        } else if (cause != throwable)
        {
            Throwable thr = unwrapThrowable(cause);
            cause = thr.getCause();
            if (cause == null)
            {
                cause = thr;
            }
        }

        if (cause instanceof InvocationTargetException)
        {
            Throwable thr = ((InvocationTargetException) cause).getTargetException();
            if (thr != null)
            {
                cause = thr;
            }
        }

        return cause;
    }

    public static Throwable unwrapThrowable(Throwable wrapped)
    {
        Throwable unwrapped = wrapped;
        while (true)
        {
            if (unwrapped instanceof InvocationTargetException)
            {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException)
            {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else
            {
                return unwrapped;
            }
        }
    }

    public static void deleteFiles(File... files)
    {
        for (File file : files)
        {
            delete(file);
        }
    }

    public static void deleteFiles(List<File> files)
    {
        for (File file : files)
        {
            delete(file);
        }
    }

    public static boolean delete(File file)
    {
        if (file != null)
        {
            try
            {
                return FileTool.delete(file);
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 若不为null则调用关闭closeable.close().
     *
     * @param closeable
     */
    public static void close(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * 若不为null则调用关闭autoCloseable.close().
     *
     * @param autoCloseable
     */
    public static void close(AutoCloseable autoCloseable)
    {
        if (autoCloseable != null)
        {
            try
            {
                autoCloseable.close();
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }


    public static void close(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static void close(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            } catch (SQLException e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }


    /**
     * @param array
     * @param object
     * @return 存在则，返回对应索引；不存在返回-1.
     */
    public static int contains(JSONArray array, Object object)
    {
        int index = -1;
        for (int i = 0; i < array.size(); i++)
        {
            if (array.get(i).equals(object))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 在json对象里是否含有指定的键值
     *
     * @param array  存放的是json对象
     * @param key
     * @param object
     * @return 存在则，返回对应索引；不存在返回-1.
     */
    public static int containsJsonValue(JSONArray array, String key, Object object)
    {
        int index = -1;

        for (int i = 0; i < array.size(); i++)
        {
            JSONObject jsonObject = array.getJSONObject(i);
            if (jsonObject.containsKey(key) && jsonObject.get(key).equals(object))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    private static Map<Class, Field[]> allFieldsCache;
    private static Map<Class, Method[]> allMethodsCache;
    private static Map<Class, Method[]> allPublicMethodsCache;

    static
    {
        initCache();
    }

    private static void initCache()
    {
        allFieldsCache = new WeakHashMap<>();
        allMethodsCache = new WeakHashMap<>();
        allPublicMethodsCache = new WeakHashMap<>();
    }

    public static synchronized void clearCache()
    {
        initCache();
    }

    /**
     * 得到所有字段（任何访问类型，包括父类（除了Object））.
     *
     * @param clazz
     * @return
     */
    public static synchronized Field[] getAllFields(Class<?> clazz)
    {
        Field[] fields = allFieldsCache == null ? null : allFieldsCache.get(clazz);
        if (fields == null)
        {
            List<Field> list = new ArrayList<>();
            if (!Modifier.isInterface(clazz.getModifiers()))
            {
                getAllFields(clazz, list);
            }
            fields = list.toArray(new Field[0]);
            if (allFieldsCache != null)
            {
                allFieldsCache.put(clazz, fields);
            }
        } else
        {
            LOGGER.debug("hit cache:class={},fields={}", clazz, fields.length);
        }
        return fields;
    }

    private static void getAllFields(Class<?> clazz, List<Field> list)
    {
        if (clazz == null || clazz.equals(Object.class))
        {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            list.add(fields[i]);
        }
        getAllFields(clazz.getSuperclass(), list);
    }

    /**
     * 得到所有访问类型的函数（包括父类的）。
     *
     * @param clazz
     * @return
     */
    public static synchronized Method[] getAllMethods(Class<?> clazz)
    {
        Method[] methods = allMethodsCache == null ? null : allMethodsCache.get(clazz);
        if (methods == null)
        {
            if (Modifier.isInterface(clazz.getModifiers()))
            {
                return clazz.getMethods();
            }
            Set<Method> set = new HashSet<>();
            getAllMethods(clazz, set);
            methods = set.toArray(new Method[0]);
            if (allMethodsCache != null)
            {
                allMethodsCache.put(clazz, methods);
            }
        } else
        {
            LOGGER.debug("hit cache:class={},methods={}", clazz, methods.length);
        }
        return methods;
    }

    private static void getAllMethods(Class<?> clazz, Set<Method> set)
    {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            getAllMethods(superClass, set);//获取父类声明的函数
        }
        addAll(set, clazz.getMethods());
        addAll(set, clazz.getDeclaredMethods());
    }


    /**
     * 得到所有公共的函数（包括父类的,但是不包括Object的函数）。
     *
     * @param clazz
     * @return
     */
    public static Method[] getAllPublicMethods(Class<?> clazz)
    {
        Method[] methods = allPublicMethodsCache == null ? null : allPublicMethodsCache.get(clazz);
        if (methods == null)
        {

            Set<Method> set = new HashSet<>();

            Method[] _methods = clazz.getMethods();
            for (Method method : _methods)
            {
                if (method.getDeclaringClass().equals(Object.class))
                {
                    continue;
                }
                set.add(method);
            }

            methods = set.toArray(new Method[0]);
            if (allPublicMethodsCache != null)
            {
                allPublicMethodsCache.put(clazz, methods);
            }
        } else
        {
            LOGGER.debug("hit cache:class={},methods={}", clazz, methods.length);
        }
        return methods;
    }


    /**
     * @param nameValues 必须是key(String),value(Object),key,value...的形式
     * @return
     */
    public static JSONObject fromArray2JSON(Object... nameValues)
    {
        DefaultNameValues defaultNameValues = DefaultNameValues.fromArray(nameValues);
        return defaultNameValues.toJSON();
    }


    public static int getObjectAttrInt(JSONObject jsonObject, String attr)
    {
        return getObjectAttrInt(jsonObject, attr, 0);
    }

    public static int getObjectAttrInt(JSONObject jsonObject, String attr, int defaultValue)
    {
        Integer value = getObjectAttr(Integer.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }


    public static long getObjectAttrLong(JSONObject jsonObject, String attr)
    {
        return getObjectAttrLong(jsonObject, attr, 0);
    }

    public static long getObjectAttrLong(JSONObject jsonObject, String attr, long defaultValue)
    {
        Long value = getObjectAttr(Long.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static float getObjectAttrFloat(JSONObject jsonObject, String attr)
    {
        return getObjectAttrFloat(jsonObject, attr, 0f);
    }

    public static float getObjectAttrFloat(JSONObject jsonObject, String attr, float defaultValue)
    {
        Float value = getObjectAttr(Float.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static double getObjectAttrDouble(JSONObject jsonObject, String attr)
    {
        return getObjectAttrDouble(jsonObject, attr, 0d);
    }

    public static double getObjectAttrDouble(JSONObject jsonObject, String attr, double defaultValue)
    {
        Double value = getObjectAttr(Double.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static short getObjectAttrShort(JSONObject jsonObject, String attr)
    {
        return getObjectAttrShort(jsonObject, attr, (short) 0);
    }

    public static short getObjectAttrShort(JSONObject jsonObject, String attr, short defaultValue)
    {
        Short value = getObjectAttr(Short.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static byte getObjectAttrByte(JSONObject jsonObject, String attr)
    {
        return getObjectAttrByte(jsonObject, attr, (byte) 0);
    }

    public static byte getObjectAttrByte(JSONObject jsonObject, String attr, byte defaultValue)
    {
        Byte value = getObjectAttr(Byte.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static boolean getObjectAttrBoolean(JSONObject jsonObject, String attr)
    {
        return getObjectAttrBoolean(jsonObject, attr, false);
    }

    public static boolean getObjectAttrBoolean(JSONObject jsonObject, String attr, boolean defaultValue)
    {
        Boolean value = getObjectAttr(Boolean.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static Date getObjectAttrDate(JSONObject jsonObject, String attr)
    {
        return getObjectAttrDate(jsonObject, attr, null);
    }

    public static Date getObjectAttrDate(JSONObject jsonObject, String attr, Date defaultValue)
    {
        Date value = getObjectAttr(Date.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static String getObjectAttrString(JSONObject jsonObject, String attr)
    {
        return getObjectAttrString(jsonObject, attr, null);
    }


    public static String getObjectAttrString(JSONObject jsonObject, String attr, String defaultValue)
    {
        String value = getObjectAttr(String.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }

        return value;
    }

    public static JSONObject getObjectAttrJSON(JSONObject jsonObject, String attr)
    {
        return getObjectAttrJSON(jsonObject, attr, null);
    }


    public static JSONObject getObjectAttrJSON(JSONObject jsonObject, String attr, JSONObject defaultValue)
    {
        JSONObject value = getObjectAttr(JSONObject.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }


    public static JSONArray getObjectAttrArray(JSONObject jsonObject, String attr)
    {
        return getObjectAttrArray(jsonObject, attr, null);
    }

    public static JSONArray getObjectAttrArray(JSONObject jsonObject, String attr, JSONArray defaultValue)
    {
        JSONArray value = getObjectAttr(JSONArray.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    public static Object getObjectAttr(JSONObject jsonObject, String attr)
    {
        return getObjectAttr(jsonObject, attr, null);
    }

    public static Object getObjectAttr(JSONObject jsonObject, String attr, Object defaultValue)
    {
        Object value = getObjectAttr(Object.class, jsonObject, attr, null);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    /**
     * 获取对象深层属性。
     *
     * @param type
     * @param jsonObject
     * @param attr       "rs"、"rs.total"等。注意：若有{a:{b:'C'},'a.b':'C2'}，获取'a.b'得到的是'C'；{'a.b':'C2'}获取到的是null。
     * @param <T>
     * @return
     */
    public static <T> T getObjectAttr(Class<T> type, JSONObject jsonObject, String attr, T defaultValue)
    {
        if (jsonObject == null || OftenTool.isEmpty(attr))
        {
            return null;
        }

        Object result = null;
        String[] attrs = OftenStrUtil.split(attr, ".");
        JSONObject currentJson = jsonObject;
        for (int i = 0; i < attrs.length && currentJson != null; i++)
        {
            if (i == attrs.length - 1)
            {
                result = currentJson.getObject(attrs[i], type);
                break;
            } else
            {
                currentJson = currentJson.getJSONObject(attrs[i]);
            }
        }

        if (result == null)
        {
            result = defaultValue;
        }

        return (T) result;
    }

    /**
     * 设置深层属性。
     *
     * @param jsonObject
     * @param attr       "rs"、"rs.total"等
     * @param value
     * @return 返回原来的属性
     */
    public static Object setObjectAttr(JSONObject jsonObject, String attr, Object value)
    {
        if (jsonObject == null)
        {
            throw new NullPointerException("jsonObject is null");
        }

        String[] attrs = OftenStrUtil.split(attr, ".");
        JSONObject currentJson = jsonObject;
        for (int i = 0; i < attrs.length; i++)
        {
            if (i == attrs.length - 1)
            {
                return currentJson.put(attrs[i], value);
            } else
            {
                JSONObject json = currentJson.getJSONObject(attrs[i]);
                if (json == null)
                {
                    json = new JSONObject();
                    currentJson.put(attrs[i], json);
                }
                currentJson = json;
            }
        }

        return null;
    }

}
