package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.INameValues;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterThrowsSync;

/**
 * Created by chenyg on 2018-03-02.
 */
class SyncPorterThrowsImpl implements PorterThrowsSync
{

    private SyncPorterImpl syncPorter;
    @AutoSet
    private Delivery delivery;

    public SyncPorterThrowsImpl(SyncPorterImpl syncPorter)
    {
        this.syncPorter = syncPorter;
    }

    @AutoSet.SetOk(priority = Integer.MAX_VALUE)
    public void setOk()
    {
        syncPorter.delivery = delivery;
        syncPorter.setOk();
    }

    private static <T> T deal(T t)
    {
        if (t == null)
        {
            return null;
        }
        if (t instanceof OftenCallException)
        {
            throw (OftenCallException) t;
        } else if (t instanceof Throwable)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setExCause((Throwable) t);
            throw new OftenCallException(jResponse);
        } else if (t instanceof JResponse && ((JResponse) t).isNotSuccess())
        {
            JResponse jResponse = (JResponse) t;
            Throwable throwable = jResponse.getExCause();
            if (throwable != null)
            {
                try
                {
                    throw throwable;
                } catch (Throwable e)
                {

                }
            }
            throw new OftenCallException((JResponse) t);
        } else
        {
            return t;
        }
    }

    @Override
    public <T> T request(OftenObject oftenObject)
    {
        T t = syncPorter.request(oftenObject);
        return deal(t);
    }

    @Override
    public <T> T request(OftenObject oftenObject, INameValues INameValues)
    {
        T t = syncPorter.request(oftenObject, INameValues);
        return deal(t);
    }

    @Override
    public <T> T requestSimple(OftenObject oftenObject, Object... nameValues)
    {
        T t = syncPorter.requestSimple(oftenObject, nameValues);
        return deal(t);
    }

    @Override
    public <T> T invokeWithObjects(OftenObject oftenObject, Object... objects)
    {
        T t = syncPorter.invokeWithObjects(oftenObject, objects);
        return deal(t);
    }

    @Override
    public <T> T requestWNull()
    {
        T t = syncPorter.requestWNull();
        return deal(t);
    }

    @Override
    public <T> T requestWNull(INameValues INameValues)
    {
        T t = syncPorter.requestWNull(INameValues);
        return deal(t);
    }

    @Override
    public <T> T requestWNullSimple(Object... nameValues)
    {
        T t = syncPorter.requestWNullSimple(nameValues);
        return deal(t);
    }
}
