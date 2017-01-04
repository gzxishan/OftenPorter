package cn.xishan.oftenporter.uibinder.core;

/**
 * Created by ZhuiFeng on 2015/6/12.
 */
public class BinderSet
{
    /**
     * 控件的接口方法绑定名
     */
    public final String tiedFunName;

    /**
     * Binder属性名
     */
    public final AttrEnum attrEnum;

    /**
     * 接口中对应的参数名称
     */
    public final String paramName;
    /**
     * 值
     */
    public final Object value;

    /**
     *
     * @param tiedFunName 控件的接口方法绑定名
     * @param paramName 接口中对应的参数名称
     * @param attrEnum Binder属性名
     * @param value 值
     */
    public BinderSet(String tiedFunName, String paramName, AttrEnum attrEnum, Object value) {
        this.tiedFunName = tiedFunName;
        this.attrEnum = attrEnum;
        this.paramName = paramName;
        this.value = value;
    }


}
