package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.After;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.Before;

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
     * 只能在{@linkplain After After}或{@linkplain Before Before}中调用。
     */
    PORT_AFTER_BEFORE
}
