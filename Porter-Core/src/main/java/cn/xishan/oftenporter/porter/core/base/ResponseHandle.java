package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/10.
 */
public interface ResponseHandle
{
    /**
     * 访问的接口不存在时（注意，如果接口内部返回404响应，并不会调用该回调）。
     *
     * @param request
     * @param response
     * @param jResponse
     * @return
     */
    Object toResponseOf404(@NotNull WRequest request, @NotNull WResponse response, @NotNull JResponse jResponse);

    /**
     * @param wObject
     * @param finalPorterObject 见{@linkplain Porter#getFinalPorterObject()}
     * @param porterObject
     * @param porterMethod
     * @param object
     * @return
     */
    Object toResponse(@NotNull WObject wObject, @NotNull Object finalPorterObject, @NotNull Object porterObject,
            @NotNull
                    Method porterMethod, @NotNull Object object);
}
