package cn.xishan.oftenporter.porter.core.base;

/**
 * 接口绑定类型。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum TiedType
{
    REST,
    /**
     * <pre>
     *     强制REST：
     *     1）加在类上，函数都为REST；但不影响混入的接口
     *     2）加在函数上，函数为REST。
     * </pre>
     */
    FORCE_REST,
    DEFAULT;

    /**
     * <pre>
     *     1）当类和方法的类型都是{@linkplain #REST},结果才为{@linkplain #REST}
     *     2）当有一个是{@linkplain #FORCE_REST}时，结果为{@linkplain #FORCE_REST}
     * </pre>
     *
     * @param classTiedType
     * @param methodTiedType
     * @return
     */
    public static TiedType typeForFun(TiedType classTiedType, TiedType methodTiedType)
    {
        if (classTiedType == REST && methodTiedType == REST)
        {
            return REST;
        } else if (classTiedType == FORCE_REST || methodTiedType ==
                FORCE_REST)
        {
            return FORCE_REST;
        } else
        {
            return DEFAULT;
        }
    }
}
