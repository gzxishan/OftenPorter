package cn.xishan.oftenporter.porter.core.base;

/**
 * 接口绑定类型,见{@linkplain #typeForFun(TiedType, TiedType)}。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum TiedType
{
    /**
     * 通过请求方法绑定进行绑定。
     */
    METHOD,
    /**
     * <pre>
     *     强制METHOD：
     *     1）加在类上，函数都为METHOD；但不影响混入的接口
     *     2）加在函数上，函数为METHOD。
     * </pre>
     */
    FORCE_METHOD,
    /**
     * 通过方法绑定名进行绑定。
     */
    DEFAULT;


    public boolean isRest(){
        return this==FORCE_METHOD||this==METHOD;
    }
    /**
     * <pre>
     *     1）当类和方法的类型都是{@linkplain #METHOD},结果才为{@linkplain #METHOD}
     *     2）当有一个是{@linkplain #FORCE_METHOD}时，结果为{@linkplain #FORCE_METHOD}
     * </pre>
     *
     * @param classTiedType
     * @param methodTiedType
     * @return
     */
    public static TiedType typeForFun(TiedType classTiedType, TiedType methodTiedType)
    {
        if (classTiedType == METHOD && methodTiedType == METHOD)
        {
            return METHOD;
        } else if (classTiedType == FORCE_METHOD || methodTiedType ==
                FORCE_METHOD)
        {
            return FORCE_METHOD;
        } else
        {
            return DEFAULT;
        }
    }
}
