package cn.xishan.oftenporter.porter.core.base;

/**
 * Created by https://github.com/CLovinr on 2017/8/8.
 */
public interface DefaultReturnFactory {
    Object getVoidReturn(WObject wObject, Object finalPorterObject, Object handleObject, Object handleMethod);

    Object getNullReturn(WObject wObject, Object finalPorterObject, Object handleObject, Object handleMethod);
}
