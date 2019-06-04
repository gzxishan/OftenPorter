package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.sysset.IAutoVarGetter;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.websocket.WebSocketHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2018/4/18.
 */
@HandlesTypes(OftenInitializer.class)
public final class OftenServletContainerInitializer implements ServletContainerInitializer
{
    public static final int BRIDGE_SERVLET_LOAD_VALUE = 10;
    public static final String FROM_INITIALIZER_ATTR = "__FROM_INITIALIZER_ATTR__";

    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServletContainerInitializer.class);

    //用于实际的绑定
    static class RealServlet extends HttpServlet
    {
        private BridgeServlet bridgeServlet;
        private OftenInitializer oftenInitializer;

        public RealServlet(BridgeServlet bridgeServlet, OftenInitializer oftenInitializer)
        {
            this.bridgeServlet = bridgeServlet;
            this.oftenInitializer = oftenInitializer;
        }

        @Override
        protected void service(HttpServletRequest request,
                HttpServletResponse response) throws ServletException, IOException
        {
            String method = request.getMethod();
            if (method.equals("GET"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.GET);
            } else if (method.equals("HEAD"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.HEAD);
            } else if (method.equals("POST"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.POST);
            } else if (method.equals("PUT"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.PUT);
            } else if (method.equals("DELETE"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.DELETE);
            } else if (method.equals("OPTIONS"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.OPTIONS);
            } else if (method.equals("TRACE"))
            {
                oftenInitializer.onDoRequest(bridgeServlet, request, response, PortMethod.TARCE);
            } else
            {
                super.service(request, response);
            }

        }
    }

    //只用于启动
    static class BridgeServlet extends StartupServlet implements OftenInitializer.BuilderBefore
    {
        private List<OftenInitializer> servletInitializerList;
        private ServletContext servletContext;
        private String servletName;

        public BridgeServlet(ServletContext servletContext,
                Set<Class<?>> servletInitializerClasses) throws Exception
        {
            super("", true);
            super.doSysInit = false;
            this.servletName = this.toString();
            this.servletContext = servletContext;
            servletInitializerList = new ArrayList<>(servletInitializerClasses.size());
            for (Class<?> clazz : servletInitializerClasses)
            {
                try
                {
                    OftenInitializer initializer = (OftenInitializer) OftenTool.newObject(clazz);
                    initializer.beforeStart(servletContext, this);
                    servletInitializerList.add(initializer);
                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            }
        }

        class BuilderImpl implements OftenInitializer.Builder
        {

            private OftenInitializer initializer;
            private CustomServletPath[] customServletPaths;

            public BuilderImpl(OftenInitializer initializer)
            {
                this.initializer = initializer;
            }

            @Override
            public PorterConf newPorterConfWithImporterClasses(Class... importers)
            {
                return BridgeServlet.this.newPorterConf(importers);
            }

            @Override
            public void startOne(PorterConf porterConf)
            {
                BridgeServlet.this.startOne(porterConf);
//                bindOftenServlet(porterConf.getOftenContextName(), new RealServlet(BridgeServlet.this, initializer));

                String urlPattern = "/" + porterConf.getOftenContextName() + "/*";
                ServletContext servletContext = getServletContext();
                HttpServlet httpServlet = new RealServlet(BridgeServlet.this, initializer);
                ServletRegistration.Dynamic dynamic = servletContext
                        .addServlet(httpServlet.getClass().getName() + "-" + porterConf.getOftenContextName(),
                                httpServlet);
                LOGGER.debug("mapping:{}", urlPattern);
                dynamic.addMapping(urlPattern);
                dynamic.setAsyncSupported(true);
                dynamic.setLoadOnStartup(BRIDGE_SERVLET_LOAD_VALUE);

                if (customServletPaths != null)
                {
                    for (CustomServletPath servletPath : customServletPaths)
                    {
                        servletPath.regServlet(BridgeServlet.this);
                    }
                }
            }

            @Override
            public void setCustomServletPaths(CustomServletPath... customServletPaths)
            {
                this.customServletPaths = customServletPaths;
            }

            @Override
            public BridgeLinker getBridgeLinker()
            {
                return BridgeServlet.this.getBridgeLinker();
            }

            @Override
            public IAutoVarGetter getAutoVarGetter(String context)
            {
                return BridgeServlet.this.getAutoVarGetter(context);
            }
        }

        @Override
        public void init(ServletConfig config) throws ServletException
        {
            if (isInit())
            {//由servlet初始化时进行调用,此时filter已经初始化完毕
                doSysInit();
            } else
            {//直接进行调用
                super.init(config);
            }
        }

        @Override
        public void onStart()
        {
            try
            {
                for (OftenInitializer initializer : servletInitializerList)
                {
                    initializer.onStart(getServletContext(), new BuilderImpl(initializer));
                }
            } catch (Throwable e)
            {
                throw new InitException(e);
            }
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

        void doStart() throws ServletException
        {
            ServletConfig servletConfig = new ServletConfig()
            {
                @Override
                public String getServletName()
                {
                    return BridgeServlet.this.getServletName();
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
                    return null;
                }
            };
            init(servletConfig);
        }

        @Override
        public void destroy()
        {
            super.destroy();
            for (OftenInitializer initializer : servletInitializerList)
            {
                try
                {
                    initializer.onDestroyed();
                } catch (Exception e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        @Override
        public void setMultiPartOption(MultiPartOption multiPartOption)
        {
            super.multiPartOption = multiPartOption;
        }

        @Override
        public void setBridgeName(String bridgeName)
        {
            if (isStarted)
            {
                return;
            }
            super.bridgeName = bridgeName;
        }

        @Override
        public String getBridgeName()
        {
            return bridgeName;
        }

        @Override
        public void setDoPUT(boolean willDo)
        {
            if (isStarted)
            {
                return;
            }
            super.addPutDealt = willDo;
        }

        @Override
        public boolean isDoPUT()
        {
            return addPutDealt;
        }
    }

    @Override
    public void onStartup(Set<Class<?>> servletInitializerClasses,
            ServletContext servletContext) throws ServletException
    {

        Set<Class<?>> initialClasses = new HashSet<>();
        for (Class<?> c : servletInitializerClasses)
        {
            if (!Modifier.isAbstract(c.getModifiers()))
            {
                initialClasses.add(c);
            }
        }
        servletContext.setAttribute(FROM_INITIALIZER_ATTR, true);
        try
        {
            BridgeServlet bridgeServlet = new BridgeServlet(servletContext, initialClasses);
            bridgeServlet.doStart();//直接调用

            ServletRegistration.Dynamic dynamic = servletContext
                    .addServlet(bridgeServlet.getServletName(), bridgeServlet);
            dynamic.setAsyncSupported(true);
            dynamic.setLoadOnStartup(BRIDGE_SERVLET_LOAD_VALUE-1);
        } catch (Throwable e)
        {
            throw new ServletException(e);
        }
        WebSocketHandle.initFilter(servletContext);
//        BridgeFilter.init(servletContext);
    }

//    public static void bindOftenServlet(String oftenContext, HttpServlet servlet)
//    {
//        PortUtil.checkName(oftenContext);
//        BridgeFilter.bind(oftenContext, servlet);
//    }

}
