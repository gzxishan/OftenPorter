package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;

/**
 * <pre>
 *     注意：失败的对象可以是{@linkplain cn.xishan.oftenporter.porter.core.JResponse}
 * </pre>
 * Created by chenyg on 2017/1/5.
 */
public abstract class CheckHandle {
    /**
     * 返回值。
     */
    public final Object returnObj;
    /**
     * 调用异常信息。
     */
    public final Throwable exCause;
    /**
     * 当前的解析的地址。
     */
    public final UrlDecoder.Result urlResult;

    public final Object finalPorterObject;

    /**
     * 当前接口对象,整个时期都存在。
     */
    public final Object handleObject;

    /**
     * 当前接口函数,整个时期都存在。
     */
    public final Object handleMethod;

    /**
     * 存在时期同{@linkplain #handleMethod}
     */
    public final OutType outType;

    /**
     * 整个时期都不为null,其中的{@linkplain ABOption#portFunType}可能与实际的接口方法的不一致。
     */
    public final ABOption abOption;

    public CheckHandle(CheckHandle checkHandle) {
        this(checkHandle.returnObj, checkHandle.exCause, checkHandle.urlResult, checkHandle.finalPorterObject,
                checkHandle.handleObject, checkHandle.handleMethod, checkHandle.outType, checkHandle.abOption);
    }


    private CheckHandle(Object returnObj, Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject,
                        Object handleObject,
                        Object handleMethod, OutType outType, ABOption abOption) {
        if (abOption == null) {
            throw new NullPointerException("abOption is null!");
        }
        this.returnObj = returnObj;
        this.exCause = exCause;
        this.urlResult = urlResult;
        this.finalPorterObject = finalPorterObject;
        this.handleObject = handleObject;
        this.handleMethod = handleMethod;
        this.outType = outType;
        this.abOption = abOption;
    }

    public CheckHandle(UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject, Object handleMethod,
                       OutType outType, ABOption abOption) {
        this(null, null, urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
    }

    public CheckHandle(Object returnObj, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
                       Object handleMethod, OutType outType, ABOption abOption) {
        this(returnObj, null, urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
    }

    public CheckHandle(Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
                       Object handleMethod, OutType outType, ABOption abOption) {
        this(null, exCause, urlResult, finalPorterObject, handleObject, handleMethod, outType, abOption);
    }


    /**
     * 如果失败的对象是{@linkplain cn.xishan.oftenporter.porter.core.JResponse}，则直接输出该结果。
     *
     * @param failedObject 当此对象为空时表示成功，否则表示失败。
     */
    public abstract void go(Object failedObject);

    public void failed(Object failedObject) {
        if (failedObject == null) {
            throw new NullPointerException("the failedObject is null!");
        }
        go(failedObject);
    }

    public void next() {
        go(null);
    }

    /**
     * 得到接口类对应的对象。
     *
     * @return
     */
    public abstract Porter getClassPorter();

    /**
     * 得到接口方法对应的对象。
     *
     * @return
     */
    public abstract PorterOfFun getFunPorter();
}
