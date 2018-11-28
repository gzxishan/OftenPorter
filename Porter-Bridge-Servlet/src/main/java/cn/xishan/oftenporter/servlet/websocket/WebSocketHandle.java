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

    private static Set<String> webSocketPaths = ConcurrentHashMap.newKeySet();

    @AutoSet
    private ServerContainer serverContainer;


    @AutoSet.SetOk
    public void setOk(OftenObject oftenObject) throws Exception
    {
        initWithServerContainer(oftenObject, thePorterOfFun);
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

    private void initWithServerContainer(OftenObject oftenObject, PorterOfFun fun) throws Exception
    {
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
        fun.invokeByHandleArgs(oftenObject, WS.newWS(WebSocket.Type.ON_CONFIG, null, true, wsConfig));

        WebSocketOption webSocketOption = wsConfig.getWebSocketOption();

        this.path = fun.getPath();
        ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, this.path);
        if (webSocketOption != null)
        {
            builder.encoders(Arrays.asList(webSocketOption.getEncoders()));
            builder.decoders(Arrays.asList(webSocketOption.getDecoders()));
            builder.subprotocols(Arrays.asList(webSocketOption.getSubprotocols()));
            builder.extensions(Arrays.asList(webSocketOption.getExtensions()));
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
        if (webSocket.needConnectingState())
        {
            WS ws = WS.newWS(WebSocket.Type.ON_CONNECTING, null, true, (Connecting) will -> {
                try
                {
                    if (will)
                    {
                        doConnect(oftenObject, porterOfFun);
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
            doConnect(oftenObject, porterOfFun);
        }
        return null;
    }

    private void doConnect(OftenObject oftenObject, PorterOfFun porterOfFun) throws ServletException, IOException
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();

        BridgeData bridgeData = new BridgeData(oftenObject, porterOfFun, webSocket, wsConfig);
        HttpSession session = request.getSession();
        session.setAttribute(BridgeData.class.getName(), bridgeData);
        request.setAttribute(BridgeData.class.getName(), true);

    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }


//    public static void handleWS(ServletContext servletContext)
//    {
//        try
//        {
//            //对jetty的修复。
//            String clazz = "org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter";
//            String key = clazz;
//            servletContext.removeAttribute(key);
//            Class<?> filterClazz = PackageUtil.newClass(clazz, null);
//            if (filterClazz == null)
//            {
//                LOGGER.debug("{} is null.", clazz);
//                return;
//            }
//            Filter filter = (Filter) OftenTool.newObject(filterClazz);
//            String pathSpc = XSServletWSConfig.WS_PATH;
//            FilterRegistration.Dynamic dynamic = servletContext.addFilter(WebSocketHandle.class.getName(), filter);
//            dynamic.setAsyncSupported(true);
//            //支持DispatcherType.FORWARD方式，跳转到对应的websocket上。
//            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false,
//                    pathSpc);
//        } catch (Exception e)
//        {
//            if (e instanceof ClassNotFoundException)
//            {
//                return;
//            }
//            LOGGER.debug("handle jetty error:{}", e);
//        }
//    }


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
