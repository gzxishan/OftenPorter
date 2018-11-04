package cn.xishan.oftenporter.servlet.websocket;


import cn.xishan.oftenporter.servlet.tomcat.websocket.server.WsHandshakeRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator
{

    public HttpSessionConfigurator()
    {
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
            HandshakeRequest request, HandshakeResponse response)
    {
        WsHandshakeRequest handshakeRequest = (WsHandshakeRequest) request;
        HttpServletRequest servletRequest = handshakeRequest.getHttpServletRequest();
        BridgeData bridgeData = (BridgeData) servletRequest.getAttribute(BridgeData.class.getName());
        servletRequest.removeAttribute(BridgeData.class.getName());
        sec.getUserProperties().put(BridgeData.class.getName(), bridgeData);
    }


}