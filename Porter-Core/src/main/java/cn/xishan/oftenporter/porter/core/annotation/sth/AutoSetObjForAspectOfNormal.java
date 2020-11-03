package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.proxy.IOftenProxy;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public class AutoSetObjForAspectOfNormal
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSetObjForAspectOfNormal.class);

    public abstract static class PatternHandle implements AspectOperationOfNormal.Handle
    {

        private String methodPattern;

        /**
         * @param methodPattern 用于匹配方法（支持的字符集：[a-zA-Z0-9\s_\[\]$.(),?*]），支持通配符:?与*。见{@linkplain Method#toString()}。
         */
        public PatternHandle(String methodPattern)
        {
            this.methodPattern = methodPattern;
        }

        @Override
        public final boolean init(Annotation current, IConfigData configData, Object originObject, Class originClass,
                Method originMethod) throws Exception
        {
            return init(configData, originObject, originClass, originMethod);
        }

        public abstract boolean init(IConfigData configData, Object originObject, Class originClass,
                Method originMethod) throws Exception;

        public String getMethodPattern()
        {
            return methodPattern;
        }

        public void setMethodPattern(String methodPattern)
        {
            this.methodPattern = methodPattern;
        }
    }

    public static class AdvancedHandle
    {

        private static final String[] SPECIALS = {"[", "]", "(", ")", "$", "."};
        private static final Pattern LEGAL_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s_\\[\\]$.(),?*]+$");

        private Pattern pattern;
        private boolean isAfter;
        private PatternHandle handle;

        /**
         * @param isAfter true放在注解切面的后面；false放在注解切面的前面。
         * @param handle
         */
        public AdvancedHandle(boolean isAfter, PatternHandle handle)
        {
            String methodPattern = handle.getMethodPattern();
            methodPattern = methodPattern.trim();

            if (!LEGAL_PATTERN.matcher(methodPattern).find())
            {
                throw new InitException("illegal pattern:" + methodPattern);
            }

            for (String spe : SPECIALS)
            {
                methodPattern = methodPattern.replace(spe, "\\" + spe);
            }

            methodPattern = methodPattern.replace("?", ".?");
            methodPattern = methodPattern.replace("*", ".*");

            methodPattern = "^" + methodPattern + "$";

            this.pattern = Pattern.compile(methodPattern);
            this.isAfter = isAfter;
            this.handle = handle;
        }

        public Pattern getPattern()
        {
            return pattern;
        }

        public boolean isAfter()
        {
            return isAfter;
        }

        public AspectOperationOfNormal.Handle getHandle()
        {
            return handle;
        }
    }

    @KeepFromProguard
    public interface IOPProxy extends IOftenProxy
    {

    }

    static class AspectTask
    {
        OftenObject oftenObject;
        AspectOperationOfNormal.Handle[] handles;
        Object interceptorObj;
        MethodProxy methodProxy;
        Object origin;
        Method originMethod;
        Object[] args;
        boolean isTop;


        public AspectTask(boolean isTop,
                AspectOperationOfNormal.Handle[] handles, Object interceptorObj, MethodProxy methodProxy, Object origin,
                Method originMethod, Object[] args)
        {
            this.handles = handles;
            this.interceptorObj = interceptorObj;
            this.methodProxy = methodProxy;
            this.origin = origin;
            this.originMethod = originMethod;
            this.args = args;
            this.isTop = isTop;
        }

        public AspectTask(OftenObject oftenObject,
                AspectOperationOfNormal.Handle[] handles, Object interceptorObj, MethodProxy methodProxy, Object origin,
                Method originMethod, Object[] args)
        {
            this(oftenObject == null || oftenObject.isOriginalRequest(),
                    handles, interceptorObj, methodProxy, origin, originMethod, args);
            this.oftenObject = oftenObject;
        }

        AspectOperationOfNormal.DefaultInvoker invoker = new AspectOperationOfNormal.DefaultInvoker()
        {
            boolean invoked = false;

            @Override
            public boolean hasInvoked()
            {
                return invoked;
            }

            @Override
            public Object invoke(Object[] args) throws Throwable
            {
                invoked = true;
                return methodProxy.invokeSuper(interceptorObj, args);
            }
        };

        Object invokeNow() throws Throwable
        {
            Throwable throwable = null;

            Object lastReturn = null;
            boolean isInvoked = false;
            try
            {
                for (int i = 0; i < handles.length; i++)
                {
                    AspectOperationOfNormal.Handle handle = handles[i];
                    if (handle
                            .preInvoke(oftenObject, isTop, origin, originMethod, invoker, args, isInvoked, lastReturn))
                    {
                        isInvoked = true;
                        lastReturn = handle
                                .doInvoke(oftenObject, isTop, origin, originMethod, invoker, args, lastReturn);
                    }
                }
                if (!isInvoked)
                {
                    lastReturn = invoker.invoke(args);
                }

                for (int i = handles.length - 1; i >= 0; i--)
                {
                    AspectOperationOfNormal.Handle handle = handles[i];
                    lastReturn = handle
                            .afterInvoke(oftenObject, isTop, origin, originMethod, invoker, args, lastReturn);
                }
            } catch (Throwable th)
            {
                throwable = th;
            }
            if (throwable != null)
            {
                throw throwable;
            }
            return lastReturn;
        }

        void invokeExceptionNow(Throwable throwable)
        {
            for (int i = handles.length - 1; i >= 0; i--)
            {
                AspectOperationOfNormal.Handle handle = handles[i];
                try
                {
                    handle.onException(oftenObject, isTop, origin, originMethod, invoker, args, throwable);
                } catch (Throwable e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    static class MethodInterceptorImpl implements MethodInterceptor
    {
        private WeakReference<Object> originRef;
        private static ThreadLocal<Boolean> threadLocalForNoOften = new ThreadLocal<>();

        Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap;

        public MethodInterceptorImpl(Object origin, Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap)
        {
            this.originRef = new WeakReference<>(origin);
            this.aspectHandleMap = aspectHandleMap;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
        {
            OftenObject oftenObject = OftenObject.current();
            AspectOperationOfNormal.Handle[] handles = this.aspectHandleMap.get(method);
            AspectTask aspectTask;

            boolean isTop = false;
            if (oftenObject == null)
            {
                if (threadLocalForNoOften.get() == null)
                {
                    isTop = true;
                    threadLocalForNoOften.set(true);
                }
                aspectTask = new AspectTask(isTop, handles, obj, methodProxy, originRef.get(), method, args);
            } else
            {
                aspectTask = new AspectTask(oftenObject, handles, obj, methodProxy, originRef.get(), method, args);
            }


            try
            {
                Object lastReturn = aspectTask.invokeNow();
                return lastReturn;
            } catch (Throwable throwable)
            {
                aspectTask.invokeExceptionNow(throwable);
                throw throwable;
            } finally
            {
                if (isTop)
                {
                    threadLocalForNoOften.remove();
                }
            }
        }
    }

    private static Set<String> aspectMethodHandleCache;

    //*********List的Object类型：Annotation[]，AdvancedHandle
    private Map<String, List<Object>> methodHanldeCache;
    private Set<Object> seekedObjectSet = Collections.newSetFromMap(new WeakHashMap<>());
    private List<AdvancedHandle> advancedHandleList;

    private static CallbackFilter callbackFilter = method -> {//static类型，防止当前类被引用无法释放。
        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            String mkey = method.toString();
            return aspectMethodHandleCache.contains(mkey) ? 1 : 0;
        }
    };


    public AutoSetObjForAspectOfNormal(List<AdvancedHandle> advancedHandleList)
    {
        clearCache();
        this.advancedHandleList = advancedHandleList;
    }


    public void clearCache()
    {
        aspectMethodHandleCache = ConcurrentHashMap.newKeySet();
        methodHanldeCache = new ConcurrentHashMap<>();
    }

    boolean hasProxy(Object object)
    {
        return object instanceof IOPProxy;
    }

    Object doProxyOrNew(Object objectMayNull, Class objectClass, AutoSetHandle autoSetHandle) throws Exception
    {
        if (objectMayNull != null && seekedObjectSet.contains(objectMayNull))
        {
            return objectMayNull;
        }

        if (hasProxy(objectMayNull))
        {
            return objectMayNull;
        }

        Class<?> clazz = objectMayNull == null ? objectClass : PortUtil.getRealClass(objectMayNull);
        LOGGER.debug("seek normal aspect of class:{}", clazz);

        //*********List的Object类型：Annotation[]，AdvancedHandle
        Map<Method, List<Object>> methodTypeMap = new WeakHashMap<>();

        Map<Annotation, AspectOperationOfNormal> classAspectOperationMap = new HashMap<>();

        {
            Annotation[] annotations = AnnoUtil.getAnnotations(clazz);
            for (Annotation annotation : annotations)
            {
                AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil
                        .getAspectOperationOfNormal(annotation);
                if (aspectOperationOfNormal != null)
                {
                    classAspectOperationMap.put(annotation, aspectOperationOfNormal);
                }
            }
        }

        AspectOperationOfNormal.IgnoreAspect ignoreAspect = AnnoUtil.getAnnotation(clazz,
                AspectOperationOfNormal.IgnoreAspect.class);

        boolean willIgnore = ignoreAspect != null && ignoreAspect.willIgnore();

        if (!willIgnore)
        {
            Method[] methods = OftenTool.getAllMethods(clazz);

            for (Method method : methods)
            {

                if (Modifier.isStatic(method.getModifiers()) || Modifier.isPrivate(method.getModifiers()))
                {
                    continue;
                }
                if (PortUtil.willIgnoreAdvanced(method.getDeclaringClass()))
                {
                    continue;
                }

                String mkey = method.toString();
                List<Object> list = methodHanldeCache.get(mkey);

                if (list == null)
                {
                    Annotation[] annotations = AnnoUtil.getAnnotations(method);
                    list = new ArrayList<>(5);
                    for (Annotation annotation : annotations)
                    {
                        AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil
                                .getAspectOperationOfNormal(annotation);
                        if (aspectOperationOfNormal == null)
                        {
                            aspectOperationOfNormal = classAspectOperationMap.get(annotation);//如果函数上不存在指定注解、则获取类上的
                        }
                        if (aspectOperationOfNormal != null)
                        {
                            list.add(new Annotation[]{
                                    annotation, aspectOperationOfNormal
                            });
                        }
                    }
                    methodHanldeCache.put(mkey, list);

                    int beforeIndex = 0;
                    for (AdvancedHandle advancedHandle : advancedHandleList)
                    {
                        Matcher matcher = advancedHandle.getPattern().matcher(mkey);
                        if (matcher.find())
                        {
                            if (advancedHandle.isAfter())
                            {
                                list.add(advancedHandle.getHandle());
                            } else
                            {
                                list.add(beforeIndex++, advancedHandle.getHandle());
                            }
                        }
                    }
                }

                if (list.size() > 0)
                {
                    if (Modifier.isPrivate(method.getModifiers()))
                    {
                        LOGGER.warn("ignore private method aspect of normal:{}", method);
                    } else
                    {
                        aspectMethodHandleCache.add(method.toString());
                        methodTypeMap.put(method, list);
                    }
                }
            }
        }

        if (objectMayNull != null)
        {
            seekedObjectSet.add(objectMayNull);
        }

        Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap = new HashMap<>();

        Object objectForInit = objectMayNull;
        if (!methodTypeMap.isEmpty())
        {
            MethodInterceptorImpl methodInterceptor = new MethodInterceptorImpl(objectMayNull, aspectHandleMap);
            Callback[] callbacks = new Callback[]{NoOp.INSTANCE, methodInterceptor};

            Enhancer enhancer = new Enhancer();
            enhancer.setUseCache(true);
            enhancer.setCallbacks(callbacks);
            enhancer.setCallbackFilter(callbackFilter);
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[]{
                    IOPProxy.class
            });
            Object proxyObject;
            try
            {
                proxyObject = enhancer.create();
            } catch (Throwable e)
            {
                LOGGER.warn("create proxy object error:class={}", clazz);
                throw e;
            }
            if (objectMayNull == null)
            {
                methodInterceptor.originRef = new WeakReference<>(proxyObject);
            } else
            {
                ProxyUtil.initFieldsValue(objectMayNull, proxyObject, true);
            }

            autoSetHandle.putProxyObject(objectMayNull, proxyObject);
            objectMayNull = proxyObject;
        } else if (objectMayNull == null)
        {
            objectMayNull = OftenTool.newObjectMayNull(objectClass);
        }

        if (objectForInit == null)
        {
            objectForInit = objectMayNull;
        }


        if (!methodTypeMap.isEmpty())
        {
            IConfigData configData = autoSetHandle.getContextObject(IConfigData.class);

            for (Map.Entry<Method, List<Object>> entry : methodTypeMap.entrySet())
            {
                List<Object> annotationList = entry.getValue();
                List<AspectOperationOfNormal.Handle> handles = new ArrayList<>(annotationList.size());

                for (int i = 0; i < annotationList.size(); i++)
                {
                    AspectOperationOfNormal.Handle handle;
                    Annotation annotation = null;
                    Object handleObject = annotationList.get(i);

                    if (handleObject != null && handleObject.getClass().isArray())
                    {
                        Annotation[] annotations = (Annotation[]) handleObject;
                        annotation = annotations[0];
                        AspectOperationOfNormal aspectOperationOfNormal = (AspectOperationOfNormal) annotations[1];

                        handle = OftenTool.newObject(aspectOperationOfNormal.handle());
                    } else
                    {
                        handle = (AspectOperationOfNormal.Handle) handleObject;
                    }

                    if (handle.init(annotation, configData, objectForInit, clazz, entry.getKey()))
                    {
                        autoSetHandle.addAutoSetsForNotPorter(new Object[]{handle});
                        handles.add(handle);
                    }
                }
                if (handles.size() > 0)
                {
                    aspectHandleMap.put(entry.getKey(), handles.toArray(new AspectOperationOfNormal.Handle[0]));
                }

            }

        }

        return objectMayNull;
    }
}
