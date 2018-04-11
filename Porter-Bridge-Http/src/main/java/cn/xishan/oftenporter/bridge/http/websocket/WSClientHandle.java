package cn.xishan.oftenporter.bridge.http.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.SyncOption;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
class WSClientHandle extends AspectFunOperation.HandleAdapter<ClientWebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WSClientHandle.class);

    class Handle implements SessionImpl.OnClose
    {
        private WSClient wsClient = new WSClient();
        private WSClientConfig wsClientConfig;
        private boolean isDestroyed = false;
        private ScheduledExecutorService scheduledExecutorService;
        private WObject wObject;

        void start(WObject wObject)
        {

            try
            {
                this.wObject = wObject;
                this.wsClient.set(ClientWebSocket.Type.ON_CONFIG, null, true);
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
                    if (wsClient.session == null || wsClient.session.isClosed())
                    {
                        checkConnect();
                    } else if (wsClient.session.webSocketClient.isOpen())
                    {
                        wsClient.session.sendPing();
                    }
                }, wsClientConfig.initDelay, wsClientConfig.heartDelay, TimeUnit.MILLISECONDS);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onClosed()
        {
            handleSet.remove(this);
            wsClient.session = null;
            destroy();
        }

        void destroy()
        {
            if (!isDestroyed)
            {
                return;
            }
            isDestroyed = true;
            if (wsClient.session != null)
            {
                wsClient.session.close();
                wsClient.session = null;
            }
            if (scheduledExecutorService != null)
            {
                scheduledExecutorService.shutdownNow();
                scheduledExecutorService = null;
            }
        }

        void checkConnect()
        {
            if (wsClient.session == null)
            {
                connect();
            } else
            {
                scheduledExecutorService.schedule(() -> connect(), wsClientConfig.retryDelay, TimeUnit.MILLISECONDS);
            }
        }

        void connect(){
            try
            {
                _connect();
            }catch (Exception e){
                LOGGER.error(e.getMessage(),e);
            }
        }


     private   void _connect()
        {
            if (wsClient.session != null && wsClient.session.webSocketClient.isOpen())
            {
                return;
            }
            if (wsClient.session != null)
            {
                wsClient.session.close();
            }
            WebSocketClient webSocketClient = new WebSocketClient(URI.create(wsClientConfig.getWSUrl()),
                    new Draft_6455(),
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
                        wsClient.session.send(bs);
                    } else if (obj instanceof ByteBuffer)
                    {
                        ByteBuffer byteBuffer = (ByteBuffer) obj;
                        wsClient.session.send(byteBuffer);
                    } else
                    {
                        wsClient.session.send(String.valueOf(obj));
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshakedata)
                {
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_OPEN, null, true);
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
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_MESSAGE, message, true);
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
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_BINARY_BYTE_BUFFER, bytes, true);
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
                            wsClient.set(ClientWebSocket.Type.ON_PONG, decoder.decode(byteBuffer).toString(), true);
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
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_CLOSE, new ClientCloseReason(code, reason), true);
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
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_ERROR, ex, true);
                        porterOfFun.invoke(wObject, new Object[]{wObject, wsClient});
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
            wsClient.setSession(new SessionImpl(webSocketClient, this));
            webSocketClient.connect();
        }


    }

    private PorterOfFun porterOfFun;
    private Set<Handle> handleSet = ConcurrentHashMap.newKeySet();
    private ClientWebSocket clientWebSocket;


    @Override
    public boolean init(ClientWebSocket current, PorterOfFun porterOfFun)
    {
        this.porterOfFun = porterOfFun;
        this.clientWebSocket = current;
        return true;
    }

    @Override
    public void onStart(WObject wObject)
    {
        if (clientWebSocket.autoStart())
        {
            SyncOption syncOption = new SyncOption(porterOfFun.getMethodPortIn().getMethods()[0],
                    porterOfFun.getMethodPortIn().getTiedNames()[0]);
            wObject.newSyncPorter(syncOption).request(wObject);
        }

    }


    @Override
    public void onDestroy()
    {
        Iterator<Handle> iterator = handleSet.iterator();
        while (iterator.hasNext())
        {
            try
            {
                iterator.next().destroy();
            } catch (Exception e)
            {
                LOGGER.debug(e.getMessage(), e);
            }
            iterator.remove();
        }
    }

    @Override
    public Object invoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        Handle handle = new Handle();
        handle.start(wObject);
        handleSet.add(handle);
        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }


}
