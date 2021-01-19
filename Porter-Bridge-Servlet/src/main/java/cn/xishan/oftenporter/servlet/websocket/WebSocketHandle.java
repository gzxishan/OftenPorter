package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.OftenServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WebSocketHandle extends AspectOperationOfPortIn.HandleAdapter<WebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandle.class);
    private WSConfig wsConfig = new WSConfig();
    private WebSocket webSocket;
    private PorterOfFun thePorterOfFun;
    private String path;
    private OftenObject forInit;

    private static Set<String> webSocketPaths = ConcurrentHashMap.newKeySet();

    @AutoSet.SetOk
    public void setOk(OftenObject oftenObject) throws Throwable
    {
        this.forInit = oftenObject;
        List<WebSocketHandle> webSocketHandleList = oftenObject.getConfigData().get(WebSocketHandle.class.getName());
        if (webSocketHandleList == null)
        {
            webSocketHandleList = new ArrayList<>();
            oftenObject.getConfigData().set(WebSocketHandle.class.getName(), webSocketHandleList);
        }
        webSocketHandleList.add(this);
    }

    public static boolean isWebSocket(HttpServletRequest request)
    {
        return webSocketPaths.contains(OftenServletRequest.getOftenPath(request));
    }

    @PortDestroy
    public void destroy()
    {
        webSocketPaths.remove(path);
    }

    @Override
    public boolean init(WebSocket webSocket, IConfigData configData, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        this.thePorterOfFun = porterOfFun;
        return true;
    }

    public static void initWithServerContainer(IConfigData configData, ServerContainer serverContainer) throws Exception
    {
        List<WebSocketHandle> webSocketHandleList = configData.get(WebSocketHandle.class.getName());
        if (webSocketHandleList != null)
        {
            configData.remove(WebSocketHandle.class.getName());
            for (WebSocketHandle handle : webSocketHandleList)
            {
                handle.initWithServerContainer(handle.thePorterOfFun, serverContainer);
            }
        }
    }

    private void initWithServerContainer(PorterOfFun fun, ServerContainer serverContainer) throws Exception
    {
        OftenObject oftenObject = forInit;
        forInit = null;
        _PortIn funIn = fun.getMethodPortIn();
        if (funIn.getMethods().length != 1 || funIn.getMethods()[0] != PortMethod.GET)
        {
            throw new RuntimeException("WebSocket函数的@PortIn只能设置一个method方法且必须为GET");
        } else if (funIn.getTiedNames().length != 1)
        {
            throw new RuntimeException("WebSocket函数的@PortIn只能设置一个tieds只能设置一个元素");
        }

        wsConfig.setMaxBinaryBuffer(webSocket.maxBinaryBuffer());
        wsConfig.setMaxTextBuffer(webSocket.maxTextBuffer());
        wsConfig.setMaxIdleTime(webSocket.maxIdleTime());
        wsConfig.setPartial(webSocket.isPartial());

        //进行配置
        fun.invokeByHandleArgs(oftenObject, WS.newWS(State.DEFAULT, WebSocket.Type.ON_CONFIG, null, true, wsConfig));

        WebSocketOption webSocketOption = wsConfig.getWebSocketOption();

        this.path = fun.getPath();
        ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, this.path);
        if (webSocketOption != null)
        {
            if (webSocketOption.getEncoders() != null)
            {
                builder.encoders(Arrays.asList(webSocketOption.getEncoders()));
            }

            if (webSocketOption.getDecoders() != null)
            {
                builder.decoders(Arrays.asList(webSocketOption.getDecoders()));
            }

            if (webSocketOption.getSubprotocols() != null)
            {
                builder.subprotocols(Arrays.asList(webSocketOption.getSubprotocols()));
            }

            if (webSocketOption.getExtensions() != null)
            {
                builder.extensions(Arrays.asList(webSocketOption.getExtensions()));
            }

            builder.configurator(new HttpSessionConfigurator(webSocketOption.getConfigurator()));
        } else
        {
            builder.configurator(new HttpSessionConfigurator(null));
        }
        ServerEndpointConfig config = builder.build();
        serverContainer.addEndpoint(config);
        webSocketPaths.add(this.path);
    }


    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((key, values) -> {//保留请求的参数。
            if (values != null && values.length > 0)
            {
                if (OftenTool.isNullOrEmptyCharSequence(oftenObject.url().getParam(key)))
                {
                    oftenObject.url().setParam(key, values[0]);
                }
            }
        });

        if (webSocket.needConnectingState())
        {
            WS ws = WS.newWS(State.DEFAULT, WebSocket.Type.ON_CONNECTING, null, true, (Connecting) will -> {
                try
                {
                    if (will)
                    {
                        initAttr(oftenObject, porterOfFun);
                    } else
                    {
                        oftenObject.getResponse().close();
                    }
                } catch (Exception e)
                {
                    LOGGER.debug(e.getMessage(), e);
                }

            });
            porterOfFun.invokeByHandleArgs(oftenObject, ws);
        } else
        {
            initAttr(oftenObject, porterOfFun);
        }
        return null;
    }

    private void initAttr(OftenObject oftenObject, PorterOfFun porterOfFun) throws ServletException, IOException
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();

        BridgeData bridgeData = new BridgeData(oftenObject, porterOfFun, webSocket, wsConfig);
        HttpSession session = request.getSession();
        oftenObject.putRequestData(HttpSession.class, session);
        session.setAttribute(BridgeData.class.getName(), bridgeData);
        request.setAttribute(BridgeData.class.getName(), true);
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }

    @Override
    public boolean needInvoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn)
    {
        return true;
    }

    public static void initFilter(ServletContext servletContext)
    {
        if (servletContext.getAttribute(OftenWebSocketFilter.class.getName()) != null)
        {
            return;
        }

        OftenWebSocketFilter oftenWebSocketFilter = new OftenWebSocketFilter();
        FilterRegistration.Dynamic dynamic = servletContext.addFilter(OftenWebSocketFilter.class.getName(),
                oftenWebSocketFilter);
        dynamic.setAsyncSupported(true);
        dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false,
                "/*");
        servletContext.setAttribute(OftenWebSocketFilter.class.getName(), oftenWebSocketFilter);
    }

}
