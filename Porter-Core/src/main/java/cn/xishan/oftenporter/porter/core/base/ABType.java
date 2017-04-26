package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/24.
 */
public enum ABType
{
    /**
     * 在接口方法之前调用。
     */
    METHOD_OF_BEFORE,
    /**
     * 在接口方法后调用。
     */
    METHOD_OF_AFTER,
    /**
     * 正在调用当前接口方法。
     */
    METHOD_OF_CURRENT,
    /**
     * 接口方法内部调用。
     */
    METHOD_OF_INNER
}
