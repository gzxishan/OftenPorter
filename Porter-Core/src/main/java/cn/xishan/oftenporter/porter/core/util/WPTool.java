package cn.xishan.oftenporter.porter.core.util;


import cn.xishan.oftenporter.porter.simple.SimpleAppValues;
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
public class WPTool
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WPTool.class);

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
     * <li>
     * object为{@linkplain Collection},且为空。
     * </li>
     * <li>
     * object为{@linkplain Map},且为空。
     * </li>
     * <li>
     * object为数组，且最高维长度为0。
     * </li>
     * </ol>
     *
     * @param object
     * @return
     */
    public static boolean isEmpty(Object object)
    {
        boolean rs = object == null
                || (object instanceof CharSequence) && "".equals(String.valueOf(object))
                || (object instanceof Collection && ((Collection<?>) object).isEmpty())
                || (object instanceof Map && ((Map<?, ?>) object).isEmpty())
                || (object.getClass().isArray() && Array.getLength(object) == 0);
        return rs;
    }

    public static boolean existsEmpty(Object... objects)
    {
        return !notNullAndEmptyForAll(objects);
    }


    public static boolean isEmptyOfAll(Object... objects)
    {
        for (Object obj : objects)
        {
            if (notNullAndEmpty(obj))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否不为空，见{@linkplain #isEmpty(Object)}.
     *
     * @param object
     * @return
     */
    public static boolean notNullAndEmpty(Object object)
    {
        return !isEmpty(object);
    }

    /**
     * 判断是否全部都不为空，见{@linkplain #isEmpty(Object)}.
     *
     * @param objects
     * @return
     */
    public static boolean notNullAndEmptyForAll(Object... objects)
    {
        for (Object object : objects)
        {
            if (isEmpty(object))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断child是否是theSuper的子类、子接口或接口实现者。
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
     * 判断child是否是theSupers中某个类的子类、子接口或接口实现者。
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
    public static final <T> void addAll(List<T> list, T... ts)
    {
        for (T t : ts)
        {
            list.add(t);
        }
    }

    public static final <T> void addAll(Set<T> set, T... ts)
    {
        for (T t : ts)
        {
            set.add(t);
        }
    }


    public static String join(String separator, String... strs)
    {
        return StrUtil.join(separator, strs);
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
        return StrUtil.join(separator, args);
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
        return StrUtil.join(separator, collection);
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
        StackTraceElement element = cause.getStackTrace()[0];
        return msg + " " + LogUtil.toString(element);
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
                return file.delete();
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

    private static Map<Class, Field[]> allFieldsCache = new WeakHashMap<>();
    private static Map<Class, Method[]> allMethodsCache = new WeakHashMap<>();

    public static synchronized void clearCache()
    {
        allFieldsCache.clear();
        allMethodsCache.clear();
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
        }else
        {
            LOGGER.debug("hit cache:class={},fields={}", clazz, fields.length);
        }
        return fields;
    }

    private static void getAllFields(Class<?> clazz, List<Field> list)
    {
        if (clazz.equals(Object.class))
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
     * 得到所有公共的函数（包括父类的）。
     *
     * @param clazz
     * @return
     */
    public static Method[] getAllPublicMethods(Class<?> clazz)
    {
        Method[] methods = clazz.getMethods();
        return methods;
    }



    /**
     * @param nameValues 必须是key(String),value(Object),key,value...的形式
     * @return
     */
    public static JSONObject fromArray2JSON(Object... nameValues)
    {
        SimpleAppValues simpleAppValues = SimpleAppValues.fromArray(nameValues);
        return simpleAppValues.toJson();
    }

}
