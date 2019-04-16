package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.ResourceUtil;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
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
 * @author Created by https://github.com/CLovinr on 2018-09-29.
 */
public class AnnoUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnoUtil.class);
    private static final AdvancedAnnotation DEFAULT_ADVANCED_ANNOTATION = AdvancedAnnotation.class.getAnnotation(
            AdvancedAnnotation.class);
    private static final ThreadLocal<Stack<Configable>> threadLocal = new ThreadLocal<>();
    private static Configable defaultConfigable;

    private static Map<CacheKey, WeakReference<Object>> annotationCache;
    private static Method javaGetAnnotations;
    private static final Object NULL = new Object();
    //防止死循环
    private static final ThreadLocal<Set<String>> threadLocalForLoop = ThreadLocal.withInitial(
            () -> {
                Set<String> set = ConcurrentHashMap.newKeySet();
                return set;
            });

    private static final Set<String> EXCEPT_CLASS_NAMES = OftenTool
            .addAll(new HashSet<>(), CacheKey.class.getName(), AnnoUtil.class.getName(),
                    Advance.class.getName(), NoCache.class.getName());
    private static Map<String, Integer> cacheCount;
    private static Map<String, IDynamicAnnotationImprovable[]> classDynamicArrayMap = Collections.emptyMap();
    private static final IDynamicAnnotationImprovable[] DYNAMIC_EMPTY = new IDynamicAnnotationImprovable[0];
    private static IDynamicAnnotationImprovable[] porterDynamicArray;
    private static IDynamicAnnotationImprovable[] aspectDynamicArray;

    private static final String DYNAMIC_ANNOTATION_IMPROVABLES_STRING;


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
            LOGGER.info("invoke count:\n\t\t{}", OftenTool.join("\n\t\t", strs));
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

        Map<String, List<DynamicAnnotationImprovableWrap>> classDynamicListMap = new HashMap<>();
        List<IDynamicAnnotationImprovable> porterDynamicList = new ArrayList<>();
        List<IDynamicAnnotationImprovable> aspectDynamicList = new ArrayList<>();
        List<DynamicAnnotationImprovableWrap> allDynamicList = new ArrayList<>();
        try
        {
            String path = "/OP-INF/cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable";
            List<String> dynamicNames = ResourceUtil.getAbsoluteResourcesString(path, "utf-8");

            for (String _classNames : dynamicNames)
            {
                if (OftenTool.isEmpty(_classNames))
                {
                    continue;
                }
                String[] classNames = OftenStrUtil.split(_classNames.trim(), "\n");
                for (String className : classNames)
                {
                    DynamicAnnotationImprovableWrap dynamic = new DynamicAnnotationImprovableWrap(
                            className);
                    Set<String> supports = dynamic.supportClassNames();
                    if (supports != null)
                    {
                        for (String support : supports)
                        {
                            support = support == null ? null : support.trim();
                            if (OftenTool.isEmpty(support))
                            {
                                continue;
                            }
                            if ("*".equals(support))
                            {
                                allDynamicList.add(dynamic);
                                continue;
                            }
                            List<DynamicAnnotationImprovableWrap> list = classDynamicListMap.get(support);
                            if (list == null)
                            {
                                list = new ArrayList<>();
                                classDynamicListMap.put(support, list);
                            }
                            list.add(dynamic);
                        }
                    }

                    if (dynamic.supportPorter())
                    {
                        porterDynamicList.add(dynamic);
                    }
                    if (dynamic.supportAspect())
                    {
                        aspectDynamicList.add(dynamic);
                    }
                }
            }
            classDynamicArrayMap = new HashMap<>();
            DynamicAnnotationImprovableWrap[] dynamics;
            for (Map.Entry<String, List<DynamicAnnotationImprovableWrap>> entry : classDynamicListMap.entrySet())
            {
                List<DynamicAnnotationImprovableWrap> list = entry.getValue();
                list.addAll(allDynamicList);
                dynamics = list.toArray(new DynamicAnnotationImprovableWrap[0]);
                Arrays.sort(dynamics);
                classDynamicArrayMap.put(entry.getKey(), dynamics);
            }
            dynamics = porterDynamicList.toArray(new DynamicAnnotationImprovableWrap[0]);
            Arrays.sort(dynamics);
            porterDynamicArray = dynamics;

            dynamics = aspectDynamicList.toArray(new DynamicAnnotationImprovableWrap[0]);
            Arrays.sort(dynamics);
            aspectDynamicArray = dynamics;
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        DYNAMIC_ANNOTATION_IMPROVABLES_STRING = AnnoUtil.class.getName() +
                "-classDynamics-" + classDynamicArrayMap.hashCode();
    }

    /**
     * 设置或者清除默认的.
     *
     * @param iAnnotationConfigable 为null时表示清除默认的。
     */
    public static synchronized void setDefaultConfigable(IAnnotationConfigable iAnnotationConfigable)
    {
        if (iAnnotationConfigable == null)
        {
            defaultConfigable = null;
        } else
        {
            Configable configable = new Configable(iAnnotationConfigable);
            defaultConfigable = configable;
        }
    }

    public static synchronized void popAnnotationConfigable()
    {
        Stack stack = threadLocal.get();
        if (stack != null && !stack.isEmpty())
        {
            stack.pop();
        }
    }

    /**
     * 见{@linkplain #popAnnotationConfigable()}
     *
     * @param iAnnotationConfigable
     */
    public static synchronized void pushAnnotationConfigable(IAnnotationConfigable iAnnotationConfigable)
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
        Configable configable = new Configable(iAnnotationConfigable);
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
            CacheKey cacheKey = new CacheKey(t.annotationType(), "proxyAnnotationForAttr");
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
                Method[] methods = OftenTool.getAllMethods(t.annotationType());
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
            AnnoUtilDynamicAttrHandler handler = new AnnoUtilDynamicAttrHandler(t, configable.iAnnotationConfigable);
            Object obj = ProxyUtil.newProxyInstance(InvocationHandlerWithCommon.getClassLoader(), new Class[]{
                    t.annotationType(), AnnoUtilDynamicAttrHandler._Dynamic_Annotation_Str_Attrs_.class
            }, handler);
            t = (A) obj;
        }
        return t;
    }


    public static boolean isAllOfAnnotationsPresent(Class<?> clazz, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(true, clazz, annotationClasses);
    }

    public static boolean isAllOfAnnotationsPresent(Method method, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(true, method, annotationClasses);
    }

    public static boolean isAllOfAnnotationsPresent(Field field, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(true, field, annotationClasses);
    }

    public static boolean isOneOfAnnotationsPresent(Class<?> clazz, Method method, Class<?>... annotationClasses)
    {
        return isOneOfAnnotationsPresent(clazz, annotationClasses) || isOneOfAnnotationsPresent(method,
                annotationClasses);
    }

    public static boolean isOneOfAnnotationsPresent(Class<?> clazz, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(false, clazz, annotationClasses);
    }


    public static boolean isOneOfAnnotationsPresent(Method method, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(false, method, annotationClasses);
    }

    public static boolean isOneOfAnnotationsPresent(Field field, Class<?>... annotationClasses)
    {
        return isAnnotationPresent(false, field, annotationClasses);
    }

    private static boolean isAnnotationPresent(boolean isAll, Class<?> clazz, Class<?>... annotationClasses)
    {
        CacheKey cacheKey = new CacheKey(clazz, "isAnnotationPresent-Class", annotationClasses);
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (boolean) cache;
        }
        boolean rs = NoCache.isAnnotationPresent(isAll, clazz, annotationClasses);
        return cacheKey.setCache(rs);
    }

    private static boolean isAnnotationPresent(boolean isAll, Method method, Class<?>... annotationClasses)
    {
        CacheKey cacheKey = new CacheKey(method, "isAnnotationPresent-Method" + isAll, annotationClasses);
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (boolean) cache;
        }
        boolean rs = NoCache.isAnnotationPresent(isAll, method, annotationClasses);
        return rs;
    }

    private static boolean isAnnotationPresent(boolean isAll, Field field, Class<?>... annotationClasses)
    {
        CacheKey cacheKey = new CacheKey(field, "isAnnotationPresent-Field" + isAll, annotationClasses);
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (boolean) cache;
        }
        boolean rs = NoCache.isAnnotationPresent(isAll, field, annotationClasses);
        return rs;
    }

    /**
     * 得到指定类的{@linkplain AutoSetDefaultDealt}注解。
     *
     * @param clazz
     * @return
     */
    public static AutoSetDefaultDealt getAutoSetDefaultDealt(Class<?> clazz)
    {
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

        AutoSetDefaultDealt autoSetDefaultDealt = getAnnotation(clazz, AutoSetDefaultDealt.class);
        if (autoSetDefaultDealt == null)
        {
            autoSetDefaultDealt = Advance.getAnnotation(clazz, AutoSetDefaultDealt.class);
        }
        if (autoSetDefaultDealt == null)
        {
            Annotation[] annotations = NoCache.getAnnotations(clazz);
            for (Annotation annotation : annotations)
            {
                AutoSetDefaultDealt _autoSetDefaultDealt =
                        NoCache.getAnnotation(annotation.annotationType(), AutoSetDefaultDealt.class);
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
        return cacheKey.setCache(autoSetDefaultDealt);
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
        Class type = Advance.getRealTypeOfField(currentClass, field);
        return getAutoSetDefaultDealt(type);
    }

    private static class _AdvancedAnnotation
    {
        AdvancedAnnotation advancedAnnotation;
        AdvancedAnnotation.Handle handle;

        public _AdvancedAnnotation(AdvancedAnnotation advancedAnnotation)
        {
            this.advancedAnnotation = advancedAnnotation;
        }

        boolean enableCache()
        {
            return advancedAnnotation.enableCache();
        }

        private final <A extends Annotation> A gotAnnotation(@MayNull Class clazz, @MayNull Method method,
                @MayNull Parameter parameter, @MayNull Field field, A annotation)
        {
            if (handle == null)
            {
                return annotation;
            } else
            {
                return (A) handle.onGotAnnotation(clazz, method, parameter, field, annotation);
            }
        }

        final <A extends Annotation> A onGotAnnotation(Class clazz, A annotation)
        {
            return gotAnnotation(clazz, null, null, null, annotation);
        }

        final <A extends Annotation> A onGotAnnotation(Method method, A annotation)
        {
            return gotAnnotation(null, method, null, null, annotation);
        }

        final <A extends Annotation> A onGotAnnotation(Parameter parameter, A annotation)
        {
            return gotAnnotation(null, null, parameter, null, annotation);
        }

        final <A extends Annotation> A onGotAnnotation(Field field, A annotation)
        {
            return gotAnnotation(null, null, null, field, annotation);
        }

        final <A extends Annotation> A onGotAnnotation(A annotation)
        {
            return gotAnnotation(null, null, null, null, annotation);
        }

        public boolean enableAdvancedAnnotation()
        {
            return advancedAnnotation.enableAdvancedAnnotation();
        }
    }

    private static final <A extends Annotation> _AdvancedAnnotation getAdvancedAnnotation(Class<A> annotationClass)
    {
        CacheKey cacheKey = new CacheKey(annotationClass, "getAdvancedAnnotation");
        Object cache = cacheKey.getCache();
        _AdvancedAnnotation _advancedAnnotation = null;
        if (cache != null)
        {
            _advancedAnnotation = (_AdvancedAnnotation) cache;
        }
        if (_advancedAnnotation == null)
        {
            AdvancedAnnotation advancedAnnotation = annotationClass.getAnnotation(AdvancedAnnotation.class);
            if (advancedAnnotation == null)
            {
                advancedAnnotation = DEFAULT_ADVANCED_ANNOTATION;
            }
            _advancedAnnotation = new _AdvancedAnnotation(advancedAnnotation);
            if (!advancedAnnotation.handle().equals(AdvancedAnnotation.Handle.class))
            {
                try
                {
                    AdvancedAnnotation.Handle handle = OftenTool.newObject(advancedAnnotation.handle());
                    _advancedAnnotation.handle = handle;
                } catch (Throwable e)
                {
                    e = OftenTool.getCause(e);
                    LOGGER.warn(e.getMessage(), e);
                }
            }

        }

        return cacheKey.setCache(_advancedAnnotation);
    }

    public static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(annotationClass, "getAnnotation[]", annotations);
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
        }
        A a = NoCache.getAnnotation(annotations, annotationClass);
        a = advancedAnnotation.onGotAnnotation(a);
        return cacheKey == null ? a : cacheKey.setCache(a);
    }

    /**
     * 获取类上的注解。
     * @param object 用于获取类。
     * @param annotationClass
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Object object, Class<A> annotationClass)
    {
        return getAnnotation(PortUtil.getRealClass(object), annotationClass);
    }

    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(clazz, "getAnnotation", annotationClass);
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
        }


        A a = NoCache.getAnnotation(clazz, annotationClass);
        if (a == null && advancedAnnotation.enableAdvancedAnnotation())
        {
            a = Advance.getAnnotation(clazz, annotationClass);
        }
        a = advancedAnnotation.onGotAnnotation(clazz, a);
        return cacheKey == null ? a : cacheKey.setCache(a);
    }

    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(field, "getAnnotation", annotationClass);
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
        }

        A a = NoCache.getAnnotation(field, annotationClass);
        if (a == null && advancedAnnotation.enableAdvancedAnnotation())
        {
            a = Advance.getAnnotation(field, annotationClass);
        }
        a = advancedAnnotation.onGotAnnotation(field, a);
        return cacheKey == null ? a : cacheKey.setCache(a);
    }

    /**
     * 首先在函数上找，如果不存在则从类上找。
     *
     * @param annotationClass 待获取的注解。
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class clazz, Class<A> annotationClass)
    {
        A a = getAnnotation(method, annotationClass);
        if (a == null)
        {
            a = getAnnotation(clazz, annotationClass);
        }
        return a;
    }

    /**
     * 首先在成员变量上找，如果不存在则从类上找。
     *
     * @param annotationClass 待获取的注解。
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Field field, Class clazz, Class<A> annotationClass)
    {
        A a = getAnnotation(field, annotationClass);
        if (a == null)
        {
            a = getAnnotation(clazz, annotationClass);
        }
        return a;
    }

    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(method, "getAnnotation", annotationClass);
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
        }
        A a = NoCache.getAnnotation(method, annotationClass);
        if (a == null && advancedAnnotation.enableAdvancedAnnotation())
        {
            a = Advance.getAnnotation(method, annotationClass);
        }
        a = advancedAnnotation.onGotAnnotation(method, a);
        return cacheKey == null ? a : cacheKey.setCache(a);
    }

    public static <A extends Annotation> A getAnnotation(Parameter parameter, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(parameter, "getAnnotation", annotationClass);
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
        }

        A a = NoCache.getAnnotation(parameter, annotationClass);
        if (a == null && advancedAnnotation.enableAdvancedAnnotation())
        {
            a = Advance.getAnnotation(parameter, annotationClass);
        }
        a = advancedAnnotation.onGotAnnotation(parameter, a);
        return cacheKey == null ? a : cacheKey.setCache(a);
    }

    public static Annotation[] getAnnotations(Class clazz)
    {
        CacheKey cacheKey = new CacheKey(clazz, "getAnnotations");
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (Annotation[]) cache;
        }
        Annotation[] as = NoCache.getAnnotations(clazz);
        return cacheKey.setCache(as);
    }

    public static Annotation[] getAnnotationsForAspectOperationOfPortIn(Porter porter)
    {
        CacheKey cacheKey = new CacheKey(porter.getClazz(), "aspect-portin");
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
        Annotation[] as = Advance.getAnnotationsForAspectOperationOfPortIn(porter);
        return cacheKey.setCache(as);
    }

    public static Annotation[] getAnnotationsForAspectOperationOfPortIn(PorterOfFun porterOfFun)
    {
        CacheKey cacheKey = new CacheKey(porterOfFun.getMethod(), "aspect-portfun");
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
        Annotation[] as = Advance.getAnnotationsForAspectOperationOfPortIn(porterOfFun);
        return cacheKey.setCache(as);
    }

    /**
     * 得到指定的注解上的{@linkplain AspectOperationOfNormal}注解
     *
     * @param annotation
     * @return
     */
    public static AspectOperationOfNormal getAspectOperationOfNormal(Annotation annotation)
    {
        CacheKey cacheKey = new CacheKey(annotation.annotationType(), "AspectOperationOfNormal");
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
        return cacheKey.setCache(Advance.getAspectOperationOfNormal(annotation));
    }

    /**
     * 得到指定注解上的{@linkplain AspectOperationOfPortIn}注解。
     *
     * @param annotation
     * @return
     */
    public static AspectOperationOfPortIn getAspectOperationOfPortIn(Annotation annotation)
    {
        CacheKey cacheKey = new CacheKey(annotation.annotationType(), "AspectOperationOfPortIn");

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
        return cacheKey.setCache(Advance.getAspectOperationOfPortIn(annotation));
    }

    public static Annotation[] getAnnotations(Method method)
    {
        CacheKey cacheKey = new CacheKey(method, "getAnnotations");
        Object cache = cacheKey.getCache();
        if (cache != null && cache != NULL)
        {
            return (Annotation[]) cache;
        }
        return cacheKey.setCache(NoCache.getAnnotations(method));
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
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(clazz, "getAnnotationsWithSuper", annotationClass);
            Object cache = cacheKey.getCache();
            if (cache != null)
            {
                if (cache == NULL)
                {
                    return null;
                } else
                {
                    return (List<A>) cache;
                }
            }
        }
        List<A> list = NoCache.getAnnotationsWithSuper(clazz, annotationClass);
        for (int i = 0; i < list.size(); i++)
        {
            A a = list.get(i);
            A a2 = advancedAnnotation.onGotAnnotation(clazz, a);
            if (a2 != a)
            {
                list.set(i, a2);
            }
        }
        return cacheKey == null ? list : cacheKey.setCache(list);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(clazz, "getRepeatableAnnotations", annotationClass);
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
        }
        A[] as = NoCache.getRepeatableAnnotations(clazz, annotationClass);
        if (OftenTool.isEmpty(as) && advancedAnnotation.enableAdvancedAnnotation())
        {
            as = Advance.getRepeatableAnnotations(clazz, annotationClass);
        }
        for (int i = 0; i < as.length; i++)
        {
            A a = as[i];
            A a2 = advancedAnnotation.onGotAnnotation(clazz, a);
            if (a2 != a)
            {
                as[i] = a2;
            }
        }
        return cacheKey == null ? as : cacheKey.setCache(as);
    }

    public static <A extends Annotation> A[] getRepeatableAnnotations(Field field, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(field, "getRepeatableAnnotations", annotationClass);
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
        }
        A[] as = NoCache.getRepeatableAnnotations(field, annotationClass);
        if (OftenTool.isEmpty(as) && advancedAnnotation.enableAdvancedAnnotation())
        {
            as = Advance.getRepeatableAnnotations(field, annotationClass);
        }
        for (int i = 0; i < as.length; i++)
        {
            A a = as[i];
            A a2 = advancedAnnotation.onGotAnnotation(field, a);
            if (a2 != a)
            {
                as[i] = a2;
            }
        }
        return cacheKey == null ? as : cacheKey.setCache(as);
    }


    public static <A extends Annotation> A[] getRepeatableAnnotations(Method method, Class<A> annotationClass)
    {
        _AdvancedAnnotation advancedAnnotation = getAdvancedAnnotation(annotationClass);
        CacheKey cacheKey = null;
        if (advancedAnnotation.enableCache())
        {
            cacheKey = new CacheKey(method, "getRepeatableAnnotations", annotationClass);
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
        }
        A[] as = NoCache.getRepeatableAnnotations(method, annotationClass);
        if (OftenTool.isEmpty(as) && advancedAnnotation.enableAdvancedAnnotation())
        {
            as = Advance.getRepeatableAnnotations(method, annotationClass);
        }
        for (int i = 0; i < as.length; i++)
        {
            A a = as[i];
            A a2 = advancedAnnotation.onGotAnnotation(method, a);
            if (a2 != a)
            {
                as[i] = a2;
            }
        }
        return cacheKey == null ? null : cacheKey.setCache(as);
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

    public static class NoCache
    {
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

        public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass)
        {
            return _getAnnotation(clazz, annotationClass, true);
        }

        private static <A extends Annotation> A _getAnnotation(Class<?> clazz, Class<A> annotationClass,
                boolean willProxy)
        {
            A t = clazz.getAnnotation(annotationClass);
            if (t == null && Modifier.isInterface(clazz.getModifiers()) && annotationClass
                    .isAnnotationPresent(Inherited.class))
            {
                Class<?>[] ins = clazz.getInterfaces();
                for (Class<?> inClass : ins)
                {
                    t = _getAnnotation(inClass, annotationClass, willProxy);//递归调用
                    if (t != null)
                    {
                        return t;//已经代理过
                    }
                }
            }
            return willProxy ? proxyAnnotationForAttr(t) : t;
        }

        public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass)
        {
            return _getAnnotation(field, annotationClass, true);
        }

        private static <A extends Annotation> A _getAnnotation(Field field, Class<A> annotationClass, boolean willProxy)
        {
            A t = field.getAnnotation(annotationClass);
            return willProxy ? proxyAnnotationForAttr(t) : t;
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

        private static <A extends Annotation> A _getAnnotation(Method method, Class<A> annotationClass,
                boolean willProxy)
        {
            boolean isInherited = annotationClass.isAnnotationPresent(Inherited.class);
            A t = getAnnotation(method.getDeclaringClass(), method, annotationClass, isInherited);
            if (t == null && isInherited && Modifier.isInterface(method.getDeclaringClass().getModifiers()))
            {
                t = getAnnotationForFromInterface(method, annotationClass);
            }
            t = willProxy ? proxyAnnotationForAttr(t) : t;
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
                    Method superMethod = Advance.getSuperMethod(childClass, method, superClass, false);
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

        private static <A extends Annotation> A getAnnotationForFromInterface(Method method, Class<A> annotationClass)
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

        public static <A extends Annotation> A getAnnotation(Parameter parameter, Class<A> annotationClass)
        {
            A t = parameter.getAnnotation(annotationClass);
            return proxyAnnotationForAttr(t);
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
            getAnnotations(clazz.getSuperclass(), list, typeSet);//递归调用
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
            OftenTool.addAll(list, as);
            for (Annotation annotation : as)
            {
                typeSet.add(annotation.annotationType());
            }

            Class childClass = method.getDeclaringClass();
            Class<?> superClass = childClass.getSuperclass();
            while (superClass != null)
            {
                Method superMethod = Advance.getSuperMethod(childClass, method, superClass, false);
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


        public static boolean isAnnotationPresent(boolean isAll, Class<?> clazz,
                Class<?>... annotationClasses)
        {
            boolean rs;
            outer:
            do
            {
                for (Class c : annotationClasses)
                {
                    if (_getAnnotation(clazz, c, false) != null)
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
            return rs;
        }

        public static boolean isAnnotationPresent(boolean isAll, Method method, Class<?>... annotationClasses)
        {
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
            return rs;
        }

        public static boolean isAnnotationPresent(boolean isAll, Field field, Class<?>... annotationClasses)
        {
            boolean rs;
            outer:
            do
            {
                for (Class c : annotationClasses)
                {
                    Annotation t = _getAnnotation(field, c, false);
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
            return rs;
        }
    }

    /**
     * 用于获取注解的高级选项,见{@linkplain IDynamicAnnotationImprovable}
     */
    public static class Advance
    {
        public static void addDeny(String className)
        {
            DynamicAnnotationImprovableWrap.addDeny(className);
        }

        public static void addWhite(String className)
        {
            DynamicAnnotationImprovableWrap.addWhite(className);
        }

        /**
         * 代理指定的注解，使得注解字串内容支持参数。
         */
        public static <A extends Annotation> A doProxyForDynamicAttr(A a)
        {
            return proxyAnnotationForAttr(a);
        }

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

        private static <A extends Annotation> IDynamicAnnotationImprovable[] getDynamic(Class<A> annotationType)
        {
            IDynamicAnnotationImprovable[] as = classDynamicArrayMap.get(annotationType.getName());
            if (as == null)
            {
                as = DYNAMIC_EMPTY;
            }
            return as;
        }

        public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationType)
        {
            Worked worked = hasWorked(clazz, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            worked.reset();
            return (A) annotationResult;
        }

        public static <A extends Annotation> A getAnnotation(Parameter parameter, Class<A> annotationType)
        {
            Worked worked = hasWorked(parameter, annotationType);
            if (worked.isWorked)
            {
                return null;
            }
            Annotation annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
            {
                IDynamicAnnotationImprovable.Result<InvocationHandler, A> result =
                        iDynamicAnnotationImprovable.getAnnotation(parameter, annotationType);
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

            Annotation[] annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : porterDynamicArray)
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
            Annotation[] annotationResult = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : porterDynamicArray)
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
            worked.reset();
            return annotationResult;
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

            Class<? extends Annotation> atype = annotation.annotationType();
            AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil
                    .getAnnotation(atype, AspectOperationOfNormal.class);
            if (aspectOperationOfNormal == null)
            {
                for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : aspectDynamicArray)
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
            worked.reset();
            return aspectOperationOfNormal;
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

            Class<? extends Annotation> atype = annotation.annotationType();
            AspectOperationOfPortIn aspectOperationOfPortIn = AnnoUtil
                    .getAnnotation(atype, AspectOperationOfPortIn.class);
            if (aspectOperationOfPortIn == null)
            {
                for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : aspectDynamicArray)
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
            worked.reset();
            return aspectOperationOfPortIn;
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
            CacheKey cacheKey = new CacheKey(realClass, "getDirectGenericRealTypeAt-" + index, realClass);
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
            CacheKey cacheKey = new CacheKey(realClass, "getDirectGenericRealTypeByAssignable", realClass,
                    superClassOrInterface);
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
                if (OftenTool.isAssignable(clazz, superClassOrInterface))
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


        private static Class<?> getRealType(Class<?> declaringClass, Class<?> realClass, Class sourceType,
                Type genericType)
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
            if (rt == null && genericType instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] types = parameterizedType.getActualTypeArguments();
                if (types.length == 1 && types[0] instanceof WildcardType)
                {
                    WildcardType wildcardType = (WildcardType) types[0];
                    Type[] upperBounds = wildcardType.getUpperBounds();
                    if (upperBounds.length == 1 && upperBounds[0] instanceof Class)
                    {
                        rt = (Class<?>) upperBounds[0];
                    }
                } else if (sourceType.equals(Class.class) && types.length == 1 && types[0] instanceof Class)
                {
                    rt = (Class<?>) types[0];
                }
            }
            return rt;
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

            CacheKey cacheKey = new CacheKey(realClass, "getRealTypeOfField", field);
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
            Class type = getRealType(field.getDeclaringClass(), realClass, field.getType(), field.getGenericType());
            if (type == null)
            {
                type = field.getType();
            }
            cacheKey.setCache(type);
            return type;
        }


        /**
         * 获取实际的参数类型类别，支持泛型。
         *
         * @param realClass 实际的类
         * @param method    方法
         * @return
         */
        public static Class[] getParameterRealTypes(Class realClass, Method method)
        {
            int argCount = method.getParameterCount();
            Class[] types = new Class[argCount];
            for (int i = 0; i < argCount; i++)
            {
                Class<?> paramType = AnnoUtil.Advance.getRealTypeOfMethodParameter(realClass, method, i);
                types[i] = paramType;
            }
            return types;
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

            CacheKey cacheKey = new CacheKey(realClass, "getRealTypeOfMethodParameter-" + argIndex, method);
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
            Class type = getRealType(method.getDeclaringClass(), realClass, method.getParameterTypes()[argIndex],
                    method.getGenericParameterTypes()[argIndex]);
            if (type == null)
            {
                type = method.getParameterTypes()[argIndex];
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

            CacheKey cacheKey = new CacheKey(realClass, "getRealTypeOfMethodReturn", method);
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
                    method.getReturnType(), method.getGenericReturnType());
            if (type == null)
            {
                type = method.getReturnType();
            }
            cacheKey.setCache(type);
            return type;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Class<?> clazz, Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", clazz, annotationType);
            if (worked.isWorked)
            {
                return null;
            }

            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            worked.reset();
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Field field, Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", field, annotationType);
            if (worked.isWorked)
            {
                return null;
            }

            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            worked.reset();
            return annos;
        }

        public static <A extends Annotation> A[] getRepeatableAnnotations(Method method, Class<A> annotationType)
        {
            Worked worked = hasWorked("getRepeatableAnnotations:", method, annotationType);
            if (worked.isWorked)
            {
                return null;
            }

            A[] annos = null;
            for (IDynamicAnnotationImprovable iDynamicAnnotationImprovable : getDynamic(annotationType))
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
            worked.reset();
            return annos;
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
                    if (OftenTool.getAccessType(m) > OftenTool.getAccessType(method))
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
         * 获取父类中被继承或重载的函数。
         *
         * @param method
         * @return
         */
        public static Method getSuperMethod(Method method)
        {
            return getSuperMethod(method.getDeclaringClass(), method, method.getDeclaringClass().getSuperclass(), true);
        }

        public static void removeDeny(String className)
        {
            DynamicAnnotationImprovableWrap.removeDeny(className);
        }

        public static void removeWhite(String className)
        {
            DynamicAnnotationImprovableWrap.removeWhite(className);
        }

        public static void setUseWhiteOrDeny(boolean useWhiteOrDeny)
        {
            DynamicAnnotationImprovableWrap.setUseWhiteOrDeny(useWhiteOrDeny);
        }

    }

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

    private static class Configable
    {
        IAnnotationConfigable iAnnotationConfigable;

        public Configable(IAnnotationConfigable iAnnotationConfigable)
        {
            this.iAnnotationConfigable = iAnnotationConfigable;
        }
    }

    private static class CacheKey
    {
        WeakReference<Object> targetRef;
        Object annotationType;
        int hashCode;

        public CacheKey(Object target, String tag, Object... array)
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

//        public CacheKey(Object target, Object annotationType)
//        {
//            this.targetRef = new WeakReference<>(target);
//            this.annotationType = annotationType;
//        }

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

        <T> T setCache(T obj)
        {
//            if (LOGGER.isDebugEnabled())
//            {
//                LOGGER.debug("set cache:key=[{}],cache value=[{}]", this, obj == null ? "null" : obj);
//            }
            annotationCache.put(this, new WeakReference<>(obj == null ? NULL : obj));
            return obj;
        }
    }
}
