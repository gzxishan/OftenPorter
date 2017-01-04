package cn.xishan.oftenporter.porter.core.init;

/**
 * 初始化配置源。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface InitParamSource
{
    /**
     * 得到初始化的参数值
     *
     * @param name
     * @return
     */
    Object getInitParameter(String name);

    void putInitParameter(String name, Object value);
}
