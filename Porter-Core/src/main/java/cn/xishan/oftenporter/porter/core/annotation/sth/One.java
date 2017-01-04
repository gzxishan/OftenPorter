package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.base.InNames;

import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class One
{
    public final Class<?> clazz;
    /**
     * {@linkplain InNames#inner}无效。
     */
    public final InNames inNames;
    public final Field[] neceObjFields, unneceObjFields;

    public One(Class<?> clazz, InNames inNames, Field[] neceObjFields, Field[] unneceObjFields)
    {
        this.clazz = clazz;
        this.inNames = inNames;
        this.neceObjFields = neceObjFields;
        this.unneceObjFields = unneceObjFields;
    }

}