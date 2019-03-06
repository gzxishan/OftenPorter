package cn.xishan.oftenporter.demo.servlet.demo1;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.xishan.oftenporter.porter.core.base.CheckHandle;
import cn.xishan.oftenporter.servlet.StartupServlet;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.demo.bridge.http.MyHServerServlet;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeCallback;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker.Direction;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.bridge.BridgeResponse;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;

/**
 * @author https://github.com/CLovinr <br>
 *         2016年9月6日 下午11:55:04
 */
@WebServlet(name = "PorterServlet", urlPatterns = "/S/*", loadOnStartup = 5,
        initParams = {@WebInitParam(name = "bridgeName", value = "Servlet1")})
public class MyOftenServlet extends StartupServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MyOftenServlet.class);

    public MyOftenServlet()
    {
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        req.setAttribute("time", System.nanoTime());
        super.doGet(req, resp);
    }

    @Override
    public void onStart()
    {
        PropertyConfigurator
                .configure(getClass().getResource("/log4j.properties"));
        PorterConf porterConf = newPorterConf();

        porterConf.addContextCheck(new CheckPassable()
        {

            @Override
            public void willPass(OftenObject oftenObject, DuringType type, CheckHandle checkHandle)
            {
                //LOGGER.debug("");
                checkHandle.next();
            }

        });

        porterConf.getSeekPackages()
                .addPackages(getClass().getPackage().getName() + ".porter");
        porterConf.setContextName("T1");

        startOne(porterConf);

        BridgeLinker servletInit = getBridgeLinker();

        LocalMain localMain = new LocalMain(true, new BridgeName("local"), "utf-8");
        PorterConf porterConf2 = localMain.newPorterConf();
        porterConf2.setContextName("T2");
        porterConf2.getSeekPackages()
                .addPackages(getClass().getPackage().getName() + ".lporter");
        localMain.startOne(porterConf2);

        localMain.getBridgeLinker().link(servletInit, Direction.BothAll);
        MyHServerServlet.bridgeLinker = getBridgeLinker();

        BridgeRequest request = new BridgeRequest(":Servlet1/T1/Hello/say")
                .addParam("name", "xiaoming").addParam("age", 15)
                .addParam("sex", "男");

        servletInit.currentBridge().request(request, new BridgeCallback()
        {

            @Override
            public void onResponse(BridgeResponse lResponse)
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            }
        });

        servletInit.toAllBridge().request(request, new BridgeCallback()
        {

            @Override
            public void onResponse(BridgeResponse lResponse)
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            }
        });

        localMain.getBridgeLinker().toAllBridge().request(request, new BridgeCallback()
        {

            @Override
            public void onResponse(BridgeResponse lResponse)
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);

            }
        });
    }
}
