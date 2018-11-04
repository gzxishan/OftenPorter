package cn.xishan.oftenporter.servlet.websocket;


import cn.xishan.oftenporter.servlet.websocket.WebSocket.Type;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public abstract class WS
{
    private final Type type;

    public Type type()
    {
        return type;
    }

    private final Session session;

    public Session session()
    {
        return session;
    }

    /**
     * 当{@linkplain WebSocket#isPartial()}为false时，该值始终为true。
     */
    private final boolean isLast;

    /**
     * 当{@linkplain WebSocket#isPartial()}为false时，该值始终为true。
     */
    public boolean isLast()
    {
        return isLast;
    }

    /**
     * <p>
     * 返回值类型:
     * <ol>
     * <li>{@linkplain Type#ON_CONFIG}:{@linkplain WSConfig}</li>
     * <li>{@linkplain Type#ON_CONNECTING}:{@linkplain Connecting}</li>
     * <li>{@linkplain Type#ON_OPEN}:null</li>
     * <li>{@linkplain Type#ON_MESSAGE}:String</li>
     * <li>{@linkplain Type#ON_ERROR}:Throwable</li>
     * <li>{@linkplain Type#ON_CLOSE}:{@linkplain CloseReason}</li>
     * <li>{@linkplain Type#ON_PONG}:{@linkplain PongMessage}</li>
     * <li>{@linkplain Type#ON_BINARY_BYTE_ARRAY}:byte[]</li>
     * <li>{@linkplain Type#ON_BINARY_BYTE_BUFFER}:{@linkplain java.nio.ByteBuffer}</li>
     * <li>{@linkplain Type#ON_BINARY_INPUT_STREAM}:{@linkplain java.io.InputStream}</li>
     * <li>{@linkplain Type#ON_MESSAGE_READER}:{@linkplain java.io.Reader}</li>
     * <li></li>
     * </ol>
     * </p>
     *
     * @param <T>
     * @return
     */
    public abstract <T> T object();

    public WS(Type type, Session session, boolean isLast)
    {
        this.type = type;
        this.session = session;
        this.isLast = isLast;
    }

    public void close() throws IOException
    {
        close(null);
    }

    public void close(CloseReason closeReason) throws IOException
    {
        if (closeReason != null)
        {
            session.close(closeReason);
        } else
        {
            session.close();
        }
    }

    static WS newWS(Type type, Session session, boolean isLast, Object value)
    {
        return new WS(type, session, isLast)
        {
            @Override
            public <T> T object()
            {
                return (T) value;
            }
        };
    }

    public final <T> T removeAttribute(Class<?> clazzKey)
    {
        return removeAttribute(clazzKey.getName());
    }

    public final <T> T removeAttribute(String key)
    {
        return (T) session.getUserProperties().remove(key);
    }

    public final <T> T putAttribute(Class<?> clazzKey, Object vlaue)
    {
        return putAttribute(clazzKey.getName(), vlaue);
    }

    /**
     * @return 返回上一次的属性。
     */
    public final <T> T putAttribute(String key, Object vlaue)
    {
        return (T) session.getUserProperties().put(key, vlaue);
    }

    public final <T> T getAttribute(Class<?> clazzKey)
    {
        return getAttribute(clazzKey.getName());
    }

    public HttpSession getHttpSession()
    {
        return getAttribute(HttpSession.class);
    }


    public final <T> T getAttribute(String key)
    {
        Object v = session.getUserProperties().get(key);
        return (T) v;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof WS))
        {
            return false;
        } else
        {
            WS ws = (WS) obj;
            return ws.session.getId().equals(session.getId());
        }
    }

    @Override
    public int hashCode()
    {
        return session.getId().hashCode();
    }
}
