package cn.xishan.oftenporter.servlet.tomcat;

import cn.xishan.oftenporter.servlet.tomcat.websocket.server.Constants;
import cn.xishan.oftenporter.servlet.tomcat.websocket.server.WsServerContainer;
import cn.xishan.oftenporter.servlet.tomcat.websocket.server.WsSessionListener;

import javax.servlet.ServletContext;

/**
 * @author Created by https://github.com/CLovinr on 2018/11/3.
 */
public class WsMain
{
    private static WsServerContainer sc = null;

    public static WsServerContainer getInstance()
    {
        return sc;
    }

    public static void destroy(){
        WsServerContainer wsServerContainer = getInstance();
        if (wsServerContainer != null)
        {
            sc=null;
            wsServerContainer.destroy();
        }
    }

    public static WsServerContainer init(ServletContext servletContext)
    {
        sc = WsServerContainer.newInstance(servletContext);
        servletContext.setAttribute(
                Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE, sc);
        servletContext.addListener(new WsSessionListener(sc));
        return sc;
    }
}
