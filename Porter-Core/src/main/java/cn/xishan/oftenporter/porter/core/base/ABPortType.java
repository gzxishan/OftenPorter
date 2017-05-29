package cn.xishan.oftenporter.porter.core.base;

/**
 * 调用接口的时期.
 * Created by chenyg on 2017-04-25.
 */
public enum ABPortType
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
     * 调用最后一个方法
     */
    FINAL_LAST,
    /**
     * 只有一个方法的情况。
     */
    BOTH_FIRST_LAST
}
