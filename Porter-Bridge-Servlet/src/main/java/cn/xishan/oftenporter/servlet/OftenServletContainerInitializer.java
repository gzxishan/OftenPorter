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
@HandlesTypes({OftenInitializer.class, OftenServerStateListener.class})
public final class OftenServletContainerInitializer implements ServletContainerInitializer
{
    public static final int BRIDGE_SERVLET_LOAD_VALUE = 10;
    public static final String FROM_INITIALIZER_ATTR = "__FROM_INITIALIZER_ATTR__";
    private static List<CustomerFrameworkServlet> customerFrameworkServlets = new ArrayList<>();
    private static boolean isLoad = false;

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
    static class BridgeServlet extends CustomerFrameworkServlet implements OftenInitializer.BuilderBefore
    {
        private List<OftenInitializer> servletInitializerList;
        private ServletContext servletContext;
        private Set<OftenServerStateListener> stateListenerSet;

        public BridgeServlet(ServletContext servletContext,
                Set<OftenServerStateListener> stateListenerSet,
                Set<Class<OftenInitializer>> servletInitializerClasses)
        {
            super(servletContext);
            this.servletContext = servletContext;
            this.stateListenerSet = stateListenerSet;
            servletInitializerList = new ArrayList<>(servletInitializerClasses.size());

            Set<Class<OftenInitializer>> readOnlySet = Collections.unmodifiableSet(servletInitializerClasses);
            Set<OftenInitializer> allInitializers = new HashSet<>();

            for (Class<OftenInitializer> clazz : servletInitializerClasses)
            {
                try
                {
                    OftenInitializer initializer = OftenTool.newObject(clazz);
                    allInitializers.add(initializer);
                    if (initializer.beforeStart(servletContext, this, readOnlySet))
                    {
                        servletInitializerList.add(initializer);
                    }
                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            }

            for (OftenInitializer initializer : allInitializers)
            {
                initializer.beforeStart(servletContext, Collections.unmodifiableList(servletInitializerList));
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
            for (OftenServerStateListener stateListener : stateListenerSet)
            {
                try
                {
                    stateListener.onDestroyed();
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

        Set<OftenServerStateListener> stateListenerSet = new HashSet<>();

        Set<Class<OftenInitializer>> initialClasses = new HashSet<>();
        if (servletInitializerClasses != null)
        {
            for (Class<?> c : servletInitializerClasses)
            {
                if (!Modifier.isAbstract(c.getModifiers()))
                {
                    if (OftenTool.isAssignable(c, OftenInitializer.class))
                    {
                        initialClasses.add((Class<OftenInitializer>) c);
                    } else
                    {
                        Class<OftenServerStateListener> clazz = (Class<OftenServerStateListener>) c;
                        try
                        {
                            stateListenerSet.add(OftenTool.newObject(clazz));
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        servletContext.setAttribute(FROM_INITIALIZER_ATTR, true);
        servletContext.setAttribute(OftenServerStateListener.class.getName(), stateListenerSet);


        for (OftenServerStateListener listener : stateListenerSet)
        {
            listener.beforeInit(servletContext);
        }

        try
        {
            BridgeServlet bridgeServlet = new BridgeServlet(servletContext, stateListenerSet, initialClasses);
            bridgeServlet.doStart(true);//直接调用

            bridgeServlet.registerServlet(BRIDGE_SERVLET_LOAD_VALUE - 1);
        } catch (Throwable e)
        {
            throw new ServletException(e);
        }
        WebSocketHandle.initFilter(servletContext);
//        BridgeFilter.init(servletContext);

        for (OftenServerStateListener listener : stateListenerSet)
        {
            listener.afterInit(servletContext);
        }
        isLoad = true;
        List<CustomerFrameworkServlet> list = customerFrameworkServlets;
        customerFrameworkServlets = new ArrayList<>(0);
        for (CustomerFrameworkServlet customerFrameworkServlet : list)
        {
            try
            {
                customerFrameworkServlet.doStart(true);
            } catch (ServletException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    static void onReady(CustomerFrameworkServlet customerFrameworkServlet)
    {
        if (isLoad)
        {
            try
            {
                customerFrameworkServlet.doStart(true);
            } catch (ServletException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        } else
        {
            customerFrameworkServlets.add(customerFrameworkServlet);
        }
    }

//    public static void bindOftenServlet(String oftenContext, HttpServlet servlet)
//    {
//        PortUtil.checkName(oftenContext);
//        BridgeFilter.bind(oftenContext, servlet);
//    }

}
