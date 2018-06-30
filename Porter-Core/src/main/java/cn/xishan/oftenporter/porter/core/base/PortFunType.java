package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.SyncNotInnerPorter;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorterThrows;

/**
 * 用于与{@linkplain PortIn PortIn}进行配合。
 * Created by chenyg on 2017-04-17.
 */
public enum PortFunType
{
    /**
     * 默认
     */
    DEFAULT,
    /**
     * <p>
     * 只能在{@linkplain SyncPorter SyncPorter}、{@linkplain SyncPorterThrows SyncPorterThrows
     * }、{@linkplain SyncNotInnerPorter SyncNotInnerPorter}、
     * {@linkplain Delivery#currentBridge() Delivery.currentBridge()}、{@linkplain Delivery#innerBridge() Delivery.innerBridge()}中调用。
     *
     * </p>
     */
    INNER,

    /**
     * <p>
     * 只能在{@linkplain SyncPorter SyncPorter}、{@linkplain SyncNotInnerPorter SyncNotInnerPorter}、
     * {@linkplain Delivery#currentBridge() Delivery.currentBridge()}、{@linkplain Delivery#innerBridge() Delivery.innerBridge()}中调用，且不会进行类参数处理、检测处理等，找到绑定后直接调用。
     * </p>
     * <p>
     * <strong>注意：</strong>
     * <ol>
     * <li>会解析函数上的参数（如{@linkplain PortIn#nece()}、{@linkplain PortInObj}等）,但类上的参数解析会被忽略。</li>
     * <li>依然会处理{@linkplain AspectOperationOfPortIn}</li>
     * </ol>
     * </p>
     */
    FAST_INNER;

    /**
     * 当函数的为{@linkplain #DEFAULT}时，返回类的；否则返回函数自己的类型。
     *
     * @param classTiedType
     * @param methodTiedType
     * @return
     */
    public static PortFunType type(PortFunType classTiedType, PortFunType methodTiedType)
    {
        if (methodTiedType == DEFAULT)
        {
            return classTiedType;
        } else
        {
            return methodTiedType;
        }
    }
}
