package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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
@HandlesTypes(OPServletInitializer.class)
public class PBSServletContainerInitializer implements ServletContainerInitializer
{
    public static final String FROM_INITIALIZER_ATTR = "__FROM_INITIALIZER_ATTR__";

    private static final Logger LOGGER = LoggerFactory.getLogger(PBSServletContainerInitializer.class);

    static class OPContextServlet extends HttpServlet
    {
        private StartupServlet startupServlet;
        private OPServletInitializer opServletInitializer;

        public OPContextServlet(StartupServlet startupServlet, OPServletInitializer opServletInitializer)
        {
            this.startupServlet = startupServlet;
            this.opServletInitializer = opServletInitializer;
        }

        @Override
        protected void service(HttpServletRequest request,
                HttpServletResponse response) throws ServletException, IOException
        {
            String method = request.getMethod();
            if (method.equals("GET"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.GET);
            } else if (method.equals("HEAD"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.HEAD);
            } else if (method.equals("POST"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.POST);
            } else if (method.equals("PUT"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.PUT);
            } else if (method.equals("DELETE"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.DELETE);
            } else if (method.equals("OPTIONS"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.OPTIONS);
            } else if (method.equals("TRACE"))
            {
                opServletInitializer.onDoRequest(startupServlet, request, response, PortMethod.TARCE);
            } else
            {
                super.service(request, response);
            }

        }
    }

    static class StartupServletImpl extends StartupServlet implements OPServletInitializer.BuilderBefore
    {
        private List<OPServletInitializer> servletInitializerList;

        public StartupServletImpl(ServletContext servletContext,
                Set<Class<?>> servletInitializerClasses) throws Exception
        {
            super("", true);
            servletInitializerList = new ArrayList<>(servletInitializerClasses.size());
            for (Class<?> clazz : servletInitializerClasses)
            {
                try
                {
                    OPServletInitializer initializer = (OPServletInitializer) WPTool.newObject(clazz);
                    initializer.beforeStart(servletContext, this);
                    servletInitializerList.add(initializer);
                } catch (Exception e)
                {
                    throw new InitException(e);
                }
            }
        }

        class BuilderImpl implements OPServletInitializer.Builder
        {

            private OPServletInitializer initializer;
            private CustomServletPath[] customServletPaths;

            public BuilderImpl(OPServletInitializer initializer)
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
            public PLinker getPLinker()
            {
                return StartupServletImpl.this.getPLinker();
            }
        }

        @Override
        public void onStart()
        {
            try
            {
                for (OPServletInitializer initializer : servletInitializerList)
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
            for (OPServletInitializer initializer : servletInitializerList)
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
        if (servletInitializerClasses == null || servletInitializerClasses.size() == 0)
        {
            return;
        }
        Set<Class<?>> initialClasses = new HashSet<>();
        for (Class<?> c : servletInitializerClasses)
        {
            if (!Modifier.isAbstract(c.getModifiers()))
            {
                initialClasses.add(c);
            }
        }
        if (initialClasses.size() == 0)
        {
            return;
        }
        servletContext.setAttribute(FROM_INITIALIZER_ATTR, true);
        try
        {
            StartupServletImpl startupServlet = new StartupServletImpl(servletContext, initialClasses);
            ServletRegistration.Dynamic dynamic = servletContext
                    .addServlet(startupServlet.toString(), startupServlet);
            dynamic.setAsyncSupported(true);
            dynamic.setLoadOnStartup(1);
            WebSocketHandle.initFilter(servletContext);
        } catch (Throwable e)
        {
            throw new ServletException(e);
        }
    }
}
