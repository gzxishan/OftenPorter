package cn.xishan.oftenporter.porter.core.pbridge;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/19.
 */
public interface Delivery
{
    /**
     * 可以访问当前实例的。请求格式为/contextName/ClassTied/[funTied|restValue][?name1=value1&name2=value2...]
     *
     * @return
     */
    PBridge currentBridge();

    /**
     * 可以访问到所有可达的框架实例。请求路径必须是fullPath（请求格式为":pname/contextName/ClassTied/[funTied|restValue][?name1=value1&name2
     * =value2...]"），即加上pname。
     *
     * @return
     */
    PBridge toAllBridge();

    /**
     * 当前实例名称。
     *
     * @return
     */
    PName currentPName();
}
