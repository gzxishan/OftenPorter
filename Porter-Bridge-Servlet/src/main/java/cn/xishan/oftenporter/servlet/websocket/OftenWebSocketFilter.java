package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.servlet.OftenServlet;
import cn.xishan.oftenporter.servlet.WrapperFilterManager;
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
public class OftenWebSocketFilter implements Filter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenWebSocketFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        LOGGER.debug("{}", filterConfig);
    }

    @Override
    public void doFilter(ServletRequest _servletRequest, ServletResponse _servletResponse,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) _servletRequest;
        HttpServletResponse response = (HttpServletResponse) _servletResponse;

        WrapperFilterManager wrapperFilterManager = WrapperFilterManager
                .getWrapperFilterManager(request.getServletContext());
        for (WrapperFilterManager.WrapperFilter wrapperFilter : wrapperFilterManager.wrapperFilters())
        {
            WrapperFilterManager.Wrapper wrapper = wrapperFilter.doFilter(request, response);
            if (wrapper != null)
            {
                request = wrapper.getRequest();
                response = wrapper.getResponse();
            }
        }

        OftenServlet oftenServlet = (OftenServlet) request.getServletContext()
                .getAttribute(OftenServlet.class.getName());
        if (WebSocketHandle.isWebSocket(request))
        {
            //接入框架进行处理
            oftenServlet.service(request, response);
            if (request.getAttribute(BridgeData.class.getName()) == null)
            {//已经成功接入websocket
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {

    }
}
