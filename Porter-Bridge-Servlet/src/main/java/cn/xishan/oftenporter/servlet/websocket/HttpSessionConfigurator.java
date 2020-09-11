package cn.xishan.oftenporter.servlet.websocket;


import javax.servlet.http.HttpSession;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator
{

    private ServerEndpointConfig.Configurator customer;
    private static final ThreadLocal<HttpSession> SESSION_THREAD_LOCAL = new ThreadLocal<>();

    public HttpSessionConfigurator(ServerEndpointConfig.Configurator customer)
    {
        this.customer = customer;
    }

    static void setSession(HttpSession session)
    {
        SESSION_THREAD_LOCAL.set(session);
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
            HandshakeRequest request, HandshakeResponse response)
    {
        HttpSession httpSession = SESSION_THREAD_LOCAL.get();
        SESSION_THREAD_LOCAL.remove();

        BridgeData bridgeData = (BridgeData) httpSession.getAttribute(BridgeData.class.getName());
        httpSession.removeAttribute(BridgeData.class.getName());

        sec.getUserProperties().put(BridgeData.class.getName(), bridgeData.gotData());
        sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
        if (customer != null)
        {
            customer.modifyHandshake(sec, request, response);
        }
    }


    @Override
    public String getNegotiatedSubprotocol(List<String> supported, List<String> requested)
    {
        if (customer != null)
        {
            return customer.getNegotiatedSubprotocol(supported, requested);
        }
        return super.getNegotiatedSubprotocol(supported, requested);
    }

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested)
    {
        if (customer != null)
        {
            return customer.getNegotiatedExtensions(installed, requested);
        }
        return super.getNegotiatedExtensions(installed, requested);
    }

    @Override
    public boolean checkOrigin(String originHeaderValue)
    {
        if (customer != null)
        {
            return customer.checkOrigin(originHeaderValue);
        }
        return super.checkOrigin(originHeaderValue);
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException
    {
        if (customer != null)
        {
            return customer.getEndpointInstance(endpointClass);
        }
        return super.getEndpointInstance(endpointClass);
    }
}