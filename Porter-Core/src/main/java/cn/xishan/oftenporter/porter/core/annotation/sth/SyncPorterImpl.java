package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.sysset.PorterNotInnerSync;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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

    private static class Temp
    {
        Object rs = null;
    }

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
    public <T> T request(WObject wObject)
    {
        return request(wObject, null);
    }

    @Override
    public <T> T request(WObject wObject, INameValues INameValues)
    {
        PRequest request;
        if (wObject == null)
        {
            request = new PRequest(null, syncPorterOption.getMethod(), syncPorterOption.getPathWithContext());
        } else
        {
            request = new PRequest(wObject, syncPorterOption.getPathWithContext());
            request.setMethod(syncPorterOption.getMethod());
        }

        request.addParamAll(INameValues);
        Temp temp = new Temp();
        if (isInner)
        {
            delivery.innerBridge().request(request, lResponse -> temp.rs = lResponse.getResponse());
        } else
        {
            delivery.currentBridge().request(request, lResponse -> temp.rs = lResponse.getResponse());
        }
        return (T) temp.rs;
    }

    @Override
    public <T> T requestSimple(WObject wObject, Object... nameValues)
    {
        INameValues INameValues = DefaultNameValues.fromArray(nameValues);
        return request(wObject, INameValues);
    }

    @Override
    public <T> T invokeWithObjects(WObject wObject, Object... objects)
    {
        List<Object> list = new ArrayList<>();
        for (Object obj : objects)
        {
            if(obj instanceof FunParam){
                FunParam funParam = (FunParam) obj;
                if(WPTool.isEmpty(funParam.getName())){
                    throw new NullPointerException("empty FunParam name!");
                }
                list.add(funParam.getName());
                list.add(funParam.getValue());
            }
            else if (obj != null)
            {
                list.add(obj.getClass().getName());
                list.add(obj);
            }
        }
        return requestSimple(wObject, list.toArray(new Object[0]));
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
