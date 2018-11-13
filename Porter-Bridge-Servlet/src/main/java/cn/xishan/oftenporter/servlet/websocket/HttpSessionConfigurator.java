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

    public HttpSessionConfigurator()
    {
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
            HandshakeRequest request, HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        BridgeData bridgeData = (BridgeData) httpSession.getAttribute(BridgeData.class.getName());
        sec.getUserProperties().put(BridgeData.class.getName(), bridgeData.gotData());
    }


}