package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-26.
 */
public interface AutoSetDealt
{
    /**
     * @param finalObject        目前对于接口，该对象为最终接口实例，例如把接口object混入到finalObject中去的情况；其他的情况，等于object。
     * @param currentObjectClass
     * @param currentObject      设置静态字段时为null。
     * @param field
     * @param fieldValue
     * @param option
     * @return
     */
    Object deal(@MayNull("设置静态字段时为null") Object finalObject, Class<?> currentObjectClass,
            @MayNull("设置静态字段时为null") Object currentObject,
            Field field,
            @MayNull Object fieldValue, String option);
}
