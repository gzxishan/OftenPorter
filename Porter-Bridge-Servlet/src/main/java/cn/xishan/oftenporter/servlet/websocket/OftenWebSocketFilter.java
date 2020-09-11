package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.servlet.OftenServletResponse;
import cn.xishan.oftenporter.servlet.StartupServlet;
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
        StartupServlet oftenServlet = (StartupServlet) request.getServletContext()
                .getAttribute(StartupServlet.class.getName());
        if (oftenServlet != null && WebSocketHandle.isWebSocket(request))
        {
            //接入框架进行处理
            oftenServlet.service(request, response);

            if (request.getAttribute(OftenServletResponse.class.getName()) == null)
            {//处理正常，连接未关闭
                HttpSessionConfigurator.setSession(request.getSession());
            }

            request = IContainerResource.getOrigin(request);
            response = IContainerResource.getOrigin(response);
        }

        chain.doFilter(request, response);
    }
}
