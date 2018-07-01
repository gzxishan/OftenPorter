package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * 用于设置{@linkplain AspectOperationOfPortIn}修饰的注解对应的处理器被添加的位置,见{@linkplain PortIn#aspectOfClassPosition()}。
 *
 * @author Created by https://github.com/CLovinr on 2018-04-05.
 */
public enum AspectPosition
{
    /**
     * 置于之前
     */
    BEFORE,
    /**
     * 置于之后
     */
    AFTER,
    /**
     * 忽略
     */
    IGNORE
}
