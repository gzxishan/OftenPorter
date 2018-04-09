package cn.xishan.oftenporter.servlet.websocket;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.servlet.WMainServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator
{
//    private WMainServlet mainServlet;
//
//    public HttpSessionConfigurator(WMainServlet servlet)
//    {
//        this.mainServlet = servlet;
//    }

    public HttpSessionConfigurator()
    {
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
            HandshakeRequest request, HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
//        if (mainServlet != null)
//        {
//            HttpServletRequest servletRequest = (HttpServletRequest) httpSession
//                    .getAttribute(HttpServletRequest.class.getName());
//            httpSession.removeAttribute(HttpServletRequest.class.getName());
//            HttpServletResponse servletResponse = (HttpServletResponse) httpSession
//                    .getAttribute(HttpServletResponse.class.getName());
//            httpSession.removeAttribute(HttpServletResponse.class.getName());
//            try
//            {
//                mainServlet.doRequest(servletRequest, null, servletResponse, PortMethod.GET);
//            } catch (IOException e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
    }


}