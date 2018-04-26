package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PBSServletContainerInitializer.class);

    static class OPContextServlet extends HttpServlet
    {
        private StartupServlet startupServlet;

        public OPContextServlet(StartupServlet startupServlet)
        {
            this.startupServlet = startupServlet;
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
            String path = request.getRequestURI().substring(request.getContextPath().length());
            startupServlet.doRequest(request, path, response, method);
        }
    }

    static class StartupServletImpl extends StartupServlet implements OPServletInitializer.Builder,
            OPServletInitializer.BuilderBefore
    {
        private CustomServletPath[] customServletPaths;
        private List<OPServletInitializer> servletInitializerList;

        public StartupServletImpl(ServletContext servletContext, Set<Class<?>> servletInitializerClasses)
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

        @Override
        public void onStart()
        {
            try
            {
                for (OPServletInitializer initializer : servletInitializerList)
                {
                    initializer.onStart(getServletContext(), this);
                }

                if (customServletPaths != null)
                {
                    for (CustomServletPath servletPath : customServletPaths)
                    {
                        servletPath.regServlet(this);
                    }
                }
                servletInitializerList = null;
            } catch (Exception e)
            {
                throw new InitException(e);
            }
        }

        @Override
        public void startOne(PorterConf porterConf)
        {
            super.startOne(porterConf);
            String urlPattern = "/" + porterConf.getContextName() + "/*";
            ServletContext servletContext = getServletContext();
            HttpServlet httpServlet = new OPContextServlet(this);

            ServletRegistration.Dynamic dynamic = servletContext
                    .addServlet(httpServlet.getClass().getName() + "-" + porterConf.getContextName(),
                            httpServlet);
            LOGGER.debug("mapping:{}", urlPattern);
            dynamic.addMapping(urlPattern);
            dynamic.setLoadOnStartup(1);
        }

        @Override
        public void setCustomServletPaths(CustomServletPath... customServletPaths)
        {
            this.customServletPaths = customServletPaths;
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
        for(Class<?> c:servletInitializerClasses){
            if(!Modifier.isAbstract(c.getModifiers())){
                initialClasses.add(c);
            }
        }
        if(initialClasses.size()==0){
            return;
        }
        StartupServletImpl startupServlet = new StartupServletImpl(servletContext, initialClasses);
        ServletRegistration.Dynamic dynamic = servletContext
                .addServlet(startupServlet.toString(), startupServlet);
        dynamic.setAsyncSupported(true);
        dynamic.setLoadOnStartup(1);
    }
}
