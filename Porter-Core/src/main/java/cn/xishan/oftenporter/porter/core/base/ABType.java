package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.PortIn.After;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.Before;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;

/**
 * 内部调用：调用接口的类型
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public enum ABType
{
    /**
     * 在主接口方法之前调用，见{@linkplain Before Before}。
     */
    METHOD_OF_BEFORE,
    /**
     * 在主接口方法后调用,见{@linkplain After After}。
     */
    METHOD_OF_AFTER,
    /**
     * 正在调用当前接口方法。
     */
    METHOD_OF_CURRENT,
    /**
     * 内部调用，如：{@linkplain SyncPorter SyncPorter}。
     */
    METHOD_OF_INNER
}
