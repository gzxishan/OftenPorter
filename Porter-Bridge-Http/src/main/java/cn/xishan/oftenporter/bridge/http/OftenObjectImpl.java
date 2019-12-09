package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-14.
 */
class OftenObjectImpl extends OftenObject
{
    private RequestData requestData;

    public OftenObjectImpl(PortMethod method, RequestData requestData)
    {
        Map<String, Object> params = requestData.getParams();
        if (OftenTool.notEmptyOf(params) && !method.isOneOf(PortMethod.POST, PortMethod.PUT))
        {
            String[] names = new String[params.size()];
            Object[] values = new Object[names.length];

            int k = 0;
            for (Map.Entry<String, Object> entry : params.entrySet())
            {
                names[k] = entry.getKey();
                values[k++] = entry.getValue();
            }
            _fInNames = InNames.fromStringArray(null, names, null);
            _fu = values;
        }

        this.requestData = requestData;
    }

    public RequestData getRequestData()
    {
        return requestData;
    }

    @Override
    public OftenRequest getRequest()
    {
        return null;
    }

    @Override
    public OftenResponse getResponse()
    {
        return null;
    }

    @Override
    public ParamSource getParamSource()
    {
        return null;
    }

    @Override
    public boolean isInnerRequest()
    {
        return false;
    }

    @Override
    public <T> T fentity(int index)
    {
        return null;
    }

    @Override
    public <T> T centity(int index)
    {
        return null;
    }


    @Override
    public Delivery delivery()
    {
        return null;
    }

    @Override
    public UrlDecoder.Result url()
    {
        return null;
    }

    @Override
    public BridgeName getBridgeName()
    {
        return null;
    }

    @Override
    public <T> T getContextSet(String objectName)
    {
        return null;
    }

    @Override
    public <T> T getGlobalSet(String objectName)
    {
        return null;
    }

    @Override
    public <T> T getContextSet(Class<T> objectClass)
    {
        return null;
    }

    @Override
    public <T> T getGlobalSet(Class<T> objectClass)
    {
        return null;
    }

    @Override
    public IConfigData getConfigData()
    {
        return null;
    }
}
