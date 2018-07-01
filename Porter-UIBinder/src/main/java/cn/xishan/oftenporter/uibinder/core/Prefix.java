package cn.xishan.oftenporter.uibinder.core;


import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;

/**
 * Created by ZhuiFeng on 2015/6/13.
 */
public class Prefix
{


    /**
     * id名称前缀
     */
    public final String idPrefix;

    /**
     * 接口路径前缀
     */
    public final String pathPrefix;

    public final ErrListener errListener;

    /**
     * 绑定完成后用于调用的接口,只调用一次。
     */
    private String callbackMethod;

    private AppValues callbackValues;


    @Override
    public String toString()
    {
        return idPrefix + "" + pathPrefix + ",callback=" + callbackMethod;
    }

    public static Prefix forDelete(String contextName, Class<?> clazz, boolean enableDefaultValue)
    {
        return new Prefix(null, "/" + contextName + "/" + classTied(clazz, enableDefaultValue) + "/", null);
    }

    public static Prefix forDelete(String porterPrefix)
    {
        return new Prefix(null, porterPrefix, null);
    }

    /**
     * @param idPrefix    代表id的内容前缀
     * @param pathPrefix  调用的接口路径前缀
     * @param errListener 错误监听器
     */
    public Prefix(String idPrefix, String pathPrefix, ErrListener errListener)
    {
        this.idPrefix = idPrefix;
        this.pathPrefix = pathPrefix;
        this.errListener = errListener;
    }

    public String getCallbackMethod()
    {
        return callbackMethod;
    }

    public void setCallbackMethod(String callbackMethod)
    {
        this.callbackMethod = callbackMethod;
    }

    public AppValues getCallbackValues()
    {
        return callbackValues;
    }

    public void setCallbackValues(AppValues callbackValues)
    {
        this.callbackValues = callbackValues;
    }

    private static String classTied(Class<?> clazz, boolean enableDefaultValue)
    {
        PortIn portIn = AnnoUtil.getAnnotation(clazz,PortIn.class);
        if (portIn == null)
        {
            throw new RuntimeException(
                    "class [" + clazz.getName() + "] not with annotation of @" + PortIn.class.getName());
        }
        String tied = PortUtil.tied(portIn, clazz, enableDefaultValue);
        return tied;
    }

    /**
     * 规则为:
     * <pre>
     *     绑定名为："TiedName"
     *     1.idPrefix:"tiedName_"
     *     2.pathPrefix:"/contextName/TiedName/"
     * </pre>
     *
     * @param c                  接口类
     * @param enableDefaultValue 是否允许类的默认绑定名。
     * @return 返回构造的对象
     */
    public static Prefix buildPrefix(String contextName, Class<?> c, boolean enableDefaultValue)
    {
        String tied = classTied(c, enableDefaultValue);
        Prefix prefix = new Prefix(
                tied.substring(0, 1).toLowerCase() + tied.substring(1) + "_",
                "/" + contextName + "/" + tied + "/", null);
        return prefix;
    }


}
