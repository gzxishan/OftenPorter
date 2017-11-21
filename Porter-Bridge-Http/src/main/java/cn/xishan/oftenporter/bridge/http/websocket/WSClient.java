package cn.xishan.oftenporter.bridge.http.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public abstract class WSClient
{
    public final ClientWebSocket.Type type;

    public final Session session;

    /**
     * 当{@linkplain ClientWebSocket#isPartial()}为false时，该值始终为true。
     */
    public final boolean isLast;

    /**
     * <pre>
     *     返回值类型:
     *     1.{@linkplain ClientWebSocket.Type#ON_OPEN}:null
     *     2.{@linkplain ClientWebSocket.Type#ON_MESSAGE}:String
     *     3.{@linkplain ClientWebSocket.Type#ON_ERROR}:Throwable
     *     4.{@linkplain ClientWebSocket.Type#ON_CLOSE}:{@linkplain ClientCloseReason}
     *     5.{@linkplain ClientWebSocket.Type#ON_PONG}:{@linkplain String}
     *     7.{@linkplain ClientWebSocket.Type#ON_BINARY_BYTE_BUFFER}:{@linkplain java.nio.ByteBuffer}
     * </pre>
     *
     * @param <T>
     * @return
     */
    public abstract <T> T object();

    public WSClient(ClientWebSocket.Type type, Session session, boolean isLast)
    {
        this.type = type;
        this.session = session;
        this.isLast = isLast;
    }

    public void close() throws IOException
    {
        close(null);
    }

    public void close(ClientCloseReason closeReason) throws IOException
    {
    }

    static WSClient newWS(ClientWebSocket.Type type, Session session, boolean isLast, Object value)
    {
        return new WSClient(type, session, isLast)
        {
            @Override
            public <T> T object()
            {
                return (T) value;
            }
        };
    }


}
