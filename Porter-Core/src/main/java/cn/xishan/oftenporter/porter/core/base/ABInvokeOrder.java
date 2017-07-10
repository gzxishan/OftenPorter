package cn.xishan.oftenporter.porter.core.base;

/**
 * 调用接口的顺序.
 * Created by chenyg on 2017-04-25.
 */
public enum ABInvokeOrder
{
    /**
     * 第一个接口方法
     */
    ORIGIN_FIRST,
    /**
     * 调用中间方法
     */
    OTHER,

    /**
     * 用于寻找第一个方法,同时会寻找最后一个
     */
    _OTHER_BEFORE,

    /**
     * 用于寻找最后一个方法
     */
    _OTHER_AFTER,

    /**
     * 调用最后一个方法
     */
    FINAL_LAST,
    /**
     * 只有一个方法的情况。
     */
    BOTH_FIRST_LAST
}
