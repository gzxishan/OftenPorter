package cn.xishan.oftenporter.porter.core.base;

/**
 * 输出类型。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum OutType
{
    /**
     * 无错误情况下，框架不会输出,而且不会调用{@linkplain WResponse#close()}。
     */
    NoResponse,

    /**
     * 当返回结果不为null时，输出返回值,最后会调用{@linkplain WResponse#close()}；当返回结果为null或返回类型为void时，则不会输出任何内容，也不会关闭连接。
     */
    Object
}
