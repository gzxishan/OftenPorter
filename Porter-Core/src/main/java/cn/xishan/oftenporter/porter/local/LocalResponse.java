package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.base.CloseListener;
import cn.xishan.oftenporter.porter.core.base.OftenResponse;
import cn.xishan.oftenporter.porter.core.bridge.BridgeCallback;

import java.io.IOException;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class LocalResponse implements OftenResponse, CloseListener.CloseHandle
{
    protected Object object;
    private BridgeCallback callback;
    private CloseListener closeListener;
    private boolean isErr = false;
    private boolean closed = false;

    public LocalResponse(BridgeCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public void toErr()
    {
        isErr = true;
    }

    @Override
    public void write(Object object) throws IOException
    {
        if (this.object != null)
        {
            throw new IOException("already write before!");
        }
        this.object = object;
    }

    @Override
    public void close() throws IOException
    {
        if (closed)
        {
            throw new IOException("already closed!");
        }
        closed = true;
        if (closeListener != null)
        {
            closeListener.onClose(object, this);
        } else
        {
            doClose(object);
        }

    }


    @Override
    public void setCloseListener(CloseListener closeListener)
    {
        this.closeListener = closeListener;
    }

    @Override
    public void doClose(Object writeObject) throws IOException
    {
        if (callback != null)
        {
            callback.onResponse(new LResponse(!isErr, writeObject));
        }
    }
}
