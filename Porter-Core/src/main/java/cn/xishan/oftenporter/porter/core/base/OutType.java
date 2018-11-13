package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.advanced.DefaultReturnFactory;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

/**
 * 输出类型。设置默认输出类型{@linkplain PorterConf#setDefaultPortOutType(OutType)}
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public enum OutType
{
    /**
     * <pre>
     * 无错误情况下，框架不会输出,而且不会调用{@linkplain OftenResponse#close()}。
     * <strong>注意：</strong>当返回类型为void且没有该注解时,
     * 且没有设置{@linkplain PorterConf#setDefaultPortOutType(OutType)}，输出类型{@linkplain #NO_RESPONSE}。
     * </pre>
     */
    NO_RESPONSE,

    /**
     * <pre>
     * 输出返回值,最后会调用{@linkplain OftenResponse#close()}.
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
    AUTO,
    /**
     * 当函数返回类型为Void且未抛出异常时,
     * 调用{@linkplain DefaultReturnFactory#getVoidReturn(OftenObject, Object, Object, Object) DefaultReturnFactory.getVoidReturn(WObject, Object, Object, Object)}.
     * <pre>
     *     最终效果等同于{@linkplain #AUTO}
     * </pre>
     */
    VoidReturn,
    /**
     * 当接口函数返回null
     * 时，调用{@linkplain DefaultReturnFactory#getNullReturn(OftenObject, Object, Object, Object)  DefaultReturnFactory.getNullReturn(WObject, Object, Object, Object)}
     * <pre>
     *     最终效果等同于{@linkplain #AUTO}
     * </pre
     */
    NullReturn,

    /**
     * 不抛出异常的情况下，始终返回{@linkplain JResponse#success(Object) JResponse.success(null)}
     */
    SUCCESS
}
