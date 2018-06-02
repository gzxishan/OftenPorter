package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PName;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by https://github.com/CLovinr on 2016/9/1.
 */
class WObjectImpl extends WObject
{
    private WRequest request;
    private WResponse response;
    private UrlDecoder.Result result;
    Object[] finObjs, cinObjs;


    Context context;
    private PName pName;
    private Delivery delivery;
    private ParamSource paramSource;
    private boolean isInnerRequest;

    WObjectImpl(PName pName, UrlDecoder.Result result, WRequest request, WResponse response,
            Context context, boolean isInnerRequest)
    {
        this.pName = pName;
        this.result = result;
        this.request = request;
        this.response = response;
        this.context = context;
        this.isInnerRequest = isInnerRequest;
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
    public WRequest getRequest()
    {
        return request;
    }

    @Override
    public WResponse getResponse()
    {
        return response;
    }

    @Override
    public ParamSource getParamSource()
    {
        return paramSource;
    }

    @Override
    public PName getPName()
    {
        return pName;
    }

    @Override
    public <T> T finObject(int index)
    {
        Object obj = finObjs[index];
        T t = (T) obj;
        return t;
    }


    @Override
    public <T> T cinObject(int index)
    {
        Object obj = cinObjs[index];
        T t = (T) obj;
        return t;
    }


    @Override
    public <T> T savedObject(String key)
    {
        T t = (T) context.innerContextBridge.contextAutoSet.get(key);
        return t;
    }

    @Override
    public <T> T gsavedObject(String key)
    {
        T t = (T) context.innerContextBridge.innerBridge.globalAutoSet.get(key);
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

    private List<AfterInvokeListener> afterInvokeListenerList;

    @Override
    public boolean isSupportAfterInvokeListener()
    {
        return true;
    }

    @Override
    public synchronized void addAfterInvokeListener(AfterInvokeListener afterInvokeListener)
    {
        if (afterInvokeListenerList == null)
        {
            afterInvokeListenerList = new ArrayList<>();
        }
        afterInvokeListenerList.add(afterInvokeListener);
    }

    void invokeAfterInvokeListeners()
    {
        if (afterInvokeListenerList != null)
        {
            for (AfterInvokeListener listener : afterInvokeListenerList)
            {
                listener.afterInvoke(this);
            }
        }
    }
}
