package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * Created by https://github.com/CLovinr on 2017/8/8.
 */
public interface DefaultReturnFactory
{
    Object getVoidReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject,
            Object handleMethod) throws Exception;

    Object getNullReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject,
            Object handleMethod) throws Exception;

    Object getExReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject, Object handleMethod,
            Throwable throwable) throws Throwable;
}
