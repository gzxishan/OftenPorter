package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;

import java.lang.reflect.Method;

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
     * @see Porter#getFinalPorterObject()
     */
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


    public CheckHandle(CheckHandle checkHandle)
    {
        this(checkHandle.returnObj, checkHandle.exCause, checkHandle.urlResult, checkHandle.finalPorterObject,
                checkHandle.handleObject, checkHandle.handleMethod, checkHandle.outType);
    }


    private CheckHandle(Object returnObj, Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject,
            Object handleObject,
            Object handleMethod, OutType outType)
    {

        this.returnObj = returnObj;
        this.exCause = exCause;
        this.urlResult = urlResult;
        this.finalPorterObject = finalPorterObject;
        this.handleObject = handleObject;
        this.handleMethod = handleMethod;
        this.outType = outType;
    }

    public CheckHandle(UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject, Object handleMethod,
            OutType outType)
    {
        this(null, null, urlResult, finalPorterObject, handleObject, handleMethod, outType);
    }

    public CheckHandle(Object returnObj, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
            Object handleMethod, OutType outType)
    {
        this(returnObj, null, urlResult, finalPorterObject, handleObject, handleMethod, outType);
    }

    public CheckHandle(Throwable exCause, UrlDecoder.Result urlResult, Object finalPorterObject, Object handleObject,
            Object handleMethod, OutType outType)
    {
        this(null, exCause, urlResult, finalPorterObject, handleObject, handleMethod, outType);
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


    /**
     * 判断当前所有的注解是否全部在最终对象上或全部在函数上注解了。
     *
     * @param annotationClasses
     * @return
     */
    public static boolean isAllPresentOnClassOrFun(Object obj,Method method,Class<?>... annotationClasses)
    {
        if (AnnoUtil.isAllOfAnnotationsPresent(PortUtil.getRealClass(obj), annotationClasses) || AnnoUtil
                .isAllOfAnnotationsPresent(method, annotationClasses))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 判断当前所有的注解是否全部在最终对象上或全部在函数上注解了。
     *
     * @param annotationClasses
     * @return
     */
    public boolean isAllPresentOnClassOrFun(Class<?>... annotationClasses)
    {
        if (AnnoUtil.isAllOfAnnotationsPresent(PortUtil.getRealClass(finalPorterObject), annotationClasses) || AnnoUtil
                .isAllOfAnnotationsPresent((Method) handleMethod, annotationClasses))
        {
            return true;
        } else
        {
            return false;
        }
    }


    /**
     * 判断是否存在一个注解在类或函数上
     *
     * @param annotationClasses
     * @return
     */
    public static boolean isOneOfPresentOnClassOrFun(Object obj, Method method, Class<?>... annotationClasses)
    {
        if (AnnoUtil.isOneOfAnnotationsPresent(PortUtil.getRealClass(obj), annotationClasses) || AnnoUtil
                .isOneOfAnnotationsPresent(method, annotationClasses))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 判断是否存在一个注解在类或函数上
     *
     * @param annotationClasses
     * @return
     */
    public boolean isOneOfPresentOnClassOrFun(Class<?>... annotationClasses)
    {
        if (AnnoUtil.isOneOfAnnotationsPresent(PortUtil.getRealClass(finalPorterObject), annotationClasses) || AnnoUtil
                .isOneOfAnnotationsPresent((Method) handleMethod, annotationClasses))
        {
            return true;
        } else
        {
            return false;
        }
    }
}
