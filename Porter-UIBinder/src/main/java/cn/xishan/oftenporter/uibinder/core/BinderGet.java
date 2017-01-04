package cn.xishan.oftenporter.uibinder.core;

/**
 * Created by ZhuiFeng on 2015/7/24.
 */
public class BinderGet
{
    /**
     * 控件的接口方法绑定名
     */
    public final String tiedFunName;

    /**
     * 接口中对应的参数名称
     */
    public final String paramName;

    public final AttrEnum varType;

    /**
     * @param tiedFunName 控件的接口方法绑定名
     * @param paramName   接口中对应的参数名称
     */
    public BinderGet(String tiedFunName, String paramName, AttrEnum varType)
    {
        this.tiedFunName = tiedFunName;
        this.paramName = paramName;
        this.varType = varType;
    }
}
