package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.servlet.OftenServlet;
import cn.xishan.oftenporter.servlet._AllFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-13.
 */
//@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, asyncSupported =
//        true)
public final class OftenWebSocketFilter extends _AllFilter.FilterX
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenWebSocketFilter.class);

    OftenWebSocketFilter()
    {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        LOGGER.debug("{}", filterConfig);
    }

    @Override
    public void destroy()
    {

    }

    @Override
    public void doSelf(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        OftenServlet oftenServlet = (OftenServlet) request.getServletContext()
                .getAttribute(OftenServlet.class.getName());
        if (oftenServlet != null && WebSocketHandle.isWebSocket(request))
        {
            //接入框架进行处理
            oftenServlet.service(request, response);
//            if (request.getAttribute(BridgeData.class.getName()) == null)
//            {//已经成功接入websocket
//                return;
//            }
        }
        chain.doFilter(request, response);
    }
}
