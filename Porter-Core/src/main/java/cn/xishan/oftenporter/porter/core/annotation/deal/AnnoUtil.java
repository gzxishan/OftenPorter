package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.ResourceUtil;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
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
        WeakReference<Object> targetRef;
        Object annotationType;
        int hashCode;

        public CacheKey(Object target, Object[] array, String tag)
        {
            hashCode = target.hashCode();
            this.targetRef = new WeakReference<>(target);
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
            this.targetRef = new WeakReference<>(target);
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CacheKey)
            {
                CacheKey cacheKey = (CacheKey) obj;
                Object target = targetRef.get();
                return target != null && target.equals(cacheKey.targetRef.get()) && annotationType
                        .equals(cacheKey.annotationType);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        @Override
        public String toString()
        {
            return targetRef.get() + ":" + annotationType;
        }

        Object getCache()
        {
            WeakReference weakReference = annotationCache.get(this);
            Object cache = weakReference == null ? null : weakReference.get();
//            if (LOGGER.isDebugEnabled())
//            {
//                if (cache != null)
//                {
//                    LOGGER.debug("hit cache:key=[{}],cache value=[{}]", this, cache == NULL ? "null" : cache);
//                }
//            }
            putCacheLog();
            return cache;
        }

        void setCache(Object obj)
        {
//            if (LOGGER.isDebugEnabled())
//            {
//                LOGGER.debug("set cache:key=[{}],cache value=[{}]", this, obj == null ? "null" : obj);
//            }
            annotationCache.put(this, new WeakReference<>(obj == null ? NULL : obj));
        }
    }

    private static final String[] EXCEPT_CLASS_NAMES = {CacheKey.class.getName(), AnnoUtil.class.getName(),
            Advanced.class.getName()};
    private static Map<String, Integer> cacheCount;
    private static IDynamicAnnotationImprovable[] DYNAMIC_ANNOTATION_IMPROVABLES;
    private static final String DYNAMIC_ANNOTATION_IMPROVABLES_STRING;
    private static Map<CacheKey, WeakReference<Object>> annotationCache;

    private static String findMaxValueKey(Map<String, Integer> map)
    {
        String key = null;
        int max = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            if (entry.getValue() > max)
            {
                key = entry.getKey();
                max = entry.getValue();
            }
        }
        return key;
    }

    private static void putCacheLog()
    {
        String from = null;
        if (LOGGER.isInfoEnabled() && (from = LogUtil.getCodePosExceptNames(EXCEPT_CLASS_NAMES)) != null)
        {
            // LOGGER.debug("invoke from:\n\t\t{}", from);
            Integer count = cacheCount.get(from);
            if (count == null)
            {
                count = 0;
            }
            cacheCount.put(from, count + 1);
        }
    }

    public static void clearCache()
    {
        if (LOGGER.isInfoEnabled())
        {
            List<String> strs = new ArrayList<>(cacheCount.size());
            while (true)
            {
                String key = findMaxValueKey(cacheCount);
                if (key == null)
                {
                    break;
                } else
                {
                    int count = cacheCount.remove(key);
                    strs.add(count + ":" + key);
                }
            }
            LOGGER.info("invoke times:\n\t\t{}", WPTool.join("\n\t\t", strs));
        }
        annotationCache.clear();
        cacheCount.clear();
        initCache();
    }

    private static void initCache()
    {
        annotationCache = new ConcurrentHashMap<>();
        cacheCount = new HashMap<>();
    }

    static
    {
        initCache();
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
                        IDynamicAnnotationImprovable iDynamicAnnotationImprovable = new DynamicAnnotationImprovableWrap(
                                className);
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

//    public static IDynamicAnnotationImprovable[] getDynamicAnnotationImprovables()
//    {
//        return DYNAMIC_ANNOTATION_IMPROVABLES;
//    }
//
//    public static void setIDynamicAnnotationImprovables(IDynamicAnnotationImprovable[] dynamicAnnotationImprovables)
//    {
//        DYNAMIC_ANNOTATION_IMPROVABLES = dynamicAnnotationImprovables;
//    }

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

        public static void setUseWhiteOrDeny(boolean useWhiteOrDeny)
        {
            DynamicAnnotationImprovableWrap.setUseWhiteOrDeny(useWhiteOrDeny);
        }

        public static void addDeny(String className)
        {
            DynamicAnnotationImprovableWrap.addDeny(className);
        }

        public static void removeDeny(String className)
        {
            DynamicAnnotationImprovableWrap.removeDeny(className);
        }

        public static void addWhite(String className)
        {
            DynamicAnnotationImprovableWrap.addWhite(className);
        }

        public static void removeWhite(String className)
        {
            DynamicAnnotationImprovableWrap.removeWhite(className);
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
            Object obj = ProxyUtil.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
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
            realClass = ProxyUtil.unwrapProxyForGeneric(realClass);
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
            CacheKey cacheKey = new CacheKey(realClass, new Object[]{realClass}, "getDirectGenericRealTypeAt-" + index);
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
         * 获取父类中被继承或重载的函数。
         *
         * @param method
         * @return
         */
        public static Method getSuperMethod(Method method)
        {
            return getSuperMethod(method.getDeclaringClass(), method, method.getDeclaringClass().getSuperclass(), true);
        }

        /**
         * 获取父类中被继承或重载的函数。
         *
         * @param method
         * @return
         */
        public static Method getSuperMethod(Class childClass, Method method, Class superClass, boolean recursive)
        {
            if (superClass == null)
            {
                return null;
            }
            int n = method.getParameterCount();
            List<Class> parameters = new ArrayList<>(n);
            for (int i = 0; i < n; i++)
            {
                parameters.add(getRealTypeOfMethodParameter(childClass, method, i));
            }

            while (superClass != null)
            {

                Method[] superMethods = superClass.getMethods();
                for (Method m : superMethods)
                {
                    if (!m.getName().equals(method.getName()))
                    {
                        continue;
                    }
                    if (!(Modifier.isStatic(m.getModifiers()) && Modifier.isStatic(method.getModifiers()) ||
                            !Modifier.isStatic(m.getModifiers()) && !Modifier.isStatic(method.getModifiers())))
                    {
                        continue;
                    }

                    if (!(m.getReturnType() == Void.TYPE && method.getReturnType() == Void.TYPE ||
                            m.getReturnType() != Void.TYPE && method.getReturnType() != Void.TYPE))
                    {
                        continue;
                    }
                    if (m.getParameterCount() != n)
                    {
                        continue;
                    }
                    if (WPTool.getAccessType(m) > WPTool.getAccessType(method))
                    {
                        continue;
                    }

                    boolean is = true;
                    for (int i = 0; i < n; i++)
                    {
                        Class type = getRealTypeOfMethodParameter(childClass, m, i);
                        if (!type.equals(parameters.get(i)))
                        {
                            is = false;
                            break;
                        }
                    }
                    if (is)
                    {
                        return m;
                    }
                }
                if (!recursive)
                {
                    break;
                }
                superClass = superClass.getSuperclass();
            }

            return null;
        }

        /**
         * 可获取泛型字段的实际类型
         *
         * @param realClass 实际子类
         * @param field     声明变量
         * @return
         */
        public static Class<?> getRealTypeOfField(Class<?> realClass, Field field)
        {

            CacheKey cacheKey = new CacheKey(realClass, new Object[]{field}, "getRealTypeOfField");
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
            Class type = getRealType(field.getDeclaringClass(), realClass, field.getGenericType());
            if (type == null)
            {
                type = field.getType();
            }
            cacheKey.setCache(type);
            return type;
        }

        /**
         * 可获取函数泛型返回值的实际类型
         *
         * @param realClass 实际子类
         * @param method    函数
         * @return
         */
        public static Class<?> getRealTypeOfMethodReturn(Class<?> realClass, Method method)
        {

            CacheKey cacheKey = new CacheKey(realClass, new Object[]{method}, "getRealTypeOfMethodReturn");
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
            Class type = getRealType(method.getDeclaringClass(), realClass, method.getGenericReturnType());
            if (type == null)
            {
                type = method.getReturnType();
            }
            cacheKey.setCache(type);
            return type;
        }

        /**
         * 可获取函数泛型参数的实际类型
         *
         * @param realClass 实际子类
         * @param method    函数
         * @param argIndex  参数索引
         * @return
         */
        public static Class<?> getRealTypeOfMethodParameter(Class<?> realClass, Method method, int argIndex)
        {

            CacheKey cacheKey = new CacheKey(realClass, new Object[]{method},
                    "getRealTypeOfMethodParameter-" + argIndex);
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
            Class type = getRealType(method.getDeclaringClass(), realClass,
                    method.getGenericParameterTypes()[argIndex]);
            if (type == null)
            {
                type = method.getParameterTypes()[argIndex];
            }
            cacheKey.setCache(type);
            return type;
        }

        private static Class<?> getRealType(Class<?> declaringClass, Class<?> realClass, Type genericType)
        {
            if (genericType instanceof Class)
            {
                return (Class<?>) genericType;
            }
            realClass = ProxyUtil.unwrapProxyForGeneric(realClass);

            Type realType = null;
            if (Modifier.isInterface(declaringClass.getModifiers()) && genericType instanceof TypeVariable)
            {
                TypeVariable typeVariable = (TypeVariable) genericType;
                GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
                if (genericDeclaration instanceof Class)
                {
                    Type[] gis = realClass.getGenericInterfaces();
                    for (Type type : gis)
                    {
                        if (type instanceof ParameterizedType)
                        {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            Type rawType = parameterizedType.getRawType();
                            if (genericDeclaration.equals(rawType))
                            {
                                realType = parameterizedType;
                                break;
                            }
                        }
                    }
                }
            } else
            {
                realType = realClass.getGenericSuperclass();
            }
            Class<?> rt = null;
            if (genericType != null && realType instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) realType;

                TypeVariable[] typeVariables = declaringClass.getTypeParameters();
                for (int i = 0; i < typeVariables.length; i++)
                {
                    TypeVariable typeVariable = typeVariables[i];
                    if (genericType.equals(typeVariable))
                    {
                        Type t = parameterizedType.getActualTypeArguments()[i];
                        if (t instanceof Class)
                        {
                            rt = (Class) t;
                        }
                        break;
                    }
                }
            }
            return rt;
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
            Class type = getRealTypeOfField(currentClass, field);
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
            if (autoSetDefaultDealt == null)
            {
                Annotation[] annotations = getAnnotations(clazz);
                for (Annotation annotation : annotations)
                {
                    AutoSetDefaultDealt _autoSetDefaultDealt = getAnnotation(annotation.annotationType(),
                            AutoSetDefaultDealt.class);
                    if (_autoSetDefaultDealt != null)
                    {
                        if (autoSetDefaultDealt != null && LOGGER.isDebugEnabled())
                        {
                            LOGGER.warn("ignore {} of {} by {}", AutoSetDefaultDealt.class.getSimpleName(),
                                    autoSetDefaultDealt.annotationType(), _autoSetDefaultDealt.annotationType());
                        }
                        autoSetDefaultDealt = _autoSetDefaultDealt;
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
                            iDynamicAnnotationImprovable.getAspectOperationOfPortIn(annotation);
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

        public static Annotation[] getAnnotationsForAspectOperationOfPortIn(Porter porter)
        {
            Worked worked = hasWorked(porter);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(porter, "");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (Annotation[]) cache;
                }
            }
            Annotation[] annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                annotationResult = iDynamicAnnotationImprovable.getAnnotationsForAspectOperationOfPortIn(porter);
                if (annotationResult != null)
                {
                    break;
                }
            }
            if (annotationResult == null)
            {
                annotationResult = porter.getClazz().getDeclaredAnnotations();
            }
            if (annotationResult != null)
            {
                for (int i = 0; i < annotationResult.length; i++)
                {
                    Annotation annotation = annotationResult[i];
                    annotationResult[i] = doProxyForDynamicAttr(annotation);
                }
            }
            cacheKey.setCache(annotationResult);
            worked.reset();
            return annotationResult;
        }

        public static Annotation[] getAnnotationsForAspectOperationOfPortIn(PorterOfFun porterOfFun)
        {
            Worked worked = hasWorked(porterOfFun);
            if (worked.isWorked)
            {
                return null;
            }
            CacheKey cacheKey = new CacheKey(porterOfFun, "");
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (Annotation[]) cache;
                }
            }
            Annotation[] annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : DYNAMIC_ANNOTATION_IMPROVABLES)
            {
                annotationResult = iDynamicAnnotationImprovable.getAnnotationsForAspectOperationOfPortIn(porterOfFun);
                if (annotationResult != null)
                {
                    break;
                }
            }
            if (annotationResult == null)
            {
                annotationResult = porterOfFun.getMethod().getDeclaredAnnotations();
            }
            if (annotationResult != null)
            {
                for (int i = 0; i < annotationResult.length; i++)
                {
                    Annotation annotation = annotationResult[i];
                    annotationResult[i] = doProxyForDynamicAttr(annotation);
                }
            }
            cacheKey.setCache(annotationResult);
            worked.reset();
            return annotationResult;
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

    private static <A extends Annotation> A[] proxyAnnotationForAttr(A[] as)
    {
        for (int i = 0; i < as.length; i++)
        {
            as[i] = proxyAnnotationForAttr(as[i]);
        }
        return as;
    }

    private static <A extends Annotation> A proxyAnnotationForAttr(A t)
    {
        if (t instanceof AnnoUtilDynamicAttrHandler._Dynamic_Annotation_Str_Attrs_)
        {
            LOGGER.debug("already proxyed:{}->{}", t.annotationType(), t);
            return t;
        } else if (t == null)
        {
            return t;
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
            CacheKey cacheKey = new CacheKey(t.annotationType(), new Object[0], "proxyAnnotationForAttr");
            Object cache = cacheKey.getCache();
            int existsStringMethod = 0;
            if (cache != null && cache != NULL)
            {
                existsStringMethod = (int) cache;
            }

            if (existsStringMethod == -1)
            {
                return t;
            } else
            {
                Method[] methods = WPTool.getAllMethods(t.annotationType());
                boolean has = false;
                for (Method method : methods)
                {
                    if (String.class.equals(method.getReturnType()) || String[].class.equals(method.getReturnType()))
                    {
                        has = true;
                        break;
                    }
                }
                existsStringMethod = has ? 1 : -1;
            }
            cacheKey.setCache(existsStringMethod);
            if (existsStringMethod == -1)
            {
                return t;
            }
            //只代理含有String或String[]的注解。
            AnnoUtilDynamicAttrHandler handler = new AnnoUtilDynamicAttrHandler(t, configable.iAnnotationConfigable,
                    configable.config);
            Object obj = ProxyUtil.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
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
        return methods(classMethod, funPorterIn.method(), funPorterIn.methods());
    }

    public static PortMethod[] methods(PortMethod classMethod, PortMethod funMethod, PortMethod[] funMethods)
    {
        PortMethod[] portMethods = funMethods;
        if (portMethods.length == 0)
        {
            portMethods = new PortMethod[]{funMethod};
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
     * 获取注解,支持继承性注解(支持接口函数、但不支持泛型接口函数)。
     *
     * @param method
     * @param annotationClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass)
    {
        return _getAnnotation(method, annotationClass, true);
    }


    private static <A extends Annotation> A getAnnotationForFromInterface(Method method,
            Class<A> annotationClass)
    {
        Class<?> clazz = method.getDeclaringClass();
        Class[] is = clazz.getInterfaces();
        for (Class c : is)
        {
            Method m = null;
            try
            {
                m = c.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e)
            {

            }
            if (m != null)
            {
                return m.getAnnotation(annotationClass);
            }
        }
        return null;
    }


    private static <A extends Annotation> A _getAnnotation(Method method, Class<A> annotationClass,
            boolean willProxy)
    {
        CacheKey cacheKey = new CacheKey(method, new Object[]{annotationClass},
                "_getAnnotation-willProxy:" + willProxy);
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
        boolean isInherited = annotationClass.isAnnotationPresent(Inherited.class);
        A t = getAnnotation(method.getDeclaringClass(), method, annotationClass, isInherited);
        if (t == null && isInherited && Modifier.isInterface(method.getDeclaringClass().getModifiers()))
        {
            t = getAnnotationForFromInterface(method, annotationClass);
        }
        t = willProxy ? proxyAnnotationForAttr(t) : t;
        cacheKey.setCache(t);
        return t;
    }

    private static <A extends Annotation> A getAnnotation(Class childClass, Method method,
            Class<A> annotationClass,
            boolean seekSuper)
    {
        A t = method.getAnnotation(annotationClass);
        if (t == null && seekSuper)
        {

            Class<?> superClass = childClass.getSuperclass();
            while (superClass != null)
            {
                Method superMethod = Advanced.getSuperMethod(childClass, method, superClass, false);
                if (superMethod != null)
                {
                    t = superMethod.getAnnotation(annotationClass);
                    if (t != null)
                    {
                        break;
                    }
                }
                superClass = superClass.getSuperclass();
            }
        }
        return t;
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
                t = getAnnotation(inClass, annotationClass);//递归调用
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
        return proxyAnnotationForAttr(as);
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
                    } else if (Modifier.isInterface(((Class) obj).getModifiers()))
                    {
                        Class[] interfaces = ((Class) obj).getInterfaces();
                        for (Class c : interfaces)
                        {
                            as = getAnnotationsByType(c, annotationClass, seekSuper);
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
                    A a = null;
                    if (obj instanceof Class)
                    {
                        a = getAnnotation((Class) obj, annotationClass);
                    } else if (obj instanceof Method)
                    {
                        a = getAnnotation((Method) obj, annotationClass);
                    } else
                    {//Field
                        a = getAnnotation((Field) obj, annotationClass);
                    }
                    if (a == null)
                    {
                        as = (A[]) Array.newInstance(annotationClass, 0);
                    } else
                    {
                        as = (A[]) Array.newInstance(annotationClass, 1);
                        as[0] = a;
                    }
                }
            }
            return as;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
            return (A[]) Array.newInstance(annotationClass, 0);
        }
    }

    /**
     * 获取函数的所有注解，<strong>包括</strong>继承性注解(只支持类的重载或继承函数)。
     *
     * @param method
     * @return
     */
    public static Annotation[] getAnnotations(Method method)
    {
        Set<Class> typeSet = new HashSet<>();
        Annotation[] as = method.getAnnotations();
        List<Annotation> list = new ArrayList<>();
        WPTool.addAll(list, as);
        for (Annotation annotation : as)
        {
            typeSet.add(annotation.annotationType());
        }


        Class childClass = method.getDeclaringClass();
        Class<?> superClass = childClass.getSuperclass();
        while (superClass != null)
        {
            Method superMethod = Advanced.getSuperMethod(childClass, method, superClass, false);
            if (superMethod != null)
            {
                as = superMethod.getAnnotations();
                for (Annotation annotation : as)
                {
                    if (!typeSet.contains(annotation.annotationType()) && annotation.annotationType()
                            .isAnnotationPresent(Inherited.class))
                    {
                        list.add(annotation);
                        typeSet.add(annotation.annotationType());
                    }
                }
            }
            superClass = superClass.getSuperclass();
        }

        as = list.toArray(new Annotation[0]);
        return proxyAnnotationForAttr(as);
    }

    /**
     * 获取类的所有注解，<strong>包括</strong>继承性注解（不支持接口上的）。
     *
     * @param clazz
     * @return
     */
    public static Annotation[] getAnnotations(Class clazz)
    {
        List<Annotation> list = new ArrayList<>();
        getAnnotations(clazz, list, new HashSet<>());
        Annotation[] as = list.toArray(new Annotation[0]);
        return proxyAnnotationForAttr(as);
    }

    private static void getAnnotations(Class clazz, List<Annotation> list, Set<Class> typeSet)
    {
        if (clazz == null)
        {
            return;
        }
        Annotation[] as = clazz.getAnnotations();
        for (Annotation annotation : as)
        {
            if (!typeSet.contains(annotation.annotationType()) && annotation.annotationType()
                    .isAnnotationPresent(Inherited.class))
            {
                list.add(annotation);
                typeSet.add(annotation.annotationType());
            }
        }
        getAnnotations(clazz.getSuperclass(), list, typeSet);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(clazz, annotationClass);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Field field, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(field, annotationClass);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Method method, Class<A> annotationClass)
    {
        return getAnnotationsOfProxy(method, annotationClass);
    }


    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass)
    {
        A t = field.getAnnotation(annotationClass);
        return proxyAnnotationForAttr(t);
    }

    public static <A extends Annotation> A getAnnotation(Parameter parameter, Class<A> annotationClass)
    {
        A t = parameter.getAnnotation(annotationClass);
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
     * 获取类及其所有父类上的所有指定注解。
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
                if (getAnnotation(clazz, c) != null)
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

    public static boolean isOneOfAnnotationsPresent(Class<?> clazz, Method method, Class<?>... annotationClasses)
    {
        return isOneOfAnnotationsPresent(clazz, annotationClasses) || isOneOfAnnotationsPresent(method,
                annotationClasses);
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
                Annotation t = _getAnnotation(method, c, false);
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
