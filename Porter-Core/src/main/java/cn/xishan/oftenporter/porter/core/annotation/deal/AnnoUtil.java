package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.util.ResourceUtil;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;
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
        IConfigData config;

        public Configable(IConfigData config, IAnnotationConfigable iAnnotationConfigable)
        {
            this.iAnnotationConfigable = iAnnotationConfigable;
            this.config = config;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnoUtil.class);
    private static final ThreadLocal<Stack<Configable>> threadLocal = new ThreadLocal<>();
    private static Configable defaultConfigable;
    private static Method javaGetAnnotations;
    private static IDynamicAnnotationImprovable[] DYNAMIC_ANNOTATION_IMPROVABLES;

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

        Set<IDynamicAnnotationImprovable> iDynamicAnnotationImprovableList = new HashSet<>();
        try
        {
            String path = "/OP-INF/cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable";
            List<String> dynamics = ResourceUtil.getAbsoluteResourcesString(path, "utf-8");
            try
            {
                for (String _classNames : dynamics)
                {
                    if (WPTool.isEmpty(_classNames))
                    {
                        continue;
                    }
                    String[] classNames = StrUtil.split(_classNames.trim(), "\n");
                    for (String className : classNames)
                    {
                        IDynamicAnnotationImprovable iDynamicAnnotationImprovable = WPTool.newObject(className);
                        iDynamicAnnotationImprovableList.add(iDynamicAnnotationImprovable);
                    }
                }
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        DYNAMIC_ANNOTATION_IMPROVABLES = iDynamicAnnotationImprovableList.toArray(new IDynamicAnnotationImprovable[0]);
    }

    public static IDynamicAnnotationImprovable[] getDynamicAnnotationImprovables()
    {
        return DYNAMIC_ANNOTATION_IMPROVABLES;
    }

    public static void setIDynamicAnnotationImprovables(IDynamicAnnotationImprovable[] dynamicAnnotationImprovables)
    {
        DYNAMIC_ANNOTATION_IMPROVABLES = dynamicAnnotationImprovables;
    }

    /**
     * 见{@linkplain IDynamicAnnotationImprovable}
     */
    public static class Advanced
    {
        /**
         * 代理指定的注解，使得注解字串内容支持参数。
         */
        public static <A extends Annotation> A doProxyForDynamicAttr(A a)
        {
            return proxyAnnotationForAttr(a);
        }

        private static <A extends Annotation> A newProxyAnnotation(IDynamicAnnotationImprovable.Result<?, A> result,
                InvocationHandler invocationHandler)
        {
            Class<A> annotationClass = result.appendAnnotation;
            AnnoUtilDynamicHandler handler = new AnnoUtilDynamicHandler(invocationHandler, annotationClass,
                    result.willHandleCommonMethods());
            Object obj = Proxy.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
                    annotationClass
            }, handler);
            A a = (A) obj;
            return proxyAnnotationForAttr(a);
        }

        /**
         * 获取泛型字段的实际类型
         *
         * @param field     声明变量
         * @param realClass 实际子类
         * @return
         */
        public static Class getFieldRealType(Field field, Class<?> realClass)
        {
            Type fc = field.getGenericType();
            Class type = field.getType();
            Type realType = realClass.getGenericSuperclass();
            if (fc != null && realType instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) realType;

                TypeVariable[] typeVariables = field.getDeclaringClass().getTypeParameters();
                for (int i = 0; i < typeVariables.length; i++)
                {
                    TypeVariable typeVariable = typeVariables[i];
                    if (fc.equals(typeVariable))
                    {
                        Type t = parameterizedType.getActualTypeArguments()[i];
                        if (t instanceof Class)
                        {
                            type = (Class) t;
                        }
                        break;
                    }
                }
            }
            return type;
        }

        /**
         * 得到指定field对应类的{@linkplain AutoSetDefaultDealt}注解。
         *
         * @param field        声明的变量
         * @param currentClass 实际的子类
         * @return
         */
        public static AutoSetDefaultDealt getAutoSetDefaultDealt(Field field, Class<?> currentClass)
        {
            Class type = getFieldRealType(field, currentClass);
            return getAutoSetDefaultDealt(type);
        }

        /**
         * 得到指定类的{@linkplain AutoSetDefaultDealt}注解。
         *
         * @param clazz
         * @return
         */
        public static AutoSetDefaultDealt getAutoSetDefaultDealt(Class<?> clazz)
        {
            AutoSetDefaultDealt autoSetDefaultDealt = AnnoUtil.Advanced.getAnnotation(clazz, AutoSetDefaultDealt.class);
            if (autoSetDefaultDealt == null)
            {
                for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
                {
                    IDynamicAnnotationImprovable.Result<InvocationHandler, AutoSetDefaultDealt> result =
                            iDynamicAnnotationImprovable.getAutoSetDefaultDealt(clazz);
                    if (result != null)
                    {
                        autoSetDefaultDealt = newProxyAnnotation(result, result.t);
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", AutoSetDefaultDealt.class.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                        break;
                    }
                }
            }
            return autoSetDefaultDealt;
        }

        /**
         * 得到指定注解上的{@linkplain AspectOperationOfPortIn}注解。
         *
         * @param annotation
         * @return
         */
        public static AspectOperationOfPortIn getAspectOperationOfPortIn(Annotation annotation)
        {
            Class<? extends Annotation> atype = annotation.annotationType();
            AspectOperationOfPortIn aspectOperationOfPortIn = AnnoUtil
                    .getAnnotation(atype, AspectOperationOfPortIn.class);
            if (aspectOperationOfPortIn == null)
            {
                for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
                {
                    IDynamicAnnotationImprovable.Result<InvocationHandler, AspectOperationOfPortIn> result =
                            iDynamicAnnotationImprovable
                                    .getAspectOperationOfPortIn(annotation);
                    if (result != null)
                    {
                        aspectOperationOfPortIn = newProxyAnnotation(result, result.t);
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", AspectOperationOfPortIn.class.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                        break;
                    }
                }
            }
            return aspectOperationOfPortIn;
        }

        /**
         * 得到指定的注解上的{@linkplain AspectOperationOfNormal}注解
         *
         * @param annotation
         * @return
         */
        public static AspectOperationOfNormal getAspectOperationOfNormal(Annotation annotation)
        {
            Class<? extends Annotation> atype = annotation.annotationType();
            AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil
                    .getAnnotation(atype, AspectOperationOfNormal.class);
            if (aspectOperationOfNormal == null)
            {
                for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
                {
                    IDynamicAnnotationImprovable.Result<InvocationHandler, AspectOperationOfNormal> result =
                            iDynamicAnnotationImprovable
                                    .getAspectOperationOfNormal(annotation);
                    if (result != null)
                    {
                        aspectOperationOfNormal = newProxyAnnotation(result, result.t);
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", AspectOperationOfNormal.class.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                        break;
                    }
                }
            }
            return aspectOperationOfNormal;
        }

        public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationType)
        {
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler, A> result =
                        iDynamicAnnotationImprovable.getAnnotation(clazz, annotationType);
                if (result != null)
                {
                    annotationResult = newProxyAnnotation(result, result.t);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                iDynamicAnnotationImprovable);
                    }
                    break;
                }
            }
            if (annotationResult == null)
            {
                annotationResult = AnnoUtil.getAnnotation(clazz, annotationType);
            }
            annotationResult = proxyAnnotationForAttr(annotationResult);
            return (A) annotationResult;
        }

        public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType)
        {
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler, A> result =
                        iDynamicAnnotationImprovable.getAnnotation(method, annotationType);
                if (result != null)
                {
                    annotationResult = newProxyAnnotation(result, result.t);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                iDynamicAnnotationImprovable);
                    }
                    break;
                }
            }
            if (annotationResult == null)
            {
                annotationResult = AnnoUtil.getAnnotation(method, annotationType);
            }
            annotationResult = proxyAnnotationForAttr(annotationResult);
            return (A) annotationResult;
        }

        public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationType)
        {
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler, A> result =
                        iDynamicAnnotationImprovable.getAnnotation(field, annotationType);
                if (result != null)
                {
                    annotationResult = newProxyAnnotation(result, result.t);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                iDynamicAnnotationImprovable);
                    }
                    break;
                }
            }
            if (annotationResult == null)
            {
                annotationResult = AnnoUtil.getAnnotation(field, annotationType);
            }
            annotationResult = proxyAnnotationForAttr(annotationResult);
            return (A) annotationResult;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz,
                Class<A> annotationType)
        {
            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler[], A> result =
                        iDynamicAnnotationImprovable.getRepeatableAnnotations(clazz, annotationType);
                if (result != null)
                {
                    InvocationHandler[] handlers = result.t;
                    annos = (A[]) Array.newInstance(result.appendAnnotation, handlers.length);
                    for (int i = 0; i < handlers.length; i++)
                    {
                        A a = newProxyAnnotation(result, handlers[i]);
                        annos[i] = a;
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                    }
                    break;
                }
            }
            if (annos == null)
            {
                annos = AnnoUtil.getRepeatableAnnotations(clazz, annotationType);
            }
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Method method,
                Class<A> annotationType)
        {
            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler[], A> result =
                        iDynamicAnnotationImprovable.getRepeatableAnnotations(method, annotationType);
                if (result != null)
                {
                    InvocationHandler[] handlers = result.t;
                    annos = (A[]) Array.newInstance(result.appendAnnotation, handlers.length);
                    for (int i = 0; i < handlers.length; i++)
                    {
                        A a = newProxyAnnotation(result, handlers[i]);
                        annos[i] = a;
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                    }
                    break;
                }
            }
            if (annos == null)
            {
                annos = AnnoUtil.getRepeatableAnnotations(method, annotationType);
            }
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Field field,
                Class<A> annotationType)
        {
            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler[], A> result =
                        iDynamicAnnotationImprovable.getRepeatableAnnotations(field, annotationType);
                if (result != null)
                {
                    InvocationHandler[] handlers = result.t;
                    annos = (A[]) Array.newInstance(result.appendAnnotation, handlers.length);
                    for (int i = 0; i < handlers.length; i++)
                    {
                        A a = newProxyAnnotation(result, handlers[i]);
                        annos[i] = a;
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("get @{} from {}", annotationType.getSimpleName(),
                                    iDynamicAnnotationImprovable);
                        }
                    }
                    break;
                }
            }
            if (annos == null)
            {
                annos = AnnoUtil.Advanced.getRepeatableAnnotations(field, annotationType);
            }
            return annos;
        }
    }

    /**
     * 设置或者清除默认的.
     *
     * @param config
     * @param iAnnotationConfigable 为null时表示清除默认的。
     */
    public static synchronized void setDefaultConfigable(IConfigData config,
            IAnnotationConfigable iAnnotationConfigable)
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
    public static synchronized void pushAnnotationConfigable(IConfigData config,
            IAnnotationConfigable iAnnotationConfigable)
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


    private static <A extends Annotation> A proxyAnnotationForAttr(A t)
    {
        if (t instanceof AnnoUtilDynamicAttrHandler._Dynamic_Annotation_Str_Attrs_)
        {
            LOGGER.debug("already proxyed:{}->{}", t.annotationType(), t);
            return t;
        } else if (t == null)
        {
            return null;
        }
        Stack<Configable> stack = threadLocal.get();
        Configable configable = stack == null || stack.isEmpty() ? null : stack.peek();
        if (configable == null)
        {
            synchronized (AnnoUtil.class)
            {
                configable = defaultConfigable;
            }
        }
        if (configable != null)
        {
            AnnoUtilDynamicAttrHandler handler = new AnnoUtilDynamicAttrHandler(t, configable.iAnnotationConfigable,
                    configable.config);
            Object obj = Proxy.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
                    t.annotationType(),
                    AnnoUtilDynamicAttrHandler._Dynamic_Annotation_Str_Attrs_.class
            }, handler);
            t = (A) obj;
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

    public static String tied(_Unece unNece, Field field, boolean enableDefaultValue)
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
        return proxyAnnotationForAttr(t);
    }


    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass)
    {
        A t = clazz.getAnnotation(annotationClass);
        return proxyAnnotationForAttr(t);
    }

    private static <A extends Annotation> A[] getAnnotationsOfProxy(Object obj, Class<A> annotationClass)
    {
        A[] as = getAnnotationsByType(obj, annotationClass);
        for (int i = 0; i < as.length; i++)
        {
            as[i] = proxyAnnotationForAttr(as[i]);
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

    public static <A extends Annotation> A[] getAnnotations(Method method)
    {
        return (A[]) method.getAnnotations();
    }

    public static <A extends Annotation> A[] getAnnotations(Class clazz)
    {
        return (A[]) clazz.getAnnotations();
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
        return proxyAnnotationForAttr(t);
    }


    public static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationClass)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().equals(annotationClass))
            {
                return proxyAnnotationForAttr((A) annotation);
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
            A t = proxyAnnotationForAttr(c.getDeclaredAnnotation(annotationClass));
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
