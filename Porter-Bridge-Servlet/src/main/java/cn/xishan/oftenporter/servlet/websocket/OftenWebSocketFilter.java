package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.servlet.OPServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-13.
 */
//@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, asyncSupported =
//        true)
public class OftenWebSocketFilter implements Filter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenWebSocketFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        LOGGER.debug("{}", filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        OPServlet opServlet = (OPServlet) request.getServletContext().getAttribute(OPServlet.class.getName());
        if (WebSocketHandle.isWebSocket(request))
        {
            //接入框架进行处理
            opServlet.service(request, response);
            if (request.getAttribute(BridgeData.class.getName()) == null)
            {//已经成功接入websocket
                return;
            }
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }
}
