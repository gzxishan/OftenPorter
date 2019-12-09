package cn.xishan.oftenporter.porter.core.base;

/**
 * 请求方法。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum PortMethod
{
    POST, GET, OPTIONS, HEAD, PUT, DELETE, TARCE,
    /**
     * 表示默认类型。
     */
    DEFAULT;

    /**
     * 判断是否为给定中的一个
     *
     * @param methods
     * @return
     */
    public boolean isOneOf(PortMethod... methods)
    {
        for (PortMethod method : methods)
        {
            if (method == this)
            {
                return true;
            }
        }
        return false;
    }
}
