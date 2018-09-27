package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public class AutoSetObjForAspectOfNormal
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSetObjForAspectOfNormal.class);

    @KeepFromProguard
    public interface IOPProxy
    {

    }

    static class AspectTask
    {
        WObject wObject;
        AspectOperationOfNormal.Handle[] handles;
        Object interceptorObj;
        MethodProxy methodProxy;
        Object origin;
        Method originMethod;
        Object[] args;
        boolean isTop;


        public AspectTask(WObject wObject,
                AspectOperationOfNormal.Handle[] handles, Object interceptorObj, MethodProxy methodProxy, Object origin,
                Method originMethod, Object[] args)
        {
            this.wObject = wObject;
            this.handles = handles;
            this.interceptorObj = interceptorObj;
            this.methodProxy = methodProxy;
            this.origin = origin;
            this.originMethod = originMethod;
            this.args = args;
            this.isTop = wObject == null || wObject.isTopRequest();
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
                    if (handle.preInvoke(wObject, isTop, origin, originMethod, invoker, args, isInvoked, lastReturn))
                    {
                        isInvoked = true;
                        lastReturn = handle.doInvoke(wObject, isTop, origin, originMethod, invoker, args, lastReturn);
                    }
                }
                if (!isInvoked)
                {
                    lastReturn = invoker.invoke(args);
                }

                for (int i = handles.length - 1; i >= 0; i--)
                {
                    AspectOperationOfNormal.Handle handle = handles[i];
                    lastReturn = handle.afterInvoke(wObject, isTop, origin, originMethod, invoker, args, lastReturn);
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
                    handle.onException(wObject, isTop, origin, originMethod, invoker, args, throwable);
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
        Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap;

        public MethodInterceptorImpl(Object origin, Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap)
        {
            this.originRef = new WeakReference<>(origin);
            this.aspectHandleMap = aspectHandleMap;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
        {
            WObject wObject = WObject.fromThreadLocal();
            AspectOperationOfNormal.Handle[] handles = this.aspectHandleMap.get(method);
            AspectTask aspectTask = new AspectTask(wObject, handles, obj, methodProxy, originRef.get(), method, args);

            try
            {
                Object lastReturn = aspectTask.invokeNow();
                return lastReturn;
            } catch (Throwable throwable)
            {
                aspectTask.invokeExceptionNow(throwable);
                throw throwable;
            }
        }
    }

    private static Set<Method> aspectHandleSet = ConcurrentHashMap.newKeySet();
    private Set<Object> seekedObjectSet = Collections.newSetFromMap(new WeakHashMap<>());

    private static CallbackFilter callbackFilter = method -> {//static类型，防止当前类被引用无法释放。
        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            return aspectHandleSet.contains(method) ? 1 : 0;
        }
    };

    public AutoSetObjForAspectOfNormal()
    {

    }

    public static void clearCache()
    {
        aspectHandleSet.clear();
    }

    boolean hasProxy(Object object)
    {
        return object instanceof IOPProxy;
    }

    Object doProxyOrNew(Object objectMayNull, Class objectClass, AutoSetHandle autoSetHandle) throws Exception
    {
        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            if (objectMayNull != null && seekedObjectSet.contains(objectMayNull))
            {
                return objectMayNull;
            }
        }
        if (hasProxy(objectMayNull))
        {
            return objectMayNull;
        }

        Class<?> clazz = objectMayNull == null ? objectClass : PortUtil.getRealClass(objectMayNull);

        IConfigData configData = autoSetHandle.getContextObject(IConfigData.class);

        Map<Annotation, AspectOperationOfNormal> classAspectOperationMap = new HashMap<>();

        {
            Annotation[] annotations = AnnoUtil.getAnnotations(clazz);
            for (Annotation annotation : annotations)
            {
                AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil.Advanced
                        .getAspectOperationOfNormal(annotation);
                if (aspectOperationOfNormal != null)
                {
                    classAspectOperationMap.put(annotation, aspectOperationOfNormal);
                }
            }
        }


        Method[] methods = WPTool.getAllMethods(clazz);
        boolean existsAspect = false;
        Map<Method, List<Annotation[]>> methodTypeMap = new WeakHashMap<>();
        for (Method method : methods)
        {

            if (Modifier.isStatic(method.getModifiers()) || Modifier.isPrivate(method.getModifiers()))
            {
                continue;
            }
            Package pkg = method.getDeclaringClass().getPackage();
            if (pkg != null && (pkg.getName().startsWith("java.") || pkg.getName().startsWith("javax.")))
            {
                continue;
            }
            Annotation[] annotations = AnnoUtil.getAnnotations(method);
            List<Annotation[]> list = new ArrayList<>(1);
            for (Annotation annotation : annotations)
            {
                AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil.Advanced
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
                    existsAspect = true;
                }
            }
            if (list.size() > 0)
            {
                if (Modifier.isPrivate(method.getModifiers()))
                {
                    LOGGER.warn("ignore private method aspect of normal:{}", method);
                } else
                {
                    synchronized (AutoSetObjForAspectOfNormal.class)
                    {
                        aspectHandleSet.add(method);
                    }
                    methodTypeMap.put(method, list);
                }
            }
        }

        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            if (objectMayNull != null)
            {
                seekedObjectSet.add(objectMayNull);
            }
        }

        if (existsAspect)
        {

            Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap = new HashMap<>();
            for (Map.Entry<Method, List<Annotation[]>> entry : methodTypeMap.entrySet())
            {
                List<Annotation[]> annotationList = entry.getValue();
                List<AspectOperationOfNormal.Handle> handles = new ArrayList<>(annotationList.size());

                for (int i = 0; i < annotationList.size(); i++)
                {
                    Annotation annotation = annotationList.get(i)[0];
                    AspectOperationOfNormal aspectOperationOfNormal = (AspectOperationOfNormal) annotationList
                            .get(i)[1];

                    AspectOperationOfNormal.Handle handle = WPTool.newObject(aspectOperationOfNormal.handle());
                    if (handle.init(annotation, configData, objectMayNull, clazz, entry.getKey()))
                    {
                        autoSetHandle.addAutoSetsForNotPorter(handle);
                        handles.add(handle);
                    }
                }
                if (handles.size() > 0)
                {
                    aspectHandleMap.put(entry.getKey(), handles.toArray(new AspectOperationOfNormal.Handle[0]));
                }

            }

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
            Object proxyObject = enhancer.create();
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
            objectMayNull = WPTool.newObjectMayNull(objectClass);
        }


        return objectMayNull;
    }
}
