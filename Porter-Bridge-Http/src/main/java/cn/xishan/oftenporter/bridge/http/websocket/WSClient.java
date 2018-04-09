package cn.xishan.oftenporter.bridge.http.websocket;


import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WSClient
{
    ClientWebSocket.Type type;
    SessionImpl session;
    boolean isLast;
    Object object;

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

    /**
     * @return 当{@linkplain ClientWebSocket#isPartial()}为false时，该值始终为true。
     */
    public boolean isLast()
    {
        return isLast;
    }

    public WSClient()
    {
    }

    public void setSession(SessionImpl session)
    {
        this.session = session;
    }

    void set(ClientWebSocket.Type type,Object object, boolean isLast)
    {
        this.type = type;
        this.session = session;
        this.object = object;
        this.isLast = isLast;
    }


    public void close() throws IOException
    {
        close(null);
    }

    public void close(ClientCloseReason closeReason) throws IOException
    {
        if (closeReason != null)
        {
            session.close(closeReason.getCode(), closeReason.getReason());
        } else
        {
            session.close();
        }
    }

}
