package cn.xishan.oftenporter.porter.core.base;

import java.util.Enumeration;
import java.util.Map;

/**
 * 用于获取参数的源。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamSource
{
    /**
     * 根据名称得到参数,路径参数优先。
     *
     * @param name 参数名称
     * @return 返回参数值
     */
    <T>T getParam(String name);

    void putNewParams(Map<String, ?> newParams);


    /**
     * 获取所有的参数名称，包括地址兰和请求的。
     *
     * @return
     */
    Enumeration<String> paramNames();

    /**
     * 获取所有的参数，优先使用地址参数（如果存在）。
     *
     * @return
     */
    Enumeration<Map.Entry<String, Object>> params();
}
