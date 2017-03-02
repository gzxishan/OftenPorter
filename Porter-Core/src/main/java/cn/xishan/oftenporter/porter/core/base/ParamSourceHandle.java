package cn.xishan.oftenporter.porter.core.base;


import java.lang.reflect.Method;

/**
 *
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamSourceHandle
{
    /**
     * 自定义的可以返回null，此时会转为默认的。
     * @param wObject 注意：此时的wObject.{@linkplain WObject#getParamSource() getParamSource()}返回null,各种参数也没有处理;
     * @return
     */
    ParamSource get(WObject wObject,Class<?> porterClass,Method porterFun)throws Exception;
}
