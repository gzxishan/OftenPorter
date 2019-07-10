package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * 用于获取参数的源。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamSource
{

    void setUrlResult(UrlDecoder.Result result);

    /**
     * 根据名称得到参数,路径参数优先。
     *
     * @param name 参数名称
     * @return 返回参数值, 可能为null(不会返回空字符串)。
     */
    <T> T getParam(String name);

    /**
     * 根据名称得到参数,路径参数优先。
     *
     * @param name          参数名称
     * @param errmsgOfEmpty 为空时抛出异常的错误信息
     * @return 返回参数值, 不为null且字符串不为""。
     */
    <T> T getNeceParam(String name, String errmsgOfEmpty);

    /**
     * 见{@linkplain #getNeceParam(String, String)},使用默认的错误信息。
     */
    <T> T getNeceParam(String name);

    void putNewParams(Map<String, ?> newParams);


    /**
     * 获取所有的参数名称，包括地址兰和请求的。
     *
     * @return
     */
    Enumeration<String> paramNames();

    /**
     * 获取所有的参数，优先使用地址参数（如果存在）。
     *
     * @return
     */
    Enumeration<Map.Entry<String, Object>> params();


    public static ParamSource fromMap(Map<String, Object> params)
    {
        Map<String, Object> finalParams = Collections.unmodifiableMap(params);
        ParamSource paramSource = new ParamSource()
        {
            @Override
            public void setUrlResult(UrlDecoder.Result result)
            {
                throw new OftenCallException("not support!");
            }

            @Override
            public <T> T getParam(String name)
            {
                T t = (T) finalParams.get(name);
                if (OftenTool.isNullOrEmptyCharSequence(t))
                {
                    t = null;
                }
                return t;
            }

            @Override
            public <T> T getNeceParam(String name, String errmsgOfEmpty)
            {
                return DefaultParamSource.getNeceParamUtil(this, name, errmsgOfEmpty);
            }

            @Override
            public <T> T getNeceParam(String name)
            {
                return DefaultParamSource.getNeceParamUtil(this, name);
            }

            @Override
            public void putNewParams(Map<String, ?> newParams)
            {
                throw new OftenCallException("not support!");
            }

            @Override
            public Enumeration<String> paramNames()
            {
                return new EnumerationImpl<String>(finalParams.keySet());
            }

            @Override
            public Enumeration<Map.Entry<String, Object>> params()
            {
                return new EnumerationImpl<Map.Entry<String, Object>>(finalParams.entrySet());
            }
        };
        return paramSource;
    }
}
