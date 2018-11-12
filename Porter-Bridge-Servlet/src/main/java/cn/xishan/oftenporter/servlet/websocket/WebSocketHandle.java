package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.servlet.tomcat.WebSocketOption;
import cn.xishan.oftenporter.servlet.tomcat.WsMain;
import cn.xishan.oftenporter.servlet.tomcat.websocket.server.UpgradeUtil;
import cn.xishan.oftenporter.servlet.tomcat.websocket.server.WsMappingResult;
import cn.xishan.oftenporter.servlet.tomcat.websocket.server.WsServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WebSocketHandle extends AspectOperationOfPortIn.HandleAdapter<WebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandle.class);
    private WSConfig wsConfig = new WSConfig();
    private WebSocket webSocket;
    private PorterOfFun thePorterOfFun;

    @AutoSet.SetOk
    public void setOk(WObject wObject) throws Exception
    {
        initWithServerContainer(wObject, thePorterOfFun);
    }

    @Override
    public boolean init(WebSocket webSocket, IConfigData configData, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        this.thePorterOfFun = porterOfFun;
        return true;
    }

    private void initWithServerContainer(WObject wObject, PorterOfFun fun) throws Exception
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
        fun.invokeByHandleArgs(wObject, WS.newWS(WebSocket.Type.ON_CONFIG, null, true, wsConfig));

        WebSocketOption webSocketOption = wsConfig.getWebSocketOption();

        WsServerContainer wsServerContainer = WsMain.getInstance();
        String path = fun.getPath();
        ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, path)
                .configurator(new HttpSessionConfigurator());
        if (webSocketOption != null)
        {
            builder.encoders(Arrays.asList(webSocketOption.getEncoders()));
            builder.decoders(Arrays.asList(webSocketOption.getDecoders()));
            builder.subprotocols(Arrays.asList(webSocketOption.getSubprotocols()));
            builder.extensions(Arrays.asList(webSocketOption.getExtensions()));
        }
        ServerEndpointConfig config = builder.build();
        wsServerContainer.addEndpoint(config);
    }


    @Override
    public Object invoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
        if (!UpgradeUtil.isWebSocketUpgradeRequest(request, response))
        {
            response.sendError(400);
            return null;
        }
        if (webSocket.needConnectingState())
        {
            WS ws = WS.newWS(WebSocket.Type.ON_CONNECTING, null, true, (Connecting) will -> {
                try
                {
                    if (will)
                    {
                        doConnect(wObject, porterOfFun);
                    } else
                    {
                        wObject.getResponse().close();
                    }
                } catch (Exception e)
                {
                    LOGGER.debug(e.getMessage(), e);
                }

            });
            porterOfFun.invokeByHandleArgs(wObject, ws);
        } else
        {
            doConnect(wObject, porterOfFun);
        }


        return null;
    }

    private void doConnect(WObject wObject, PorterOfFun porterOfFun) throws ServletException, IOException
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();

        WsServerContainer wsServerContainer = WsMain.getInstance();
        String path = porterOfFun.getPath();
        WsMappingResult wsMappingResult = wsServerContainer.findMapping(path);

        BridgeData bridgeData = new BridgeData(wObject, porterOfFun, webSocket, wsConfig);
        request.setAttribute(BridgeData.class.getName(), bridgeData);

        UpgradeUtil.doUpgrade(wsServerContainer, request, response, wsMappingResult.getConfig(),
                wsMappingResult.getPathParams());
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
//            Filter filter = (Filter) WPTool.newObject(filterClazz);
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
}
