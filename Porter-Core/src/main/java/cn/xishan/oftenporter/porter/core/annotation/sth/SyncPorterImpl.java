package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.simple.SimpleAppValues;

/**
 * Created by chenyg on 2017-04-26.
 */
class SyncPorterImpl implements SyncPorter
{
    @AutoSet
    Delivery delivery;

    private _SyncPorterOption syncPorterOption;

    private static class Temp
    {
        Object rs = null;
    }

    @AutoSet.SetOk
    public void setOk()
    {
        syncPorterOption.setOk();
    }

    public SyncPorterImpl(_SyncPorterOption syncPorterOption)
    {
        this.syncPorterOption = syncPorterOption;
    }

    @Override
    public <T> T request(WObject wObject)
    {
        return request(wObject, null);
    }

    @Override
    public <T> T request(WObject wObject, AppValues appValues)
    {
        PRequest request;
        if (wObject == null)
        {
            request = new PRequest(syncPorterOption.getMethod(), syncPorterOption.getPathWithContext());
        } else
        {
            request = new PRequest(wObject.getRequest(), syncPorterOption.getPathWithContext());
            request.setMethod(syncPorterOption.getMethod());
        }

        ABOption abOption = new ABOption(wObject == null ? null : wObject._otherObject, PortFunType.INNER,ABInvokeOrder.OTHER);
        request._setABOption_(abOption);
        request.addParamAll(appValues);
        Temp temp = new Temp();
        delivery.innerBridge().request(request, lResponse -> temp.rs = lResponse.getResponse());
        return (T) temp.rs;
    }

    @Override
    public <T> T requestSimple(WObject wObject, Object... nameValues)
    {
        AppValues appValues = SimpleAppValues.fromArray(nameValues);
        return request(wObject, appValues);
    }

    @Override
    public <T> T requestWNull()
    {
        return request(null, null);
    }

    @Override
    public <T> T requestWNull(AppValues appValues)
    {
        return request(null, appValues);
    }

    @Override
    public <T> T requestWNullSimple(Object... nameValues)
    {
        AppValues appValues = SimpleAppValues.fromArray(nameValues);
        return request(null, appValues);
    }
}
