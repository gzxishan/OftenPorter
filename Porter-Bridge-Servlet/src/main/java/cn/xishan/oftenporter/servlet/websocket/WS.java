package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
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
    public final Type type;

    public final Session session;

    /**
     * 当{@linkplain WebSocket#isPartial()}为false时，该值始终为true。
     */
    public final boolean isLast;

    /**
     * <pre>
     *     返回值类型:
     *     1.{@linkplain Type#ON_OPEN}:null
     *     2.{@linkplain Type#ON_MESSAGE}:String
     *     3.{@linkplain Type#ON_ERROR}:Throwable
     *     4.{@linkplain Type#ON_CLOSE}:{@linkplain CloseReason}
     *     5.{@linkplain Type#ON_PONG}:{@linkplain PongMessage}
     *     6.{@linkplain Type#ON_BINARY_BYTE_ARRAY}:byte[]
     *     7.{@linkplain Type#ON_BINARY_BYTE_BUFFER}:{@linkplain java.nio.ByteBuffer}
     *     8.{@linkplain Type#ON_BINARY_INPUT_STREAM}:{@linkplain java.io.InputStream}
     *     9.{@linkplain Type#ON_MESSAGE_READER}:{@linkplain java.io.Reader}
     * </pre>
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
        HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
        httpSession.removeAttribute(WObject.class.getName());
        httpSession.removeAttribute(PorterOfFun.class.getName());
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
}
