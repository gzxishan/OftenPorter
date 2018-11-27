package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.websocket.WebSocketHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2018/4/18.
 */
@HandlesTypes(OftenInitializer.class)
public final class OftenServletContainerInitializer implements ServletContainerInitializer
{
    public static final String FROM_INITIALIZER_ATTR = "__FROM_INITIALIZER_ATTR__";

    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServletContainerInitializer.class);

    static class OPContextServlet extends HttpServlet
    {
        private StartupServlet startupServlet;
        private OftenInitializer oftenInitializer;

        public OPContextServlet(StartupServlet startupServlet, OftenInitializer oftenInitializer)
        {
            this.startupServlet = startupServlet;
            this.oftenInitializer = oftenInitializer;
        }

        @Override
        protected void service(HttpServletRequest request,
                HttpServletResponse response) throws ServletException, IOException
        {
            String method = request.getMethod();
            if (method.equals("GET"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.GET);
            } else if (method.equals("HEAD"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.HEAD);
            } else if (method.equals("POST"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.POST);
            } else if (method.equals("PUT"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.PUT);
            } else if (method.equals("DELETE"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.DELETE);
            } else if (method.equals("OPTIONS"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.OPTIONS);
            } else if (method.equals("TRACE"))
            {
                oftenInitializer.onDoRequest(startupServlet, request, response, PortMethod.TARCE);
            } else
            {
                super.service(request, response);
            }

        }
    }

    static class StartupServletImpl extends StartupServlet implements OftenInitializer.BuilderBefore
    {
        private List<OftenInitializer> servletInitializerList;

        public StartupServletImpl(ServletContext servletContext,
                Set<Class<?>> servletInitializerClasses) throws Exception
        {
            super("", true);
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
                return StartupServletImpl.this.newPorterConf(importers);
            }

            @Override
            public void startOne(PorterConf porterConf)
            {
                StartupServletImpl.this.startOne(porterConf);
                String urlPattern = "/" + porterConf.getContextName() + "/*";
                ServletContext servletContext = getServletContext();
                HttpServlet httpServlet = new OPContextServlet(StartupServletImpl.this, initializer);
                ServletRegistration.Dynamic dynamic = servletContext
                        .addServlet(httpServlet.getClass().getName() + "-" + porterConf.getContextName(),
                                httpServlet);
                LOGGER.debug("mapping:{}", urlPattern);
                dynamic.addMapping(urlPattern);
                dynamic.setAsyncSupported(true);
                dynamic.setLoadOnStartup(1);
                if (customServletPaths != null)
                {
                    for (CustomServletPath servletPath : customServletPaths)
                    {
                        servletPath.regServlet(StartupServletImpl.this);
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
                return StartupServletImpl.this.getBridgeLinker();
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
        }

        @Override
        public void startOne(PorterConf porterConf)
        {
            super.startOne(porterConf);

        }

        @Override
        public void setMultiPartOption(MultiPartOption multiPartOption)
        {
            super.multiPartOption = multiPartOption;
        }

        @Override
        public void setPName(String pName)
        {
            if (isStarted)
            {
                return;
            }
            super.pname = pName;
        }

        @Override
        public String getPName()
        {
            return pname;
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
        if (servletInitializerClasses != null && servletInitializerClasses.size() > 0)
        {
            Set<Class<?>> initialClasses = new HashSet<>();
            for (Class<?> c : servletInitializerClasses)
            {
                if (!Modifier.isAbstract(c.getModifiers()))
                {
                    initialClasses.add(c);
                }
            }
            if (initialClasses.size() > 0)
            {
                servletContext.setAttribute(FROM_INITIALIZER_ATTR, true);
                try
                {
                    StartupServletImpl startupServlet = new StartupServletImpl(servletContext, initialClasses);
                    ServletRegistration.Dynamic dynamic = servletContext
                            .addServlet(startupServlet.toString(), startupServlet);
                    dynamic.setAsyncSupported(true);
                    dynamic.setLoadOnStartup(1);
                } catch (Throwable e)
                {
                    throw new ServletException(e);
                }
            }
        }
        WebSocketHandle.initFilter(servletContext);
    }
}
