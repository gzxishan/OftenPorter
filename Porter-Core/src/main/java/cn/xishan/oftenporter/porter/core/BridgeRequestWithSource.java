package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.INameValues;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
class BridgeRequestWithSource extends BridgeRequest
{


    public static class InvokeSource
    {
        public BridgeName srcBridgeName;
        public String srcContextName;
        public String srcClassTied;

        public InvokeSource(BridgeName srcBridgeName, String srcContextName, String srcClassTied)
        {
            this.srcBridgeName = srcBridgeName;
            this.srcContextName = srcContextName;
            this.srcClassTied = srcClassTied;
        }
    }


    private BridgeRequest request;
    public InvokeSource invokeSource;

    BridgeRequestWithSource(BridgeRequest request, OftenObject oftenObject)
    {
        super(oftenObject, request.getMethod(), request.getPath(), false);
        initOrigin(request);
        this.request = request;
        this.invokeSource = new InvokeSource(oftenObject.getBridgeName(), oftenObject.url().contextName(),
                oftenObject.url().classTied());
    }

    @Override
    public Object getParameter(String name)
    {
        return request.getParameter(name);
    }


    @Override
    public Map<String, Object> getParameterMap()
    {
        return request.getParameterMap();
    }

    @Override
    public BridgeRequest addParam(String name, Object value)
    {
        return request.addParam(name, value);
    }

    @Override
    public BridgeRequest addParamAll(INameValues INameValues)
    {
        return request.addParamAll(INameValues);
    }

    @Override
    public BridgeRequest addParamAll(Map<String, Object> paramMap)
    {
        return request.addParamAll(paramMap);
    }
}
