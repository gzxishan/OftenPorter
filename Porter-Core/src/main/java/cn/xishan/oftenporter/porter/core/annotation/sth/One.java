package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj.JsonObj;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortInObj;
import cn.xishan.oftenporter.porter.core.base.InNames;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class One
{
    public final Class<?> clazz;
    private _PortInObj.CLASS inObjClazz;
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

    public _PortInObj.CLASS getInObjClazz()
    {
        return inObjClazz;
    }

    public void setInObjClazz(_PortInObj.CLASS inObjClazz)
    {
        this.inObjClazz = inObjClazz;
    }
}