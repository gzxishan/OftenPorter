package cn.xishan.oftenporter.bridge.http.websocket;


import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WSClient
{
    ClientWebSocket.Type type;
    SessionImpl session;
    Object object;

    /**
     * 返回值类型:
     * <ol>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_OPEN}:null
     * </li>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_MESSAGE}:String
     * </li>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_ERROR}:Throwable
     * </li>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_CLOSE}:{@linkplain ClientCloseReason}
     * </li>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_PONG}:{@linkplain java.nio.ByteBuffer}
     * </li>
     * <li>
     * {@linkplain ClientWebSocket.Type#ON_BINARY_BYTE_BUFFER}:{@linkplain java.nio.ByteBuffer}
     * </li>
     * </ol>
     *
     * @param <T>
     * @return
     */
    public <T> T object()
    {
        return (T) object;
    }

    public Session session()
    {
        return session;
    }

    public ClientWebSocket.Type type()
    {
        return type;
    }


    WSClient()
    {
    }


    void setSession(SessionImpl session)
    {
        this.session = session;
    }

    void set(ClientWebSocket.Type type, Object object)
    {
        this.type = type;
        this.object = object;
    }


    public void close() throws IOException
    {
        close(null);
    }

    public void close(ClientCloseReason closeReason) throws IOException
    {
        if (session == null)
        {
            return;
        }
        if (closeReason != null)
        {
            session.close(closeReason.getCode(), closeReason.getReason());
        } else
        {
            session.close();
        }
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof WSClient))
        {
            return false;
        } else
        {
            WSClient ws = (WSClient) obj;
            return ws.session.getId().equals(session.getId());
        }
    }

    @Override
    public int hashCode()
    {
        return session.getId().hashCode();
    }

    @Override
    public String toString()
    {
        String builder = getClass().getName() + "@" + super.hashCode() + "-->" +
                session.getClass().getSimpleName() + "@" + session.hashCode() +
                ",id=" + session.getId();
        return builder;
    }

    public final <T> T removeAttribute(Class<?> clazzKey)
    {
        return removeAttribute(clazzKey.getName());
    }

    public final <T> T removeAttribute(String key)
    {
        return (T) session.getUserProperties().remove(key);
    }

    public final <T> T putAttribute(Class<?> clazzKey, Object value)
    {
        return putAttribute(clazzKey.getName(), value);
    }

    /**
     * @return 返回上一次的属性。
     */
    public final <T> T putAttribute(String key, Object value)
    {
        return (T) session.getUserProperties().put(key, value);
    }

    public final <T> T getAttribute(Class<?> clazzKey)
    {
        return getAttribute(clazzKey.getName());
    }


    public final <T> T getAttribute(String key)
    {
        Object v = session.getUserProperties().get(key);
        return (T) v;
    }

}
