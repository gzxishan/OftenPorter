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
     * <strong>注意：</strong>当返回类型为void且没有改注解时，输出类型{@linkplain #NO_RESPONSE}。
     * </pre>
     */
    NO_RESPONSE,

    /**
     * <pre>
     * 输出返回值,最后会调用{@linkplain WResponse#close()}.
     * </pre>
     */
    OBJECT,
    /**
     * 始终调用close。
     */
    CLOSE,
    /**
     * 返回结果为null时效果同{@linkplain #NO_RESPONSE},不为null时效果同Object.
     */
    AUTO
}
