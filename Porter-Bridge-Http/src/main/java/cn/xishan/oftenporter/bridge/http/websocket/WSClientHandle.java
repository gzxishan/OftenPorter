package cn.xishan.oftenporter.bridge.http.websocket;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;

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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
class WSClientHandle extends AspectOperationOfPortIn.HandleAdapter<ClientWebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WSClientHandle.class);

    class Handle implements SessionImpl.OnClose
    {
        private WSClient wsClient = new WSClient();
        private WSClientConfig wsClientConfig;
        private boolean isDestroyed = false;
        private ScheduledExecutorService scheduledExecutorService;
        private WObject wObject;
        private long lastRetryTime;
        private int retriedCount;

        void start(WObject wObject)
        {

            try
            {
                this.wObject = wObject;
                this.wsClient.set(ClientWebSocket.Type.ON_CONFIG, null);
                WSClientConfig wsClientConfig = (WSClientConfig) porterOfFun
                        .invokeByHandleArgs(wObject, wsClient);
                this.wsClientConfig = wsClientConfig;

                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                });


                Runnable runnable = () -> {
                    if (isDestroyed)
                    {
                        return;
                    }
                    if (wsClient.session == null || wsClient.session.isClosed())
                    {
                        doConnect();
                    }
                };
                scheduledExecutorService.scheduleAtFixedRate(runnable, wsClientConfig.initDelay,
                        wsClientConfig.retryDelay, TimeUnit.MILLISECONDS);

            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onClosed()
        {
        }

        void destroy()
        {
            if (isDestroyed)
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

        void doConnect()
        {
            if (wsClient.session == null)
            {
                connect();
            } else
            {
                //重试
                if (wsClientConfig.retryTimes < 0 || hasRetryTimes())
                {
                    lastRetryTime = System.currentTimeMillis();
                    scheduledExecutorService.schedule(this::connect, wsClientConfig.retryDelay, TimeUnit.MILLISECONDS);
                } else
                {
                    LOGGER.warn("stop retry!");
                    handleSet.remove(this);
                    wsClient.session = null;
                    destroy();
                }

            }
        }

        private boolean hasRetryTimes()
        {
            if (wsClientConfig.retryTimes < 0)
            {
                return true;
            }

            if (System.currentTimeMillis() - lastRetryTime > wsClientConfig.retryDelay * 10)
            {
                retriedCount = 0;
            }

            if (retriedCount++ < wsClientConfig.retryTimes)
            {
                return true;
            } else
            {
                return true;
            }

        }


        void connect()
        {
            try
            {
                _connect();
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }


        private void _connect() throws Exception
        {
            if (wsClient.session != null && wsClient.session.webSocketClient.isOpen())
            {
                return;
            }
            if (wsClient.session != null)
            {
                wsClient.session.close();
            }
            String wsUrl=wsClientConfig.getWSUrl();
            WebSocketClient webSocketClient = new WebSocketClient(URI.create(wsUrl),
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
                        wsClient.set(ClientWebSocket.Type.ON_OPEN, null);
                        Object obj = porterOfFun.invokeByHandleArgs(wObject, wsClient);
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
                        wsClient.set(ClientWebSocket.Type.ON_MESSAGE, message);
                        Object obj = porterOfFun.invokeByHandleArgs(wObject, wsClient);
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
                        wsClient.set(ClientWebSocket.Type.ON_BINARY_BYTE_BUFFER, bytes);
                        Object obj = porterOfFun.invokeByHandleArgs(wObject, wsClient);
                        maySend(obj);
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }


                @Override
                public void onWebsocketPong(WebSocket conn, Framedata f)
                {
                    try
                    {
                        ByteBuffer byteBuffer = f.getPayloadData();
                        wsClient.set(ClientWebSocket.Type.ON_PONG, byteBuffer);
                        Object obj = porterOfFun.invokeByHandleArgs(wObject, wsClient);
                        maySend(obj);
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }


                @Override
                public void onClose(int code, String reason, boolean remote)
                {
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_CLOSE, new ClientCloseReason(code, reason));
                        porterOfFun.invokeByHandleArgs(wObject, wsClient);
                    } catch (Exception e)
                    {
                        LOGGER.debug(e.getMessage(), e);
                    } finally
                    {
                        doConnect();
                    }

                }

                @Override
                public void onError(Exception ex)
                {
                    try
                    {
                        wsClient.set(ClientWebSocket.Type.ON_ERROR, ex);
                        porterOfFun.invokeByHandleArgs(wObject, wsClient);
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
            wsClient.setSession(new SessionImpl(webSocketClient, this));
            if (wsClientConfig.connectionLostTimeoutSecond != null)
            {
                webSocketClient.setConnectionLostTimeout(wsClientConfig.connectionLostTimeoutSecond);
            }
            LOGGER.debug("connect to WebSocket server...:{}",wsUrl);
            webSocketClient.connectBlocking();
        }


    }

    private PorterOfFun porterOfFun;
    private Set<Handle> handleSet = ConcurrentHashMap.newKeySet();
    private ClientWebSocket clientWebSocket;


    @Override
    public boolean init(ClientWebSocket current,IConfigData configData, PorterOfFun porterOfFun)
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
            for (int i = 0; i < clientWebSocket.startCount(); i++)
            {
                SyncOption syncOption = new SyncOption(porterOfFun.getMethodPortIn().getMethods()[0],
                        porterOfFun.getMethodPortIn().getTiedNames()[0]);
                wObject.newSyncPorter(syncOption).request(wObject);
            }
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
