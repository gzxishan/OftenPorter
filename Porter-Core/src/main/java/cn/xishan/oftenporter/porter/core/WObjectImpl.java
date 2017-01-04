package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.base.WRequest;
import cn.xishan.oftenporter.porter.core.base.WResponse;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PName;


/**
 * Created by https://github.com/CLovinr on 2016/9/1.
 */
class WObjectImpl extends WObject
{
    private WRequest request;
    private WResponse response;
    private UrlDecoder.Result result;
    Object[] finObjs, cinObjs;

    private Context context;
    private PName pName;
    private Delivery delivery;

    WObjectImpl(PName pName, UrlDecoder.Result result, WRequest request, WResponse response, Context context)
    {
        this.pName = pName;
        this.result = result;
        this.request = request;
        this.response = response;
        this.context = context;
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
    public Delivery delivery()
    {
        if (delivery == null)
        {
            synchronized (this)
            {
                if (delivery == null)
                {
                    delivery = context.deliveryBuilder.build(this);
                }
            }
        }
        return delivery;
    }

    @Override
    public UrlDecoder.Result url()
    {
        return result;
    }
}
