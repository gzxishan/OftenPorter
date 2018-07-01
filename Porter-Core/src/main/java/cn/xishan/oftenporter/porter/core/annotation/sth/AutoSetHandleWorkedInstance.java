package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.base.PortUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public synchronized Result workInstance(Object object,AutoSetHandle autoSetHandle,boolean doProxy)throws Exception
    {
        boolean isWorked = false;
        if (object != null)
        {
            Package pkg = PortUtil.getRealClass(object).getPackage();
            if (pkg != null && (pkg.getName().startsWith("java.")||pkg.getName().startsWith("javax.")))
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


        }
        if(doProxy&&!isWorked&&object!=null){
            if (autoSetObjForAspectOfNormal != null)
            {
                object = autoSetObjForAspectOfNormal.doProxy(object,autoSetHandle);//用于通用的切面操作而进行代理设置
            }
        }
        return new Result(isWorked, object);
    }

}
