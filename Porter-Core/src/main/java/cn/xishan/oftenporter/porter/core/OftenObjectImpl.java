package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;

import java.lang.ref.WeakReference;


/**
 * Created by https://github.com/CLovinr on 2016/9/1.
 */
class OftenObjectImpl extends OftenObject
{
    private OftenRequest request;
    private OftenResponse response;
    private UrlDecoder.Result result;
    Object[] fentities, centities;


    Context context;
    private BridgeName bridgeName;
    private Delivery delivery;
    private ParamSource paramSource;
    private boolean isInnerRequest;

    PorterOfFun porterOfFun;
    PortExecutor portExecutor;

    OftenObjectImpl(BridgeName bridgeName, UrlDecoder.Result result, OftenRequest request, OftenResponse response,
            Context context, boolean isInnerRequest)
    {
        this.bridgeName = bridgeName;
        this.result = result;
        this.request = request;
        this.response = response;
        this.context = context;
        this.isInnerRequest = isInnerRequest;
        threadLocal.get().push(new WeakReference<>(this));
    }

    @Override
    public void release()
    {
        threadLocal.get().pop();
    }

    @Override
    public boolean isInnerRequest()
    {
        return isInnerRequest;
    }


    void setParamSource(ParamSource paramSource)
    {
        this.paramSource = paramSource;
    }

    @Override
    public OftenRequest getRequest()
    {
        return request;
    }

    @Override
    public OftenResponse getResponse()
    {
        return response;
    }

    @Override
    public ParamSource getParamSource()
    {
        return paramSource;
    }

    @Override
    public BridgeName getPName()
    {
        return bridgeName;
    }

    @Override
    public <T> T fentity(int index)
    {
        Object obj = fentities[index];
        T t = (T) obj;
        return t;
    }

    @Override
    public <T> T extraEntity(String key)
    {
        if (portExecutor != null)
        {
            return (T) portExecutor.getExtrwaEntity(this, key);
        }
        return super.extraEntity(key);
    }

    @Override
    public <T> T centity(int index)
    {
        Object obj = centities[index];
        T t = (T) obj;
        return t;
    }

    @Override
    public synchronized Delivery delivery()
    {
        if (delivery == null)
        {
            delivery = context.deliveryBuilder.build(this);
        }
        return delivery;
    }

    @Override
    public UrlDecoder.Result url()
    {
        return result;
    }

    @Override
    public <T> T getContextSet(String objectName)
    {
        return context.getContextSet(objectName);
    }

    @Override
    public <T> T getGlobalSet(String objectName)
    {
        return context.getGlobalSet(objectName);
    }

    @Override
    public <T> T getContextSet(Class<T> objectClass)
    {
        return context.getContextSet(objectClass);
    }

    @Override
    public <T> T getGlobalSet(Class<T> objectClass)
    {
        return context.getGlobalSet(objectClass);
    }

    @Override
    public IConfigData getConfigData()
    {
        return context.getConfigData();
    }
}
