package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
class IdGenDealt implements AutoSetGen
{
    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field,
            String option)
    {
        return IdGen.getDefault();
    }

}
