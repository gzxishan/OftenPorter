package cn.xishan.oftenporter.oftendb.mybatis;

import cn.xishan.oftenporter.oftendb.annotation.MyBatisMapper;
import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class IDynamicAnnotationImprovableForDao extends IDynamicAnnotationImprovable.Adapter
{

    @Override
    public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Class<?> clazz, Class<A> annotationType)
    {
        if (!Modifier.isInterface(clazz.getModifiers()) || !annotationType.equals(AutoSetDefaultDealt.class))
        {
            return null;
        }
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
            Result result = new Result<>(invocationHandler, annotationType);
            return result;
        }
        return null;
    }


    @Override
    public Set<String> supportClassNames()
    {
        Set<String> set = new HashSet<>();
        set.add(AutoSetDefaultDealt.class.getName());
        return set;
    }

    @Override
    public boolean supportPorter()
    {
        return false;
    }

    @Override
    public boolean supportAspect()
    {
        return false;
    }
}
