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
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Object NULL = new Object();

    //防止死循环
    private static final ThreadLocal<Set<String>> threadLocalForLoop = ThreadLocal.withInitial(
            () -> {
                Set<String> set = ConcurrentHashMap.newKeySet();
                return set;
            });

    private static class Worked
    {
        boolean isWorked;
        String key;

        public Worked(boolean isWorked, String key)
        {
            this.isWorked = isWorked;
            this.key = key;
        }

        void reset()
        {
            threadLocalForLoop.get().remove(key);
        }
    }


    private static class CacheKey
    {
        Object target;
        Object annotationType;

        public CacheKey(Object target, Object[] array, String tag)
        {
            this.target = target;
            StringBuilder builder = new StringBuilder();
            for (Object obj : array)
            {
                builder.append(obj).append("---");
            }
            builder.append(tag);
            this.annotationType = builder.toString();
        }

        public CacheKey(Object target, Object annotationType)
        {
            this.target = target;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CacheKey)
            {
                CacheKey cacheKey = (CacheKey) obj;
                return target.equals(cacheKey.target) && annotationType.equals(cacheKey.annotationType);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return target.hashCode();
        }

        @Override
        public String toString()
        {
            return target + ":" + annotationType;
        }

        Object getCache()
        {
            WeakReference weakReference = annotationCache.get(this);
            Object cache = weakReference == null ? null : weakReference.get();
            if (cache != null && LOGGER.isDebugEnabled())
            {
                LOGGER.debug("hit cache:key=[{}],cache value=[{}]", this, cache == NULL ? "null" : cache);
            }
            return cache;
        }

        void setCache(Object obj)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("set cache:key=[{}],cache value=[{}]", this, obj == null ? "null" : obj);
            }
            annotationCache.put(this, new WeakReference<>(obj == null ? NULL : obj));
        }
    }

    private static IDynamicAnnotationImprovable[] DYNAMIC_ANNOTATION_IMPROVABLES;
    private static final String DYNAMIC_ANNOTATION_IMPROVABLES_STRING;
    private static Map<CacheKey, WeakReference<Object>> annotationCache;

    static
    {
        annotationCache = new ConcurrentHashMap<>();
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
        DYNAMIC_ANNOTATION_IMPROVABLES_STRING = String.valueOf(DYNAMIC_ANNOTATION_IMPROVABLES);
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
         * 防止死循环
         *
         * @param objects
         * @return
         */
        static Worked hasWorked(Object... objects)
        {
            StringBuilder builder = new StringBuilder();
            for (Object obj : objects)
            {
                builder.append(obj).append("___");
            }
            builder.append(DYNAMIC_ANNOTATION_IMPROVABLES_STRING);

            String key = builder.toString();
            boolean rs = threadLocalForLoop.get().contains(key);
            return new Worked(rs, key);
        }

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

        private static void getAllTypesRecursiveForClass(List<Class<?>> typeList, boolean isLastGeneric, Type... types)
        {
            for (Type type : types)
            {
                if (type instanceof Class)
                {
                    if (isLastGeneric)
                    {
                        typeList.add((Class) type);
                    }
                } else if (type instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    if (isLastGeneric)
                    {
                        Type rawType = parameterizedType.getRawType();
                        if (rawType instanceof Class)
                        {
                            typeList.add((Class) rawType);
                        }
                    }
                    getAllTypesRecursiveForClass(typeList, true, parameterizedType.getActualTypeArguments());
                }
            }
        }

        private static List<Class<?>> getAllGenericRealClassType(Class<?> realClass)
        {
            List<Class<?>> typeList = new ArrayList<>(4);
            Type superType = realClass.getGenericSuperclass();
            if (superType != null)
            {
                getAllTypesRecursiveForClass(typeList, false, superType);
            }
            getAllTypesRecursiveForClass(typeList, false, realClass.getGenericInterfaces());
            return typeList;
        }

        /**
         * 从泛型的实际类型中获取是superClassOrInterface子类或接口实现者的类，见{@linkplain #getDirectGenericRealTypeAt(Class, int)}
         *
         * @param realClass
         * @param superClassOrInterface
         * @return
         */
        public static
        @MayNull
        Class<?> getDirectGenericRealTypeBySuperType(Class<?> realClass, Class<?> superClassOrInterface)
        {
            if (superClassOrInterface.equals(realClass))
            {
                return realClass;
            }
            CacheKey cacheKey = new CacheKey(realClass, new Object[]{realClass, superClassOrInterface},
                    "getDirectGenericRealTypeByAssignable");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (Class<?>) cache;
                }
            }

            List<Class<?>> typeList = getAllGenericRealClassType(realClass);

            Class<?> type = null;
            for (Class<?> clazz : typeList)
            {
                if (WPTool.isAssignable(clazz, superClassOrInterface))
                {
                    if (type != null)
                    {
                        throw new RuntimeException("too many child found:" + type + "," + clazz);
                    }
                    type = clazz;
                }
            }
            cacheKey.setCache(type);
            return type;
        }

        /**
         * 获取指定类上的直接泛型类型（必须是Class,且必须是包含在"&lt;&gt;"中的）.
         * <ol>
         * <li>
         * class ClassA extends A&lt; ClassB &gt;<strong> : </strong>getDirectGenericRealTypeAt(ClassA.class,1)
         * 返回ClassB.class
         * </li>
         * <li>
         * class ClassAA extends AA&lt; ClassB,ClassC&lt;ClassD&gt; &gt;<strong> :
         * </strong>getDirectGenericRealTypeAt(ClassAA
         * .class,2)
         * 返回ClassC.class,getDirectGenericRealTypeAt(ClassAA.class,3) 返回ClassD.class
         * </li>
         * <li>
         * class ClassE extend A&lt; ClassB &gt; implements IC&lt;ClassD&gt;<strong> :
         * </strong>getDirectGenericRealTypeAt
         * (ClassE
         * .class,1)
         * 返回ClassB.class,getDirectGenericRealTypeAt(ClassE.class,2) 返回ClassD.class
         * </li>
         * </ol>
         *
         * @param realClass
         * @param index     0返回realClass，其他值返回对应的类
         * @return
         */
        public static
        @NotNull
        Class<?> getDirectGenericRealTypeAt(Class<?> realClass, int index)
        {
            if (index < 0)
            {
                throw new IllegalArgumentException("negative index:" + index);
            }
            if (index == 0)
            {
                return realClass;
            }
            CacheKey cacheKey = new CacheKey(realClass, new Object[]{realClass, index}, "getDirectGenericRealTypeAt");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (Class<?>) cache;
                }
            }

            List<Class<?>> typeList = getAllGenericRealClassType(realClass);

            if (index > typeList.size())
            {
                throw new RuntimeException("index out of bounds:" + index);
            } else
            {
                Class<?> type = typeList.get(index - 1);
                cacheKey.setCache(type);
                return type;
            }
        }

        /**
         * 获取泛型字段的实际类型
         *
         * @param realClass 实际子类
         * @param field     声明变量
         * @return
         */
        public static Class<?> getFieldRealType(Class<?> realClass, Field field)
        {

            CacheKey cacheKey = new CacheKey(realClass, new Object[]{field}, "getFieldRealType");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (Class<?>) cache;
                }
            }

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
            cacheKey.setCache(type);
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
            Class type = getFieldRealType(currentClass, field);
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
            Worked worked = hasWorked(AutoSetDefaultDealt.class, clazz);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(clazz, "AutoSetDefaultDealt");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (AutoSetDefaultDealt) cache;
                }
            }

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
            cacheKey.setCache(autoSetDefaultDealt);
            worked.reset();
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
            Worked worked = hasWorked(AspectOperationOfPortIn.class, annotation);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(annotation, "AspectOperationOfPortIn");

            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (AspectOperationOfPortIn) cache;
                }
            }

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
            cacheKey.setCache(aspectOperationOfPortIn);
            worked.reset();
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
            Worked worked = hasWorked(AspectOperationOfNormal.class, annotation);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(annotation, "AspectOperationOfNormal");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (AspectOperationOfNormal) cache;
                }
            }

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
            cacheKey.setCache(aspectOperationOfNormal);
            worked.reset();
            return aspectOperationOfNormal;
        }

        public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationType)
        {
            Worked worked = hasWorked(clazz, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(clazz, annotationType);

            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A) cache;
                }
            }

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
            cacheKey.setCache(annotationResult);
            worked.reset();
            return (A) annotationResult;
        }

        public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType)
        {
            Worked worked = hasWorked(method, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(method, annotationType);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A) cache;
                }
            }
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
            cacheKey.setCache(annotationResult);
            worked.reset();
            return (A) annotationResult;
        }

        public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationType)
        {
            Worked worked = hasWorked(field, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(field, annotationType);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A) cache;
                }
            }

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
            cacheKey.setCache(annotationResult);
            worked.reset();
            return (A) annotationResult;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz,
                Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", clazz, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(clazz, "getRepeatableAnnotations" + annotationType);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A[]) cache;
                }
            }
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
            cacheKey.setCache(annos);
            worked.reset();
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Method method,
                Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", method, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(method, "getRepeatableAnnotations" + annotationType);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A[]) cache;
                }
            }

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
            cacheKey.setCache(annos);
            worked.reset();
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Field field,
                Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", field, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(field, "getRepeatableAnnotations" + annotationType);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (A[]) cache;
                }
            }
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
            cacheKey.setCache(annos);
            worked.reset();
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
        if (t == null && Modifier.isInterface(clazz.getModifiers()) && annotationClass
                .isAnnotationPresent(Inherited.class))
        {
            Class<?>[] ins = clazz.getInterfaces();
            for (Class<?> inClass : ins)
            {
                t = getAnnotation(inClass, annotationClass);
                if (t != null)
                {
                    return t;//已经代理过
                }
            }
        }
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
        CacheKey cacheKey = new CacheKey(clazz, annotationClasses, "isAnnotationPresent-Class");
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (boolean) cache;
        }
        boolean rs;
        outer:
        do
        {
            for (Class c : annotationClasses)
            {
                if (clazz.isAnnotationPresent(c))
                {
                    if (!isAll)
                    {
                        rs = true;
                        break outer;
                    }
                } else if (isAll)
                {
                    rs = false;
                    break outer;
                }
            }
            rs = isAll;
        } while (false);
        cacheKey.setCache(rs);
        return rs;
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
        CacheKey cacheKey = new CacheKey(method, annotationClasses, "isAnnotationPresent-Method");
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (boolean) cache;
        }
        boolean rs;
        outer:
        do
        {
            for (Class c : annotationClasses)
            {
                Annotation t = getAnnotation(method, c, c.isAnnotationPresent(Inherited.class));
                if (t != null)
                {
                    if (!isAll)
                    {
                        rs = true;
                        break outer;
                    }
                } else if (isAll)
                {
                    rs = false;
                    break outer;
                }
            }
            rs = isAll;
        } while (false);
        cacheKey.setCache(rs);
        return rs;
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
