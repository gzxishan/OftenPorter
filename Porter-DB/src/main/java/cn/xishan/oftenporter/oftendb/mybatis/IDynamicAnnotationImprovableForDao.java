package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class IDynamicAnnotationImprovableForDao implements IDynamicAnnotationImprovable
{
    @Override
    public Result<InvocationHandler, AspectOperationOfNormal> getAspectOperationOfNormal(Annotation annotation)
    {
        return null;
    }

    @Override
    public Result<InvocationHandler, AspectOperationOfPortIn> getAspectOperationOfPortIn(Annotation annotation)
    {
        return null;
    }

    @Override
    public Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz)
    {
        MyBatisMapper myBatisMapper = AnnoUtil.getAnnotation(clazz, MyBatisMapper.class);
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
