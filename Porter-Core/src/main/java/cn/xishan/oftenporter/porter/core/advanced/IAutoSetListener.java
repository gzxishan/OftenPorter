package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._AutoSet;

import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2018/8/28.
 */
public interface IAutoSetListener
{
    /**
     * @param autoSet       对应与{@linkplain AutoSet}注解
     * @param currentClass  当前类
     * @param currentObject 当前实例，可能为null。
     * @param field         成员变量
     * @param realFieldType 变量的真实类型
     * @param value         被设置的值
     * @param willSet       是否会被设置
     * @return true表示进行设置，false表示不进行设置（有一个返回false则不会进行设置,会继续执行后面的监听对象）。
     */
    boolean willSet(_AutoSet autoSet, Class currentClass, @MayNull Object currentObject, Field field,
            Class realFieldType, @NotNull Object value, boolean willSet);
}
