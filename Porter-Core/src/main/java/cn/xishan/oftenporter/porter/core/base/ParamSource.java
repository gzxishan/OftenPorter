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
     * 根据名称得到参数
     *
     * @param name 参数名称
     * @return 返回参数值
     */
    Object getParam(String name);

    void putNewParams(Map<String, ?> newParams);

    Enumeration<String> paramNames();
}
