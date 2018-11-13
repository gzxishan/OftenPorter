package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _BindEntities
{
    public static class CLASS
    {
        public final Class<?> clazz;
        Method method;
        String option;
        public final BindEntityDealt.IHandle bindEntityDealtHandle;

        public CLASS(Class<?> clazz)
        {
            this.clazz = clazz;
            this.bindEntityDealtHandle = null;
        }

        public CLASS(Class<?> clazz, Method method, String option, BindEntityDealt.IHandle bindEntityDealtHandle)
        {
            this.clazz = clazz;
            this.method = method;
            this.option = option;
            this.bindEntityDealtHandle = bindEntityDealtHandle;
        }

        public void init() throws Exception
        {
            if (bindEntityDealtHandle != null)
            {
                if (method == null)
                {
                    bindEntityDealtHandle.init(option, clazz);
                } else
                {
                    bindEntityDealtHandle.init(option, method);
                }
                option = null;
                method = null;
            }
        }

        @Override
        public int hashCode()
        {
            return clazz.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof CLASS)
            {
                CLASS clazz = (CLASS) obj;
                if (method != null)
                {
                    return method.equals(clazz.method);
                } else
                {
                    return this.clazz.equals(clazz.clazz);
                }
            } else
            {
                return false;
            }
        }

        public Object deal(OftenObject oftenObject, Porter porter, @NotNull Object object) throws Exception
        {
            return bindEntityDealtHandle == null ? object : bindEntityDealtHandle.deal(oftenObject, porter, object);
        }

        public Object deal(OftenObject oftenObject, PorterOfFun fun, @NotNull Object object) throws Exception
        {
            return bindEntityDealtHandle == null ? object : bindEntityDealtHandle.deal(oftenObject, fun, object);
        }
    }

    CLASS[] value;

    public CLASS[] getValue()
    {
        return value;
    }
}
