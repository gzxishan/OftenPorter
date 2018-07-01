package cn.xishan.oftenporter.servlet.websocket;


import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator
{
//    private OPServlet mainServlet;
//
//    public HttpSessionConfigurator(OPServlet servlet)
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