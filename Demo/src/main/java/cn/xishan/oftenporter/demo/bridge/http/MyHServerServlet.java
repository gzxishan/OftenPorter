package cn.xishan.oftenporter.demo.bridge.http;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.servlet.WMainServlet;

@WebServlet(urlPatterns = "/RemoteBridge/*", loadOnStartup = 10,
	initParams = { @WebInitParam(name = "pname", value = "HServer"),
		@WebInitParam(name = "contextName", value = "C"),
		@WebInitParam(name = "urlPatternPrefix", value = "/RemoteBridge") })
public class MyHServerServlet extends WMainServlet
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    //public static Delivery delivery;
    public static PLinker pLinker;

    public MyHServerServlet()
    {

    }


    @Override
    public PLinker getPLinker() {
        return pLinker;
    }
}
