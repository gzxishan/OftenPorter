package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorterThrows;

/**
 * Created by chenyg on 2018-03-02.
 */
class SyncPorterThrowsImpl implements SyncPorterThrows
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
        if (t instanceof WCallException)
        {
            throw (WCallException) t;
        } else if (t instanceof Throwable)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setExCause((Throwable) t);
            throw new WCallException(jResponse);
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
                    e.printStackTrace();
                }
            }
            throw new WCallException((JResponse) t);
        } else
        {
            return t;
        }
    }

    @Override
    public <T> T request(WObject wObject)
    {
        T t = syncPorter.request(wObject);
        return deal(t);
    }

    @Override
    public <T> T request(WObject wObject, AppValues appValues)
    {
        T t = syncPorter.request(wObject, appValues);
        return deal(t);
    }

    @Override
    public <T> T requestSimple(WObject wObject, Object... nameValues)
    {
        T t = syncPorter.requestSimple(wObject, nameValues);
        return deal(t);
    }

    @Override
    public <T> T requestWNull()
    {
        T t = syncPorter.requestWNull();
        return deal(t);
    }

    @Override
    public <T> T requestWNull(AppValues appValues)
    {
        T t = syncPorter.requestWNull(appValues);
        return deal(t);
    }

    @Override
    public <T> T requestWNullSimple(Object... nameValues)
    {
        T t = syncPorter.requestWNullSimple(nameValues);
        return deal(t);
    }
}
