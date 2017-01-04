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
     * 输出返回值，返回结果为null或返回类型为void则不会输出内容,最后会调用{@linkplain WResponse#close()}。
     */
    Object
}
