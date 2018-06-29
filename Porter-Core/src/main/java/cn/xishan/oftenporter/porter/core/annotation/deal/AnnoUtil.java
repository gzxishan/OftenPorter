package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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


    private static final ThreadLocal<Stack<Configable>> threadLocal = new ThreadLocal<>();


    /**
     * 见{@linkplain #popAnnotationConfigable()}
     *
     * @param config
     * @param iAnnotationConfigable
     */
    public static void pushAnnotationConfigable(Object config, IAnnotationConfigable iAnnotationConfigable)
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
        stack.push(new Configable(config, iAnnotationConfigable));
    }

    public static void popAnnotationConfigable()
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


    private static <A extends Annotation> A proxy(A t)
    {
        Stack<Configable> stack = threadLocal.get();
        Configable configable = stack == null ? null : stack.peek();
        if (t != null && configable != null)
        {
            AnnoUtilInvocationHandler handler = new AnnoUtilInvocationHandler(t, configable.iAnnotationConfigable,
                    configable.config);
            t = (A) Proxy.newProxyInstance(t.getClass().getClassLoader(), t.getClass().getInterfaces(), handler);
        }
        return t;
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
