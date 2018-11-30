package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.sysset.PorterNotInnerSync;
import cn.xishan.oftenporter.porter.simple.DefaultNameValues;

import java.util.Map;


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
    public <T> T invokeWithNameValues(OftenObject oftenObject, Object... nameValues)
    {
        return invokeWithMap(oftenObject, DefaultNameValues.fromArray(nameValues).toJSON());
    }

    @Override
    public <T> T invokeWithMap(OftenObject oftenObject, Map<String, Object> params)
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

        request.addParamAll(params);
        Object[] temp = new Object[1];
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
    public <T> T invokeWithObjects(OftenObject oftenObject, Object... objects)
    {
        return invokeWithMap(oftenObject, FunParam.toJSON(objects));
    }


}
