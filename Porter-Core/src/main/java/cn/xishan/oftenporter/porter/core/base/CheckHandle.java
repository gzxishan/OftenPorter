package cn.xishan.oftenporter.porter.core.base;

/**
 * <pre>
 *     注意：失败的对象可以是{@linkplain cn.xishan.oftenporter.porter.core.JResponse}
 * </pre>
 * Created by chenyg on 2017/1/5.
 */
public abstract class CheckHandle
{
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

    /**
     * 当前接口对象,整个时期都存在。
     */
    public final Object handleObject;

    /**
     * 当前接口函数,从{@linkplain DuringType#ON_METHOD}时期开始存在。
     */
    public final Object handleMethod;

    /**
     * 存在时期同{@linkplain #handleMethod}
     */
    public final OutType outType;

    public CheckHandle(CheckHandle checkHandle)
    {
        this.returnObj = checkHandle.returnObj;
        this.exCause = checkHandle.exCause;
        this.urlResult = checkHandle.urlResult;
        this.handleObject = checkHandle.handleObject;
        this.handleMethod = checkHandle.handleMethod;
        this.outType=checkHandle.outType;
    }


    public CheckHandle(UrlDecoder.Result urlResult, Object handleObject)
    {
        this(null, null, urlResult, handleObject, null,null);
    }

    private CheckHandle(Object returnObj, Throwable exCause, UrlDecoder.Result urlResult, Object handleObject,
            Object handleMethod,OutType outType)
    {
        this.returnObj = returnObj;
        this.exCause = exCause;
        this.urlResult = urlResult;
        this.handleObject = handleObject;
        this.handleMethod = handleMethod;
        this.outType=outType;
    }

    public CheckHandle(UrlDecoder.Result urlResult, Object handleObject, Object handleMethod,OutType outType)
    {
        this(null, null, urlResult, handleObject, handleMethod,outType);
    }

    public CheckHandle(Object returnObj, UrlDecoder.Result urlResult, Object handleObject, Object handleMethod,OutType outType)
    {
        this(returnObj, null, urlResult, handleObject, handleMethod,outType);
    }

    public CheckHandle(Throwable exCause, UrlDecoder.Result urlResult, Object handleObject, Object handleMethod,OutType outType)
    {
        this(null, exCause, urlResult, handleObject, handleMethod,outType);
    }


    /**
     * 如果失败的对象是{@linkplain cn.xishan.oftenporter.porter.core.JResponse}，则直接输出该结果。
     *
     * @param failedObject 当此对象为空时表示成功，否则表示失败。
     */
    public abstract void go(Object failedObject);

    public void failed(Object failedObject)
    {
        if (failedObject == null)
        {
            throw new NullPointerException("the failedObject is null!");
        }
        go(failedObject);
    }

    public void next()
    {
        go(null);
    }
}
