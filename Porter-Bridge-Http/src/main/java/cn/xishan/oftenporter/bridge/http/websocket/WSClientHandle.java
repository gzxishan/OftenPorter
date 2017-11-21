package cn.xishan.oftenporter.bridge.http.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
class WSClientHandle implements AspectFunOperation.Handle
{
    private SessionImpl session;
    private PorterOfFun porterOfFun;
    private WSClientConfig wsClientConfig;
    private boolean isDestroyed = false;
    private ScheduledExecutorService scheduledExecutorService;
    private WObject wObject;


    private static final Logger LOGGER = LoggerFactory.getLogger(WSClientHandle.class);

    @Override
    public boolean init(Annotation current, Porter porter)
    {
        return false;
    }

    @Override
    public boolean init(Annotation current, PorterOfFun porterOfFun)
    {
        this.porterOfFun = porterOfFun;
        return true;
    }

    @Override
    public void onStart(WObject wObject)
    {
        try
        {
            this.wObject = wObject;
            WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_CONFIG, null, true, null);
            WSClientConfig wsClientConfig = (WSClientConfig) porterOfFun
                    .invoke(wObject, new Object[]{wObject, wsClient});
            this.wsClientConfig = wsClientConfig;

            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            });

            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                if (isDestroyed)
                {
                    return;
                }
                if (session == null || session.isClosed())
                {
                    checkConnect();
                } else if (session.webSocketClient.isOpen())
                {
                    session.sendPing();
                }
            }, wsClientConfig.initDelay, wsClientConfig.heartDelay, TimeUnit.MILLISECONDS);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    private void checkConnect()
    {
        if (session == null)
        {
            connect();
        } else
        {
            scheduledExecutorService.schedule(() -> connect(), wsClientConfig.retryDelay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDestroy()
    {
        if (!isDestroyed)
        {
            return;
        }
        isDestroyed = true;
        if (session != null)
        {
            session.close();
        }
        if (scheduledExecutorService != null)
        {
            scheduledExecutorService.shutdownNow();
        }
    }

    @Override
    public Object invokeMethod(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }

    private void connect()
    {
        if (this.session != null && this.session.webSocketClient.isOpen())
        {
            return;
        }
        if (this.session != null)
        {
            this.session.close();
        }
        WebSocketClient webSocketClient = new WebSocketClient(URI.create(wsClientConfig.getWSUrl()), new Draft_6455(),
                wsClientConfig.getConnectHeaders(), wsClientConfig.connectTimeout)
        {

            private void maySend(Object obj)
            {
                if (obj == null)
                {
                    return;
                }

                if (obj instanceof byte[])
                {
                    byte[] bs = (byte[]) obj;
                    session.send(bs);
                } else if (obj instanceof ByteBuffer)
                {
                    ByteBuffer byteBuffer = (ByteBuffer) obj;
                    session.send(byteBuffer);
                } else
                {
                    session.send(String.valueOf(obj));
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata)
            {
                WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_OPEN, session, true, null);
                try
                {
                    Object obj = porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                    maySend(obj);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onMessage(String message)
            {
                WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_MESSAGE, session, true, message);
                try
                {
                    Object obj = porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                    maySend(obj);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onMessage(ByteBuffer bytes)
            {
                WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_BINARY_BYTE_BUFFER, session, true, bytes);
                try
                {
                    Object obj = porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                    maySend(obj);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onWebsocketMessageFragment(WebSocket conn, Framedata frame)
            {
                super.onWebsocketMessageFragment(conn, frame);
                if (frame.getOpcode() == Framedata.Opcode.PONG)
                {
                    try
                    {
                        ByteBuffer byteBuffer = frame.getPayloadData();
                        CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
                        WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_PONG, session, true,
                                decoder.decode(byteBuffer).toString());
                        Object obj = porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                        maySend(obj);
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote)
            {
                WSClient wsClient = WSClient
                        .newWS(ClientWebSocket.Type.ON_CLOSE, session, true, new ClientCloseReason(code, reason));
                try
                {
                    porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                } catch (Exception e)
                {
                    LOGGER.debug(e.getMessage(), e);
                } finally
                {
                    checkConnect();
                }

            }

            @Override
            public void onError(Exception ex)
            {
                WSClient wsClient = WSClient.newWS(ClientWebSocket.Type.ON_ERROR, session, true, ex);
                try
                {
                    porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        this.session = new SessionImpl(webSocketClient);
        webSocketClient.connect();
    }

}
