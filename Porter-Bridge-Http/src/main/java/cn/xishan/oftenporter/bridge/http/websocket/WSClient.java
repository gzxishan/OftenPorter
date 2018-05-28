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


    public WSClient()
    {
    }

    public void setSession(SessionImpl session)
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
        if (closeReason != null)
        {
            session.close(closeReason.getCode(), closeReason.getReason());
        } else
        {
            session.close();
        }
    }

}
