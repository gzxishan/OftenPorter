package cn.xishan.oftenporter.demo.bridge.http;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.servlet.OPServlet;

@WebServlet(urlPatterns = "/RemoteBridge/*", loadOnStartup = 10,
        initParams = {@WebInitParam(name = "pname", value = "HServer"),
                @WebInitParam(name = "responseWhenException", value = "true")})
public class MyHServerServlet extends OPServlet
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
    public void init() throws ServletException
    {
        super.init();
    }

    @Override
    public PLinker getPLinker()
    {
        return pLinker;
    }
}
