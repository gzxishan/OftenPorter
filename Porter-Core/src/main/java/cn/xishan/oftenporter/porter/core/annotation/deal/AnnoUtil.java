package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class AnnoUtil
{

    private static class Configable
    {
        IAnnotationConfigable iAnnotationConfigable;
        Object config;

        public Configable(Object config, IAnnotationConfigable iAnnotationConfigable)
        {
            this.iAnnotationConfigable = iAnnotationConfigable;
            this.config = config;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnoUtil.class);
    private static final ThreadLocal<Stack<Configable>> threadLocal = new ThreadLocal<>();
    private static Configable defaultConfigable;
    private static Method javaGetAnnotations;

    static
    {
        try
        {
            javaGetAnnotations = AnnotatedElement.class.getMethod("getAnnotationsByType", Class.class);
            javaGetAnnotations.setAccessible(true);
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * 设置或者清除默认的.
     *
     * @param config
     * @param iAnnotationConfigable 为null时表示清除默认的。
     */
    public static synchronized void setDefaultConfigable(Object config, IAnnotationConfigable iAnnotationConfigable)
    {
        if (iAnnotationConfigable == null)
        {
            defaultConfigable = null;
        } else
        {
            Configable configable = new Configable(config, iAnnotationConfigable);
            defaultConfigable = configable;
        }
    }

    /**
     * 见{@linkplain #popAnnotationConfigable()}
     *
     * @param config
     * @param iAnnotationConfigable
     */
    public static synchronized void pushAnnotationConfigable(Object config, IAnnotationConfigable iAnnotationConfigable)
    {
        if (iAnnotationConfigable == null)
        {
            throw new NullPointerException();
        }
        Stack<Configable> stack = threadLocal.get();
        if (stack == null)
        {
            stack = new Stack<>();
            threadLocal.set(stack);
        }
        Configable configable = new Configable(config, iAnnotationConfigable);
        stack.push(configable);
    }

    private static <A extends Annotation> A proxy(A t)
    {
        Stack<Configable> stack = threadLocal.get();
        Configable configable = stack == null ? null : stack.peek();
        if (configable == null)
        {
            synchronized (AnnoUtil.class)
            {
                configable = defaultConfigable;
            }
        }
        if (t != null && configable != null)
        {
            AnnoUtilInvocationHandler handler = new AnnoUtilInvocationHandler(t, configable.iAnnotationConfigable,
                    configable.config);
            t = (A) Proxy.newProxyInstance(t.getClass().getClassLoader(), t.getClass().getInterfaces(), handler);
        }
        return t;
    }

    public static synchronized void popAnnotationConfigable()
    {
        Stack stack = threadLocal.get();
        if (stack != null && !stack.isEmpty())
        {
            stack.pop();
        }
    }

    public static String tied(_UnNece unNece, Field field, boolean enableDefaultValue)
    {
        String name = unNece.getValue();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + unNece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
    }

    public static String tied(_Nece nece, Field field, boolean enableDefaultValue)
    {
        String name = nece.getValue();
        if (WPTool.isEmpty(name))
        {
            if (!enableDefaultValue)
            {
                throw new InitException("default value is not enable for " + nece + " in field '" + field + "'");
            }
            name = field.getName();
        }
        return name;
    }


    static PortMethod[] methods(PortMethod classMethod, PortIn funPorterIn)
    {
        PortMethod[] portMethods = funPorterIn.methods();
        if (portMethods.length == 0)
        {
            portMethods = new PortMethod[]{funPorterIn.method()};
        }
        for (int i = 0; i < portMethods.length; i++)
        {
            PortMethod method = portMethods[i];
            if (classMethod == PortMethod.DEFAULT && method == PortMethod.DEFAULT)
            {
                method = PortMethod.GET;
            } else if (method == PortMethod.DEFAULT)
            {
                method = classMethod;
            }
            portMethods[i] = method;
        }
        return portMethods;
    }

    /**
     * 获取注解，若在当前函数中没有找到且此注解具有继承性则会尝试从父类中的(public)函数中查找。
     *
     * @param method
     * @param annotationClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass)
    {
        A t = getAnnotation(method, annotationClass, annotationClass.isAnnotationPresent(Inherited.class));
        return proxy(t);
    }


    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass)
    {
        A t = clazz.getAnnotation(annotationClass);
        return proxy(t);
    }

    private static <A extends Annotation> A[] getAnnotationsOfProxy(Object obj, Class<A> annotationClass)
    {
        A[] as = getAnnotationsByType(obj, annotationClass);
        for (int i = 0; i < as.length; i++)
        {
            as[i] = proxy(as[i]);
        }
        return as;
    }

    private static <A extends Annotation> A[] getAnnotationsByType(Object obj, Class<A> annotationClass)
    {
        return getAnnotationsByType(obj, annotationClass, annotationClass.isAnnotationPresent(Inherited.class));
    }

    private static <A extends Annotation> A[] getAnnotationsByType(Object obj, Class<A> annotationClass,
            boolean seekSuper)
    {
        try
        {
            A[] as;
            if (javaGetAnnotations != null)
            {
                as = (A[]) javaGetAnnotations.invoke(obj, annotationClass);
                if (as.length == 0 && seekSuper)
                {
                    if (obj instanceof Method)
                    {
                        Method method = (Method) obj;
                        Class<?> superclass = method.getDeclaringClass().getSuperclass();
                        if (superclass != null)
                        {
                            try
                            {
                                method = superclass.getMethod(method.getName(), method.getParameterTypes());
                                as = getAnnotationsByType(method, annotationClass, seekSuper);
                            } catch (NoSuchMethodException e)
                            {
                            }
                        }
                    }
                }
            } else
            {
                try
                {
                    Class<? extends Annotation> annosClass = (Class<? extends Annotation>) Class
                            .forName(annotationClass.getName() + "s");
                    Annotation annos;
                    if (obj instanceof Class)
                    {
                        annos = getAnnotation((Class) obj, annosClass);
                    } else if (obj instanceof Method)
                    {
                        annos = getAnnotation((Method) obj, annosClass);
                    } else
                    {//Field
                        annos = getAnnotation((Field) obj, annosClass);
                    }
                    if (annos != null)
                    {
                        Method valueMethod = annosClass.getDeclaredMethod("value");
                        as = (A[]) valueMethod.invoke(annos);
                    } else
                    {
                        as = (A[]) Array.newInstance(annotationClass, 0);
                    }
                } catch (Exception e)
                {
                    LOGGER.warn(e.getMessage(), e);
                    as = (A[]) Array.newInstance(annotationClass, 0);
                }
            }
            return as;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            return (A[]) Array.newInstance(annotationClass, 0);
        }
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(clazz, annotationClass);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Method method, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(method, annotationClass);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Field field, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(field, annotationClass);
    }


    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass)
    {
        A t = field.getAnnotation(annotationClass);
        return proxy(t);
    }


    public static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationClass)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().equals(annotationClass))
            {
                return proxy((A) annotation);
            }
        }
        return null;
    }


    /**
     * 获取类及其父类上的所有指定注解。
     *
     * @param clazz
     * @param annotationClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> List<A> getAnnotationsWithSuper(Class<?> clazz, Class<A> annotationClass)
    {
        ArrayList<A> arrayList = new ArrayList<>();
        Class<?> c = clazz;
        while (true)
        {
            A t = proxy(c.getDeclaredAnnotation(annotationClass));
            if (t != null)
            {
                arrayList.add(t);
            }
            c = c.getSuperclass();
            if (c == null || c.equals(Object.class))
            {
                break;
            }
        }
        return arrayList;
    }


    private static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass, boolean seekSuper)
    {
        A t = method.getAnnotation(annotationClass);
        if (t == null && seekSuper)
        {
            Class<?> clazz = method.getDeclaringClass().getSuperclass();
            if (clazz != null)
            {
                try
                {
                    method = clazz.getMethod(method.getName(), method.getParameterTypes());
                    t = getAnnotation(method, annotationClass, true);
                } catch (NoSuchMethodException e)
                {
                }
            }
        }
        return t;
    }

    private static boolean isAnnotationPresent(boolean isAll, Class<?> clazz,
            Class<?>... annotationClasses)
    {
        for (Class c : annotationClasses)
        {
            if (clazz.isAnnotationPresent(c))
            {
                if (!isAll)
                {
                    return true;
                }
            } else if (isAll)
            {
                return false;
            }
        }
        return isAll;
    }


    public static boolean isOneOfAnnotationsPresent(Class<?> clazz, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(false, clazz, annotationClasses);
    }

    public static boolean isAllOfAnnotationsPresent(Class<?> clazz, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(true, clazz, annotationClasses);
    }

    private static boolean isAnnotationPresent(boolean isAll, Method method,
            Class<?>... annotationClasses)
    {
        for (Class c : annotationClasses)
        {
            Annotation t = getAnnotation(method, c, c.isAnnotationPresent(Inherited.class));
            if (t != null)
            {
                if (!isAll)
                {
                    return true;
                }
            } else if (isAll)
            {
                return false;
            }
        }
        return isAll;
    }


    public static boolean isOneOfAnnotationsPresent(Method method, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(false, method, annotationClasses);
    }


    public static boolean isAllOfAnnotationsPresent(Method method, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(true, method, annotationClasses);
    }
}
