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
    public class Will
    {
        public boolean willSet;
        /**
         * 可选的设置对象
         */
        public Object optionValue;

        public Will(boolean willSet)
        {
            this.willSet = willSet;
        }
    }

    /**
     * @param autoSet       对应与{@linkplain AutoSet}注解
     * @param currentClass  当前类
     * @param currentObject 当前实例，可能为null。
     * @param field         成员变量
     * @param realFieldType 变量的真实类型
     * @param lastWill
     * @return null或Will.willSet为true表示进行设置，否则表示不进行设置（有一个不设置则最终不会进行设置,会继续执行后面的监听对象）。
     */
    Will willSet(_AutoSet autoSet, Class currentClass, @MayNull Object currentObject, Field field,
            Class realFieldType, @NotNull Will lastWill)throws Exception;
}
