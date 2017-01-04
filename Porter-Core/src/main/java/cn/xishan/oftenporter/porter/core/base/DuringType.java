package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public enum DuringType {
    /**
     * 没有初始化任何参数。
     */
    ON_GLOBAL,

    /**
     * 没有初始化任何参数。
     */
    ON_CONTEXT_GLOBAL,
    /**
     * 此时类参数已经准备完成。
     */
    ON_CLASS,
    /**
     * 在执行接口函数前，此时函数参数已经准备完成。
     */
    ON_METHOD,

    /**
     * 在执行接口函数后,{@linkplain CheckPassable#willPass(WObject, DuringType, Aspect)}的{@linkplain Aspect#returnObj}为返回值。
     */
    AFTER_METHOD,
    /**
     * 在执行接口函数抛出了异常时,{@linkplain CheckPassable#willPass(WObject, DuringType, Aspect)}的{@linkplain Aspect#invokeCause}为异常信息。
     */
    ON_METHOD_EXCEPTION
}
