package cn.xishan.oftenporter.porter.core.base;

/**
 * 接口绑定类型。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum TiedType
{
    REST,
    /**
     * 强制REST
     */
    FORCE_REST,
    DEFAULT;

    /**
     * 当类和方法的类型都是{@linkplain #REST}或有一个是{@linkplain #FORCE_REST}时，结果才为{@linkplain #REST}。
     * @param classTiedType
     * @param methodTiedType
     * @return
     */
    public static TiedType type(TiedType classTiedType, TiedType methodTiedType)
    {
        if (classTiedType == REST && methodTiedType == REST||classTiedType==FORCE_REST||methodTiedType==FORCE_REST)
        {
            return REST;
        } else
        {
            return DEFAULT;
        }
    }
}
