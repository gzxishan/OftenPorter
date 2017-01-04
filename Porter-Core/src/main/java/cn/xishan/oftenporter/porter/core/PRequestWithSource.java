package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/28.
 */
public class PRequestWithSource extends PRequest
{


    public static class InvokeSource
    {
        public PName srcPName;
        public String srcContextName;
        public String srcClassTied;

        public InvokeSource(PName srcPName, String srcContextName, String srcClassTied)
        {
            this.srcPName = srcPName;
            this.srcContextName = srcContextName;
            this.srcClassTied = srcClassTied;
        }
    }


    private PRequest request;
    public InvokeSource invokeSource;

    PRequestWithSource(PRequest request, WObject wObject)
    {
        super(request.getMethod(), request.getPath(), false);
        this.request = request;
        this.invokeSource = new InvokeSource(wObject.getPName(), wObject.url().contextName(),
                wObject.url().classTied());
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
    public PRequest addParam(String name, Object value)
    {
        return request.addParam(name, value);
    }

    @Override
    public PRequest addParamAll(AppValues appValues)
    {
        return request.addParamAll(appValues);
    }

    @Override
    public PRequest addParamAll(Map<String, Object> paramMap)
    {
        return request.addParamAll(paramMap);
    }
}
