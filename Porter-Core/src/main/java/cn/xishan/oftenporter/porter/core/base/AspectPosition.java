package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;

/**
 * 用于设置{@linkplain AspectFunOperation}修饰的注解存放的位置。
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
