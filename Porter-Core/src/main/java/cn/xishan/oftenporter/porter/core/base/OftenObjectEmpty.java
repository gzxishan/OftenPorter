package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;

/**
 * @author Created by https://github.com/CLovinr on 2019/6/8.
 */
class OftenObjectEmpty extends OftenObject
{
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
