package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.bridge.BridgeResponse;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
class BridgeResponseImpl extends BridgeResponse
{
    protected BridgeResponseImpl(boolean isOk,Object object)
    {
        super(isOk,object);
    }

    static BridgeResponse exception(ResultCode code, Throwable e)
    {
        JResponse jResponse = new JResponse(code);
        jResponse.setDescription(OftenTool.getMessage(e));
        return new BridgeResponseImpl(false,jResponse);
    }
}
