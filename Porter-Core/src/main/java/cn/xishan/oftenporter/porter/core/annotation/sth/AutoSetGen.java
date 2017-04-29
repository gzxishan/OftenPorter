package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetMixin;

import java.lang.reflect.Field;

/**
 * 用于生成注入的对象。只有找不到对应的注入实例时，才会调用,且生成的对象不会被放入map中(即不会被注入到其他变量中,但除了@{@linkplain AutoSetMixin AutoSetMixin})。
 * Created by chenyg on 2017-04-29.
 */
public interface AutoSetGen
{
    Object genObject(Object object, Field field, String option);
}
