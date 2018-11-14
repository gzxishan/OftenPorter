package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-14.
 */
class OftenObjectImpl extends OftenObject
{
    private Map<String, String> headers;

    public OftenObjectImpl(RequestData requestData)
    {
        String[] names = new String[requestData.params.size()];
        Object[] values = new Object[names.length];

        int k = 0;
        for (Map.Entry<String, Object> entry : requestData.params.entrySet())
        {
            names[k] = entry.getKey();
            values[k++] = entry.getValue();
        }
        fInNames = InNames.fromStringArray(null, names, null);
        fu = values;
        this.headers = requestData.headers;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
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
    public <T> T savedObject(String key)
    {
        return null;
    }

    @Override
    public <T> T gsavedObject(String key)
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
    public BridgeName getPName()
    {
        return null;
    }
}
