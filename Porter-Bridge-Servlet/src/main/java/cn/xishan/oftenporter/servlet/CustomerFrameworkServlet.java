package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import javax.servlet.*;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 用于自定义框架。
 *
 * @author Created by https://github.com/CLovinr on 2019-08-20.
 */
public abstract class CustomerFrameworkServlet extends StartupServlet
{
    private static int count = 0;
    protected ServletContext servletContext;
    private String servletName;
    /**
     * 是否立即启动
     */
    private boolean startImmediately = true;

    public CustomerFrameworkServlet(ServletContext servletContext)
    {
        this.servletContext = servletContext;
        this.servletName = this.toString();

    }

    public CustomerFrameworkServlet(ServletContext servletContext, String servletName)
    {
        this(servletContext);
        this.servletName = servletName;
    }


    @Override
    public ServletContext getServletContext()
    {
        return servletContext;
    }

    @Override
    public String getServletName()
    {
        return servletName;
    }

    @Override
    public final void init(ServletConfig config) throws ServletException
    {
        if (!startImmediately)
        {
            //1.启动框架
            super.init(config);
            //2.初始化WebSocket、Filterer等
            doSysInit();
        } else
        {
            if (isInit())
            {//第二次：由servlet初始化时进行调用,此时filter已经初始化完毕
                // 初始化WebSocket、Filterer等
                doSysInit();
            } else
            {//第一次：将会初始化框架
                super.init(config);
            }
        }
    }

    /**
     * 启动框架，注意需要使用{@linkplain ServletContext#addServlet(String, Servlet)}进行注册、且为自启动。
     * <p>
     * 请调用{@linkplain #registerServlet()}注册servlet。
     * </p>
     *
     * @param startImmediately 是否立即启动,为false则表示在OftenPorter框架启动后再启动、这样可以保证OftenPorter正常。
     * @throws ServletException
     */
    public void doStart(boolean startImmediately) throws ServletException
    {
        this.startImmediately = startImmediately;
        if (startImmediately)
        {
            ServletConfig servletConfig = new ServletConfig()
            {
                @Override
                public String getServletName()
                {
                    return servletName;
                }

                @Override
                public ServletContext getServletContext()
                {
                    return servletContext;
                }

                @Override
                public String getInitParameter(String name)
                {
                    return null;
                }

                @Override
                public Enumeration<String> getInitParameterNames()
                {
                    return new EnumerationImpl<>(Collections.emptyList());
                }
            };
            init(servletConfig);
        } else
        {
            OftenServletContainerInitializer.onReady(this);
        }
    }

    /**
     * 注意：无需设置{@linkplain javax.servlet.ServletRegistration.Dynamic#setLoadOnStartup(int)}
     *
     * @return
     */
    /**
     * 注意：无需设置{@linkplain javax.servlet.ServletRegistration.Dynamic#setLoadOnStartup(int)}
     *
     * @return
     */
    public ServletRegistration.Dynamic registerServlet()
    {
        return registerServlet(OftenServletContainerInitializer.BRIDGE_SERVLET_LOAD_VALUE + 100 + count++);
    }

    ServletRegistration.Dynamic registerServlet(int loadOnStartup)
    {
        ServletRegistration.Dynamic dynamic = servletContext
                .addServlet(getServletName(), this);
        dynamic.setAsyncSupported(true);
        dynamic.setLoadOnStartup(loadOnStartup);
        return dynamic;
    }
}
