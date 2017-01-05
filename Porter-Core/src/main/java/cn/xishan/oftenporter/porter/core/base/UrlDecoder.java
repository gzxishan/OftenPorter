package cn.xishan.oftenporter.porter.core.base;


/**
 * 用于地址处理。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface UrlDecoder
{
    public interface Result extends ParamSource
    {
        String contextName();
        String classTied();
        String funTied();

        /**
         * 框架内部处理参数时，获取参数值，优是先从地址参数获取。
         * @param name 设置的参数名称
         * @param value 参数值
         */
        void setParam(String name,Object value);
    }

    Result decode(String path);
}
