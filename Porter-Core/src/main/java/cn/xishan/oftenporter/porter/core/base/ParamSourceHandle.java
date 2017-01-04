package cn.xishan.oftenporter.porter.core.base;


/**
 *
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public interface ParamSourceHandle
{

    ParamSource get(WRequest request, UrlDecoder.Result result);
}
