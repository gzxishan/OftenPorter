package cn.xishan.oftenporter.demo.bridge.http;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.servlet.WMainServlet;

@WebServlet(urlPatterns = "/RemoteBridge/*", loadOnStartup = 10,
	initParams = { @WebInitParam(name = "pname", value = "HServer"),
		@WebInitParam(name = "urlEncoding", value = "utf-8"),
            @WebInitParam(name = "responseWhenException", value = "true"),
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
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public PLinker getPLinker() {
        return pLinker;
    }
}
