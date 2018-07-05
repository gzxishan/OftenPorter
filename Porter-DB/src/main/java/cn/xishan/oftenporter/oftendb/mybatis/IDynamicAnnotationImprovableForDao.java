package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class IDynamicAnnotationImprovableForDao extends IDynamicAnnotationImprovable.Adapter
{

    @Override
    public Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz)
    {
        if (!Modifier.isInterface(clazz.getModifiers()))
        {
            return null;
        }
        MyBatisMapper myBatisMapper = AnnoUtil.Advanced.getAnnotation(clazz, MyBatisMapper.class);
        if (myBatisMapper != null)
        {
            InvocationHandler invocationHandler = (proxy, method, args) -> {
                if (method.getName().equals("gen"))
                {
                    return MyBatisDaoGen.class;
                } else
                {
                    return method.getDefaultValue();
                }
            };
            return new Result<>(invocationHandler, AutoSetDefaultDealt.class);
        }
        return null;
    }
}
