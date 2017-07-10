package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.After;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.Before;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;

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
     * 只能在{@linkplain SyncPorter SyncPorter}、{@linkplain Before Before}或{@linkplain After After}中调用。
     */
    INNER;

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
