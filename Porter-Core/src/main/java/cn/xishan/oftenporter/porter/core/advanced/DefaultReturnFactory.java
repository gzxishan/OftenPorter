package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * Created by https://github.com/CLovinr on 2017/8/8.
 */
public interface DefaultReturnFactory
{
    Object getVoidReturn(WObject wObject, Object finalPorterObject, Object handleObject,
            Object handleMethod) throws Exception;

    Object getNullReturn(WObject wObject, Object finalPorterObject, Object handleObject,
            Object handleMethod) throws Exception;

    Object getExReturn(WObject wObject, Object finalPorterObject, Object handleObject, Object handleMethod,
            Throwable throwable) throws Throwable;
}
