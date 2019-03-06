package cn.xishan.oftenporter.demo.bridge.http;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.servlet.OftenServlet;

@WebServlet(urlPatterns = "/RemoteBridge/*", loadOnStartup = 10,
        initParams = {@WebInitParam(name = "bridgeName", value = "HServer"),
                @WebInitParam(name = "responseWhenException", value = "true")})
public class MyHServerServlet extends OftenServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //public static Delivery delivery;
    public static BridgeLinker bridgeLinker;

    public MyHServerServlet()
    {

    }

    @Override
    public void init() throws ServletException
    {
        super.init();
    }

    @Override
    public BridgeLinker getBridgeLinker()
    {
        return bridgeLinker;
    }
}
