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
        PRequest request = new PRequest(syncPorterOption.getMethod(), syncPorterOption.getPathWithContext());
        ABOption abOption = new ABOption(wObject._otherObject, ABType.METHOD_OF_INNER, ABPortType.OTHER);
        request._setABOption_(abOption);
        request.addParamAll(appValues);
        Temp temp = new Temp();
        delivery.currentBridge().request(request, lResponse -> temp.rs = lResponse.getResponse());
        return (T) temp.rs;
    }

    @Override
    public <T> T requestSimple(WObject wObject, Object... nameValues)
    {
        if (nameValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("Illegal arguments length:" + nameValues.length);
        }
        SimpleAppValues appValues = new SimpleAppValues();
        String[] names = new String[nameValues.length / 2];
        Object[] values = new Object[nameValues.length / 2];
        for (int i = 0, k = 0; i < names.length; i += 2, k++)
        {
            names[k] = (String) nameValues[i];
            values[k] = nameValues[i + 1];
        }

        appValues.names(names).values(values);
        return request(wObject, appValues);
    }
}
