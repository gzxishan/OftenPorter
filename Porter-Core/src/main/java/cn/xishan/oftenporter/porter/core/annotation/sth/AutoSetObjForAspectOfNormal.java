package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public class AutoSetObjForAspectOfNormal
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSetObjForAspectOfNormal.class);

    @KeepFromProguard
    public interface IOPProxy
    {
        Class<?> get_R_e_a_l_C_l_a_s_s();
    }

    private static final Method METHOD_GET_REAL_CLASS;

    static
    {
        Method method = null;
        try
        {
            method = IOPProxy.class.getMethod("get_R_e_a_l_C_l_a_s_s");
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        METHOD_GET_REAL_CLASS = method;
    }

    static class AspectTask
    {
        WObject wObject;
        AspectOperationOfNormal.Handle[] handles;
        Object interceptorObj;
        MethodProxy proxy;
        Object origin;
        Method originMethod;
        Object[] args;
        boolean isTop;


        public AspectTask(WObject wObject,
                AspectOperationOfNormal.Handle[] handles, Object interceptorObj, MethodProxy proxy, Object origin,
                Method originMethod, Object[] args)
        {
            this.wObject = wObject;
            this.handles = handles;
            this.interceptorObj = interceptorObj;
            this.proxy = proxy;
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
                return proxy.invokeSuper(interceptorObj, args);
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

        void invokeNotEndExceptionNow(Throwable throwable) throws Throwable
        {
            Enumeration<AspectOperationOfNormal.Handle> enumeration = EnumerationImpl.fromArray(handles, false);
            while (enumeration.hasMoreElements())
            {
                AspectOperationOfNormal.Handle handle = enumeration.nextElement();
                handle.onException(wObject, isTop, origin, originMethod, invoker, args, throwable);
            }
        }

        private void invokeEndNow(Object lastReturn) throws Throwable
        {
            for (int i = handles.length - 1; i >= 0; i--)
            {
                AspectOperationOfNormal.Handle handle = handles[i];
                handle.onEnd(wObject, isTop, origin, originMethod, invoker, lastReturn);
            }
        }

        void invokeEnd(Object lastReturn) throws Throwable
        {
            if (wObject == null)
            {
                invokeEndNow(lastReturn);
            } else
            {
                wObject.addListener(new WObject.IFinalListener()
                {
                    @Override
                    public void afterFinal(WObject wObject) throws Throwable
                    {
                        invokeEndNow(lastReturn);
                    }

                    @Override
                    public void beforeFinal(WObject wObject) throws Throwable
                    {

                    }

                    @Override
                    public void onFinalException(WObject wObject, Throwable throwable) throws Throwable
                    {
                        for (int i = handles.length - 1; i >= 0; i--)
                        {
                            AspectOperationOfNormal.Handle handle = handles[i];
                            handle.onEndException(wObject, isTop, origin, originMethod, invoker, args, throwable);
                        }
                    }
                });
            }
        }
    }

    public class MethodInterceptorImpl implements MethodInterceptor
    {
        private Object origin;
        Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap;

        public MethodInterceptorImpl(Object origin, Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap)
        {
            this.origin = origin;
            this.aspectHandleMap = aspectHandleMap;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
        {
            if (method.equals(METHOD_GET_REAL_CLASS))
            {
                return origin.getClass();
            }

            WObject wObject = WObject.fromThreadLocal();
            AspectOperationOfNormal.Handle[] handles = this.aspectHandleMap.get(method);
            AspectTask aspectTask = new AspectTask(wObject, handles, obj, methodProxy, origin, method, args);

            try
            {
                Object lastReturn = aspectTask.invokeNow();
                aspectTask.invokeEnd(lastReturn);
                return lastReturn;
            } catch (Throwable throwable)
            {
                aspectTask.invokeNotEndExceptionNow(throwable);
                throw throwable;
            }
        }
    }

    private static Set<Method> aspectHandleSet = Collections.newSetFromMap(new WeakHashMap<>());
    private static Set<Object> seekedObjectSet = Collections.newSetFromMap(new WeakHashMap<>());
    private Object callbackFilter = null;

    static
    {
        aspectHandleSet.add(METHOD_GET_REAL_CLASS);
    }

    public AutoSetObjForAspectOfNormal()
    {
    }

    public Object doProxy(Object object, AutoSetHandle autoSetHandle) throws Exception
    {
        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            if (seekedObjectSet.contains(object))
            {
                return object;
            }
        }
        if (object instanceof IOPProxy)
        {
            return object;
        }
        Class<?> clazz = PortUtil.getRealClass(object);
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
                synchronized (AutoSetObjForAspectOfNormal.class)
                {
                    aspectHandleSet.add(method);
                }
                methodTypeMap.put(method, list);
            }
        }

        synchronized (AutoSetObjForAspectOfNormal.class)
        {
            seekedObjectSet.add(object);
        }

        if (existsAspect)
        {
            if (this.callbackFilter == null)
            {
                CallbackFilter callbackFilter = method -> {
                    synchronized (AutoSetObjForAspectOfNormal.class)
                    {
                        return aspectHandleSet.contains(method) ? 1 : 0;
                    }
                };
                this.callbackFilter = callbackFilter;
            }
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
                    if (handle.init(annotation, configData, object, entry.getKey()))
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

            Callback[] callbacks =
                    new Callback[]{NoOp.INSTANCE, new MethodInterceptorImpl(object, aspectHandleMap)};

            Enhancer enhancer = new Enhancer();
            enhancer.setCallbacks(callbacks);
            enhancer.setCallbackFilter((CallbackFilter) callbackFilter);
            enhancer.setSuperclass(object.getClass());
            enhancer.setInterfaces(new Class[]{
                    IOPProxy.class
            });
            object = enhancer.create();
        }


        return object;
    }
}
