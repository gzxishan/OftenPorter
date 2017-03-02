package cn.xishan.oftenporter.porter.core.base;

/**
 * 输出类型。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum OutType
{
    /**
     * <pre>
     * 无错误情况下，框架不会输出,而且不会调用{@linkplain WResponse#close()}。
     * <strong>注意：</strong>当返回类型为void时，输出类型{@linkplain #NoResponse}。
     * </pre>
     */
    NoResponse,

    /**
     * <pre>
     * 输出返回值,最后会调用{@linkplain WResponse#close()}.
     * <strong>注意：</strong>当接口出现异常时，输出类型{@linkplain #Object}。
     * </pre>
     */
    Object,
    /**
     * 返回结果为null时效果同{@linkplain #NoResponse},不为null时效果同Object.
     */
    Auto
}
