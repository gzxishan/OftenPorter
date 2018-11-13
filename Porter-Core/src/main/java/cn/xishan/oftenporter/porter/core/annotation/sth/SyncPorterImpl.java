package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.sysset.PorterNotInnerSync;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultNameValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyg on 2017-04-26.
 */
class SyncPorterImpl implements PorterNotInnerSync
{
    @AutoSet
    Delivery delivery;
    boolean isInner;

    private _SyncPorterOption syncPorterOption;



    @AutoSet.SetOk(priority = Integer.MAX_VALUE)
    public void setOk()
    {
        syncPorterOption.setOk();
    }

    public SyncPorterImpl(_SyncPorterOption syncPorterOption, boolean isInner)
    {
        this.syncPorterOption = syncPorterOption;
        this.isInner = isInner;
    }

    @Override
    public <T> T request(OftenObject oftenObject)
    {
        return request(oftenObject, null);
    }

    @Override
    public <T> T request(OftenObject oftenObject, INameValues INameValues)
    {
        BridgeRequest request;
        if (oftenObject == null)
        {
            request = new BridgeRequest(null, syncPorterOption.getMethod(), syncPorterOption.getPathWithContext());
        } else
        {
            request = new BridgeRequest(oftenObject, syncPorterOption.getPathWithContext());
            request.setMethod(syncPorterOption.getMethod());
        }

        request.addParamAll(INameValues);
        Object[] temp=new Object[1];
        if (isInner)
        {
            delivery.innerBridge().request(request, lResponse -> temp[0] = lResponse.getResponse());
        } else
        {
            delivery.currentBridge().request(request, lResponse -> temp[0] = lResponse.getResponse());
        }
        return (T) temp[0];
    }

    @Override
    public <T> T requestSimple(OftenObject oftenObject, Object... nameValues)
    {
        INameValues INameValues = DefaultNameValues.fromArray(nameValues);
        return request(oftenObject, INameValues);
    }

    @Override
    public <T> T invokeWithObjects(OftenObject oftenObject, Object... objects)
    {
        List<String> names = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Object obj : objects)
        {
            if (obj instanceof FunParam)
            {
                FunParam funParam = (FunParam) obj;
                if (OftenTool.isEmpty(funParam.getName()))
                {
                    throw new NullPointerException("empty FunParam name!");
                }
                names.add(funParam.getName());
                values.add(funParam.getValue());
            } else if (obj != null)
            {
                names.add(obj.getClass().getName());
                values.add(obj);
            }
        }
        DefaultNameValues defaultNameValues = new DefaultNameValues(names, values);
        return request(oftenObject, defaultNameValues);
    }

    @Override
    public <T> T requestWNull()
    {
        return request(null, null);
    }

    @Override
    public <T> T requestWNull(INameValues INameValues)
    {
        return request(null, INameValues);
    }

    @Override
    public <T> T requestWNullSimple(Object... nameValues)
    {
        INameValues INameValues = DefaultNameValues.fromArray(nameValues);
        return request(null, INameValues);
    }
}
