package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class WebSocketHandle extends AspectFunOperation.HandleAdapter<WebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandle.class);
    private WebSocket webSocket;

    @Override
    public boolean init(WebSocket webSocket, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        return true;
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
                        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
                        response.getWriter().close();
                    }
                } catch (Exception e)
                {
                    LOGGER.debug(e.getMessage(), e);
                }

            });
            porterOfFun.invoke(wObject, new Object[]{wObject, ws});
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
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(XSServletWSConfig.WS_PATH);
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
            String pathSpc =  XSServletWSConfig.WS_PATH;
            FilterRegistration.Dynamic dynamic = servletContext.addFilter(WebSocketHandle.class.getName(), filter);
            dynamic.setAsyncSupported(true);
            //支持DispatcherType.FORWARD方式，跳转到对应的websocket上。
            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false,
                    pathSpc);
        } catch (Exception e)
        {
            LOGGER.debug("handle jetty error:{}", e);
        }
    }
}
