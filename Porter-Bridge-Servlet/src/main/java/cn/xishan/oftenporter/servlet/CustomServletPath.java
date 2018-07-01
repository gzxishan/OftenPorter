package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2018/3/9.
 */
public class CustomServletPath
{
    private static final Logger LOGGER = LogUtil.logger(CustomServletPath.class);
    private String urlPattern;
    private String porterContext;
    private Class<?> porterClass;


    /**
     * 假设host为http://localhost:8080,ServletContextName为/Hello,urlPatter为/op-porter/*,porterContext为1_0,
     * UserPorter的class绑定名为User:
     * <p>当添加了new CustomServletPath("/back2/*","1_0",UserPorter.class)
     * ，则访问http://localhost:8080/Hello/back2/say等同于访问http://localhost:8080/Hello/op-porter/1_0/User/say</p>
     *
     * @param urlPattern    以"/"开头、"*"结尾
     * @param porterContext
     * @param porterClass
     */
    public CustomServletPath(String urlPattern, String porterContext, Class<?> porterClass)
    {
        if (!urlPattern.startsWith("/") && !urlPattern.endsWith("*"))
        {
            throw new IllegalArgumentException("urlPattern is illegal");
        }
        this.urlPattern = urlPattern;
        this.porterContext = porterContext;
        this.porterClass = porterClass;
    }

    public String getUrlPattern()
    {
        return urlPattern;
    }

    public String getPorterContext()
    {
        return porterContext;
    }

    public Class<?> getPorterClass()
    {
        return porterClass;
    }

    void regServlet(StartupServlet startupServlet)
    {
        String pathPrefix = "/" + porterContext + "/" + PortUtil.tied(porterClass);
        CustomServlet customServlet = new CustomServlet(startupServlet, pathPrefix);
        ServletContext servletContext = startupServlet.getServletContext();
        ServletRegistration.Dynamic dynamic = servletContext
                .addServlet(urlPattern + "---->" + pathPrefix, customServlet);
        LOGGER.debug("bind custom path:{}---->{}", urlPattern, pathPrefix);
        dynamic.setAsyncSupported(true);
        dynamic.addMapping(urlPattern);
    }

    static class CustomServlet extends HttpServlet
    {
        private StartupServlet startupServlet;
        private String pathPrefix;

        public CustomServlet(StartupServlet startupServlet, String pathPrefix)
        {
            this.startupServlet = startupServlet;
            this.pathPrefix = pathPrefix;
        }

        @Override
        protected void service(HttpServletRequest request,
                HttpServletResponse response) throws ServletException, IOException
        {
            String method = request.getMethod();
            if (method.equals("GET"))
            {
                doRequest(request, response, PortMethod.GET);
            } else if (method.equals("HEAD"))
            {
                doRequest(request, response, PortMethod.HEAD);
            } else if (method.equals("POST"))
            {
                doRequest(request, response, PortMethod.POST);
            } else if (method.equals("PUT"))
            {
                doRequest(request, response, PortMethod.PUT);
            } else if (method.equals("DELETE"))
            {
                doRequest(request, response, PortMethod.DELETE);
            } else if (method.equals("OPTIONS"))
            {
                doRequest(request, response, PortMethod.OPTIONS);
            } else if (method.equals("TRACE"))
            {
                doRequest(request, response, PortMethod.TARCE);
            } else
            {
                super.service(request, response);
            }

        }

        void doRequest(HttpServletRequest request, HttpServletResponse response, PortMethod method) throws IOException
        {
            String path = OPServlet.getPath(request);
            if (!path.startsWith("/"))
            {
                path = pathPrefix + "/" + path;
            } else
            {
                path = pathPrefix + path;
            }
            startupServlet.doRequest(request, path, response, method);
        }
    }
}
