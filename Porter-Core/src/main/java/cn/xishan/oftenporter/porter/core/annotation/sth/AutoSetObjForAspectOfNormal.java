package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.KeepFromProguard;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
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


    public class MethodInterceptorImpl implements MethodInterceptor
    {
        private Object origin;

        public MethodInterceptorImpl(Object origin)
        {
            this.origin = origin;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
        {
            if (method.equals(METHOD_GET_REAL_CLASS))
            {
                return origin.getClass();
            }

            WObject wObject = WObject.fromThreadLocal();
            AspectOperationOfNormal.Handle[] handles = aspectHandleMap.get(method);
            boolean isTop = wObject == null || wObject.isTopRequest();

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
                    return proxy.invokeSuper(obj, args);
                }
            };
            Object lastReturn = null;
            boolean isInvoked = false;
            for (int i = 0; i < handles.length; i++)
            {
                AspectOperationOfNormal.Handle handle = handles[i];
                if (handle.preInvoke(wObject, isTop, origin, method, invoker, args, lastReturn))
                {
                    isInvoked = true;
                    lastReturn = handle.doInvoke(wObject, isTop, origin, method, invoker, args, lastReturn);
                }
            }

            if (!isInvoked)
            {
                lastReturn = invoker.invoke(args);
            }

            for (int i = handles.length - 1; i >= 0; i--)
            {
                AspectOperationOfNormal.Handle handle = handles[i];
                lastReturn = handle.onEnd(wObject, isTop, origin, method, invoker, lastReturn);
            }

            return lastReturn;
        }
    }

    private Map<Method, AspectOperationOfNormal.Handle[]> aspectHandleMap = new WeakHashMap<>();
    private Object callbackFilter = null;

    public AutoSetObjForAspectOfNormal()
    {
    }

    public Object doProxy(Object object, AutoSetHandle autoSetHandle) throws Exception
    {
        if (object instanceof IOPProxy)
        {
            return object;
        }


        Method[] methods = WPTool.getAllMethods(PortUtil.getRealClass(object));
        boolean existsAspect = false;
        Map<Method, List<Annotation>> methodTypeMap = new HashMap<>();
        for (Method method : methods)
        {
            Package pkg = method.getDeclaringClass().getPackage();
            if (pkg != null && (pkg.getName().startsWith("java.") || pkg.getName().startsWith("javax.")))
            {
                continue;
            }
            Annotation[] annotations = method.getAnnotations();
            List<Annotation> list = new ArrayList<>(1);
            for (Annotation annotation : annotations)
            {
                Class<?> type = annotation.annotationType();
                if (type.isAnnotationPresent(AspectOperationOfNormal.class))
                {
                    list.add(annotation);
                    existsAspect = true;
                }
            }
            if (list.size() > 0)
            {
                methodTypeMap.put(method, list);
            }
        }

        if (existsAspect)
        {
            if (this.callbackFilter == null)
            {
                CallbackFilter callbackFilter = method -> aspectHandleMap.containsKey(method) ? 1 : 0;
                this.callbackFilter = callbackFilter;
            }
            for (Map.Entry<Method, List<Annotation>> entry : methodTypeMap.entrySet())
            {
                List<Annotation> annotationList = entry.getValue();
                List<AspectOperationOfNormal.Handle> handles = new ArrayList<>(annotationList.size());

                for (int i = 0; i < annotationList.size(); i++)
                {
                    Annotation annotation = annotationList.get(i);
                    AspectOperationOfNormal aspectOperationOfNormal = AnnoUtil
                            .getAnnotation(annotation.annotationType(), AspectOperationOfNormal.class);
                    AspectOperationOfNormal.Handle handle = WPTool.newObject(aspectOperationOfNormal.handle());
                    if (handle.init(annotation, object, entry.getKey()))
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
                    new Callback[]{NoOp.INSTANCE, new MethodInterceptorImpl(object)};

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
