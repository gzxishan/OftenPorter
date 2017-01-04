package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.UrlDecoder;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/22.
 */
public class PreRequest
{
    public Context context;
    public UrlDecoder.Result result;

    public PreRequest(Context context, UrlDecoder.Result result)
    {
        this.context = context;
        this.result = result;
    }
}
