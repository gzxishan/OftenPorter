package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterThrowsSync;

import java.util.Map;

/**
 * Created by chenyg on 2018-03-02.
 */
public class SyncPorterThrowsImpl implements PorterThrowsSync
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

    public static <T> T deal(Object t)
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
            return (T) t;
        }
    }

    @Override
    public <T> T invokeWithMap(OftenObject oftenObject, Map<String, Object> params)
    {
        T t = syncPorter.invokeWithMap(oftenObject, params);
        return deal(t);
    }

    @Override
    public <T> T invokeWithNameValues(OftenObject oftenObject, Object... nameValues)
    {
        T t = syncPorter.invokeWithNameValues(oftenObject, nameValues);
        return deal(t);
    }

    @Override
    public <T> T invokeWithObjects(OftenObject oftenObject, Object... objects)
    {
        T t = syncPorter.invokeWithObjects(oftenObject, objects);
        return deal(t);
    }


}
