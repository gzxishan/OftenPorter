package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.websocket.server.ServerContainer;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WebSocketHandle extends AspectOperationOfPortIn.HandleAdapter<WebSocket>
{
    public static final String WS_SERVER_CONTAINER_NAME = "javax.websocket.server.ServerContainer";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandle.class);
    private WebSocket webSocket;

    @AutoSet(nullAble = true)
    ServerContainer serverContainer;
    //    private static String wsPath;
    //    @AutoSet
//    ServletContext servletContext;
//    @AutoSet(value = StartupServlet.SERVLET_NAME_NAME)
//    String servletName;
//    @AutoSet
//    OPServlet mainServlet;
    private PorterOfFun thePorterOfFun;

    @AutoSet.SetOk
    public void setOk()
    {
        if (serverContainer != null)
        {
            try
            {
                initWithServerContainer(thePorterOfFun);
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean init(WebSocket webSocket,IConfigData configData, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        this.thePorterOfFun = porterOfFun;
        return true;
    }

    private void initWithServerContainer(PorterOfFun fun) throws Exception
    {
        _PortIn funIn = fun.getMethodPortIn();
        if (funIn.getMethods().length != 1 || funIn.getMethods()[0] != PortMethod.GET)
        {
            throw new RuntimeException("WebSocket函数的@PortIn只能设置一个method方法且必须为GET");
        } else if (funIn.getTiedNames().length != 1)
        {
            throw new RuntimeException("WebSocket函数的@PortIn只能设置一个tieds只能设置一个元素");
        }

//        synchronized (WebSocketHandle.class)
//        {
//            if (wsPath == null)
//            {
//                try
//                {
//                    wsPath = "/" + KeyUtil.secureRandomKeySha256(128);
//                } catch (Exception e)
//                {
//                    wsPath = "/" + KeyUtil.random48Key() + KeyUtil.random48Key();
//                }
//
//                ServerEndpointConfig config = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, wsPath)
//                        .configurator(new HttpSessionConfigurator())
//                        .build();
//                serverContainer.addEndpoint(config);
//            }
//        }

//        Porter porter = fun.getFinalPorter();
//        String[] mappings = servletContext.getServletRegistration(servletName).getMappings().toArray(new String[0]);
//
//        String classTied = porter.getPortIn().getTiedNames()[0];
//        String funTied = funIn.getTiedNames()[0];
//        String[] paths = {
//                "/" + porter.getContextName() + "/" + classTied + "/" + funTied,
//                "/=" + porter.getPName().getName() + "/" + porter.getContextName() + "/" + classTied + "/" + funTied
//        };
//
//        List<String> pathList = new ArrayList<>();
//        for (String mapping : mappings)
//        {
//            if (mapping.endsWith("/*"))
//            {
//                mapping = mapping.substring(0, mapping.length() - 2);
//            } else if (mapping.endsWith("/"))
//            {
//                mapping = mapping.substring(0, mapping.length() - 1);
//            }
//            for (String path : paths)
//            {
//
//                String wsPath = mapping + path;
//                pathList.add(wsPath);
//                ServerEndpointConfig config = ServerEndpointConfig.Builder.create(ProgrammaticServer.class, wsPath)
//                        .configurator(new HttpSessionConfigurator(mainServlet))
//                        .build();
//                serverContainer.addEndpoint(config);
//            }
//        }
//
//        FilterRegistration.Dynamic registration = servletContext
//                .addFilter(servletName + ".WebSocket.Filter-"+hashCode(), new Filter()
//                {
//                    @Override
//                    public void init(FilterConfig filterConfig) throws ServletException
//                    {
//
//                    }
//
//                    @Override
//                    public void doFilter(ServletRequest request, ServletResponse response,
//                            FilterChain chain) throws IOException, ServletException
//                    {
//                        HttpServletRequest req = (HttpServletRequest) request;
//                        HttpSession session = req.getSession();
//                        session.setAttribute(HttpServletRequest.class.getName(), req);
//                        session.setAttribute(HttpServletResponse.class.getName(), response);
//                        chain.doFilter(request, response);
//                    }
//
//                    @Override
//                    public void destroy()
//                    {
//
//                    }
//                });
//        registration
//                .addMappingForUrlPatterns(
//                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.FORWARD), false,
//                        pathList.toArray(new String[0]));
//        registration.setAsyncSupported(true);

    }


    @Override
    public Object invoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
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
            porterOfFun.invoke(new Object[]{wObject, ws});
        } else
        {
            doConnect(wObject, porterOfFun);
        }


        return null;
    }

    private void doConnect(WObject wObject, PorterOfFun porterOfFun) throws ServletException, IOException
    {
//        if (serverContainer != null&&wsPath==null)
//        {
//            return;
//        }
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
        RequestDispatcher requestDispatcher = request
                .getRequestDispatcher(/*wsPath != null ? wsPath :*/ XSServletWSConfig.WS_PATH);
        HttpSession session = request.getSession();

        session.setAttribute(WObject.class.getName(), wObject);
        session.setAttribute(PorterOfFun.class.getName(), porterOfFun);
        session.setAttribute(WebSocket.class.getName(), webSocket);

        requestDispatcher.forward(request, response);
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }


    public static void handleWS(ServletContext servletContext)
    {
        try
        {
            //对jetty的修复。
            String clazz = "org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter";
            String key = clazz;
            servletContext.removeAttribute(key);
            Class<?> filterClazz = PackageUtil.newClass(clazz, null);
            if (filterClazz == null)
            {
                LOGGER.debug("{} is null.", clazz);
                return;
            }
            Filter filter = (Filter) WPTool.newObject(filterClazz);
            String pathSpc = XSServletWSConfig.WS_PATH;
            FilterRegistration.Dynamic dynamic = servletContext.addFilter(WebSocketHandle.class.getName(), filter);
            dynamic.setAsyncSupported(true);
            //支持DispatcherType.FORWARD方式，跳转到对应的websocket上。
            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false,
                    pathSpc);
        } catch (Exception e)
        {
            if (e instanceof ClassNotFoundException)
            {
                return;
            }
            LOGGER.debug("handle jetty error:{}", e);
        }
    }
}
