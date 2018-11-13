package cn.xishan.oftenporter.porter.core.advanced;


import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import java.lang.reflect.Method;

/**
 *
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamSourceHandle
{
    /**
     * 自定义的可以返回null，此时会转为默认的。
     * @param oftenObject 注意：此时的wObject.{@linkplain OftenObject#getParamSource() getParamSource()}返回null,各种参数也没有处理;
     * @return
     */
    ParamSource get(OftenObject oftenObject,Class<?> porterClass,Method porterFun)throws Exception;
}
