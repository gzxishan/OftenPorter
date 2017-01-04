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
    }

    Result decode(String path);
}
