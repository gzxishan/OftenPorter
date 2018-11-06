package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._BindEntities;
import cn.xishan.oftenporter.porter.core.annotation.deal._BindEntities.CLASS;
import cn.xishan.oftenporter.porter.core.annotation.param.JsonObj;
import cn.xishan.oftenporter.porter.core.base.InNames;

import java.lang.reflect.Field;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class One
{
    /**
     * 等于{@linkplain CLASS#clazz}
     */
    public final Class<?> clazz;
    private CLASS entityClazz;
    /**
     * {@linkplain InNames#inner}无效。
     */
    public final InNames inNames;
    public final Field[] neceObjFields, unneceObjFields;

    /**
     * 使用{@linkplain JsonObj}注解的类变量。
     */
    public final Field[] jsonObjFields;
    public final String[] jsonObjVarnames;
    public final One[] jsonObjOnes;

    public One(Class<?> clazz, InNames inNames, Field[] neceObjFields, Field[] unneceObjFields, Field[] jsonObjFields,
            One[] jsonObjOnes, String[] jsonObjVarnames)
    {
        this.clazz = clazz;
        this.inNames = inNames;
        this.neceObjFields = neceObjFields;
        this.unneceObjFields = unneceObjFields;
        this.jsonObjFields = jsonObjFields;
        this.jsonObjOnes = jsonObjOnes;
        this.jsonObjVarnames = jsonObjVarnames;
    }

    public CLASS getEntityClazz()
    {
        return entityClazz;
    }

    public void setEntityClazz(CLASS entityClazz)
    {
        this.entityClazz = entityClazz;
    }
}