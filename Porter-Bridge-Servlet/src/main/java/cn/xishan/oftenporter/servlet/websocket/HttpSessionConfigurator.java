package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.util.LogUtil;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator
{

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
            HandshakeRequest request, HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }


}