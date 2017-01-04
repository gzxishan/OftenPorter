package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.core.util.WPTool;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
class PResponseImpl extends PResponse
{
    protected PResponseImpl(Object object)
    {
        super(object);
    }

    static PResponse exception(ResultCode code, Throwable e)
    {
        JResponse jResponse = new JResponse(code);
        jResponse.setDescription(WPTool.getMessage(e));
        return new PResponseImpl(jResponse);
    }
}
