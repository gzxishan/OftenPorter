package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _PortInObj
{
    public static class CLASS
    {
        public final Class<?> clazz;
        Method method;
        String option;
        public final PortInObj.IInObjHandle inObjHandle;

        public CLASS(Class<?> clazz)
        {
            this.clazz = clazz;
            this.inObjHandle = null;
        }

        public CLASS(Class<?> clazz, Method method, String option, PortInObj.IInObjHandle inObjHandle)
        {
            this.clazz = clazz;
            this.method = method;
            this.option = option;
            this.inObjHandle = inObjHandle;
        }

        public void init()
        {
            if (inObjHandle != null)
            {
                if (method == null)
                {
                    inObjHandle.init(option, clazz);
                } else
                {
                    inObjHandle.init(option, method);
                }
                option = null;
                method = null;
            }
        }

        public Object deal(WObject wObject, Porter porter, @NotNull Object object)
        {
            return inObjHandle == null ? object : inObjHandle.deal(wObject, porter, object);
        }

        public Object deal(WObject wObject, PorterOfFun fun, @NotNull Object object)
        {
            return inObjHandle == null ? object : inObjHandle.deal(wObject, fun, object);
        }
    }

    CLASS[] value;

    public CLASS[] getValue()
    {
        return value;
    }
}
