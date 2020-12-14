package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;

import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2017/9/28.
 */
class AutoSetHandleWorkedInstance
{

    static class Result
    {
        boolean isWorked;
        Object object;

        public Result(boolean isWorked, Object object)
        {
            this.isWorked = isWorked;
            this.object = object;
        }
    }

    private Map<Integer, List<Object>> worked = new HashMap<>();
    private AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal;


    public AutoSetHandleWorkedInstance(AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal)
    {
        this.autoSetObjForAspectOfNormal = autoSetObjForAspectOfNormal;
    }

    public synchronized void clear()
    {
        worked.clear();
    }

    public Result workInstance(Object object, AutoSetHandle autoSetHandle, boolean doProxy) throws Exception
    {
        boolean isWorked = false;
        if (object != null)
        {
            if (!(object instanceof Class) && PortUtil.willIgnoreAdvanced(PortUtil.getRealClass(object)))
            {
                isWorked = true;
            } else
            {
                List<Object> list = worked.get(object.hashCode());
                if (list == null)
                {
                    list = new ArrayList<>();
                    worked.put(object.hashCode(), list);
                    list.add(object);
                } else
                {
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (list.get(i) == object)
                        {
                            isWorked = true;
                            break;
                        }
                    }
                    if (!isWorked)
                    {
                        list.add(object);
                    }
                }
            }

            if (!(object instanceof Class))
            {
                object = mayProxyAndDoAutoSet(object, null, autoSetHandle, doProxy, false);
            }
        }
        return new Result(isWorked, object);
    }

    Object doProxy(Object object, AutoSetHandle autoSetHandle) throws Exception
    {
        return mayProxyAndDoAutoSet(object, null, autoSetHandle, true, false);
    }

    Object newAndProxy(Class objectClass, AutoSetHandle autoSetHandle) throws Exception
    {
        return mayProxyAndDoAutoSet(null, objectClass, autoSetHandle, true, false);
    }

    void doAutoSet(Object object, AutoSetHandle autoSetHandle) throws Exception
    {
        mayProxyAndDoAutoSet(object, null, autoSetHandle, false, true);
    }

    private Object mayProxyAndDoAutoSet(Object objectMayNull, Class objectClass, AutoSetHandle autoSetHandle,
            boolean doProxy,
            boolean doAutoSet) throws Exception
    {
        if (doProxy && (objectMayNull != null || objectClass != null))
        {
            if (autoSetObjForAspectOfNormal != null)
            {
                objectMayNull = autoSetObjForAspectOfNormal
                        .doProxyOrNew(objectMayNull, objectClass, autoSetHandle);//用于通用的切面操作而进行代理设置
            }
        }

        if (doAutoSet && objectMayNull != null)
        {
            objectMayNull = autoSetHandle.doAutoSetForCurrent(false, objectMayNull, objectMayNull);//递归：设置被设置的变量。
        }
        return objectMayNull;
    }

    boolean hasProxy(Object object)
    {
        return autoSetObjForAspectOfNormal.hasProxy(object);
    }

}
