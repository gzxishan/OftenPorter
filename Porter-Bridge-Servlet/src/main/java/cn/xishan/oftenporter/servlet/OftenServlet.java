package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.PreRequest;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.sysset.IAutoSetter;
import cn.xishan.oftenporter.porter.core.sysset.IAutoVarGetter;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.simple.DefaultPorterBridge;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;
import cn.xishan.oftenporter.servlet.CorsAccess.CorsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

@CorsAccess
abstract class OftenServlet extends HttpServlet implements CommonMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServlet.class);
    public static final String SERVLET_NAME_NAME = "cn.xishan.oftenporter.servlet.OftenServlet.name";

    private static final long serialVersionUID = 1L;
    protected PorterMain porterMain;
    protected String bridgeName, urlEncoding = "utf-8";
    protected Boolean responseWhenException = true;
    protected MultiPartOption multiPartOption = null;
    //protected ResponseHandle responseHandle;

    private CorsAccess defaultCorsAccess;

    /**
     * 为false，表示一律禁止跨域访问、但可通过注解{@linkplain CorsAccess}进行单独设置；为true时、并不会对跨域进行处理。
     */
    @Property(value = "op.servlet.cors.disable", defaultVal = "false")
    private Boolean ignoreCors;

    @Property(value = "op.servlet.cors.skipRes", defaultVal = "eot,ttf,otf,woff,woff2")
    private String[] skipResources;

    @Property(value = "op.servlet.cors.http2https", defaultVal = "false")
    private Boolean isHttp2Https;
    /**
     * 是否添加put参数处理,见{@linkplain PutParamSourceHandle PutParamSourceHandle}。
     */
    protected boolean addPutDealt = true;

    private WrapperFilterManager wrapperFilterManager = new WrapperFilterManager()
    {
        private List<WrapperFilter> wrapperFilterList = new ArrayList<>();

        @Override
        public void addWrapperFilter(WrapperFilter wrapperFilter)
        {
            wrapperFilterList.add(wrapperFilter);
        }

        @Override
        public void addFirstWrapperFilter(WrapperFilter wrapperFilter)
        {
            wrapperFilterList.add(0, wrapperFilter);
        }

        @Override
        public List<WrapperFilter> wrapperFilters()
        {
            return Collections.unmodifiableList(wrapperFilterList);
        }
    };

    public OftenServlet()
    {
        this.bridgeName = "OftenServlet";
    }

    @AutoSet.SetOk
    public void setOk()
    {
        LOGGER.debug("op.servlet.cors.disable={},op.servlet.cors.http2https={},skipRes={}", ignoreCors, isHttp2Https,
                skipResources);
        Arrays.sort(skipResources);
    }

    /**
     * 会添加POST与PUT的{@linkplain ParamSourceHandle},用于处理数据。
     *
     * @param multiPartOption
     */
    public OftenServlet(MultiPartOption multiPartOption)
    {
        this();
        this.multiPartOption = multiPartOption;
    }


    public OftenServlet(boolean responseWhenException)
    {
        this(null, responseWhenException);
    }

    public OftenServlet(String bridgeName, boolean responseWhenException)
    {
        this();
        if (OftenTool.notEmpty(bridgeName))
        {
            this.bridgeName = bridgeName;
        }
        this.responseWhenException = responseWhenException;
    }


    @Override
    public void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        String method = request.getMethod();
        if (method.equals("GET"))
        {
            doRequest(request, null, response, PortMethod.GET);
        } else if (method.equals("HEAD"))
        {
            doRequest(request, null, response, PortMethod.HEAD);
        } else if (method.equals("POST"))
        {
            doRequest(request, null, response, PortMethod.POST);
        } else if (method.equals("PUT"))
        {
            doRequest(request, null, response, PortMethod.PUT);
        } else if (method.equals("DELETE"))
        {
            doRequest(request, null, response, PortMethod.DELETE);
        } else if (method.equals("OPTIONS"))
        {
            doRequest(request, null, response, PortMethod.OPTIONS);
        } else if (method.equals("TRACE"))
        {
            doRequest(request, null, response, PortMethod.TARCE);
        } else
        {
            super.service(request, response);
        }

    }


    static String getPath(HttpServletRequest request)
    {
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        String servletPath = request.getServletPath();
        if (uri.equals(servletPath) || !uri.startsWith(servletPath))
        {
            return uri;
        } else
        {
            return uri.substring(servletPath.length());
        }
    }

    static String getOftenPath(HttpServletRequest request)
    {
        return request.getRequestURI().substring(getUriPrefix(request).length());
    }

    static String getUriPrefix(HttpServletRequest request)
    {
        ServletContext servletContext = request.getServletContext();
        String prefix;
        if (servletContext.getAttribute(OftenServletContainerInitializer.FROM_INITIALIZER_ATTR) != null)
        {
            prefix = request.getRequestURI().substring(0, request.getContextPath().length());
            LOGGER.debug("prefix:{}[FROM_INITIALIZER_ATTR]", prefix);
        } else
        {
            prefix = request.getRequestURI()
                    .substring(0, request.getContextPath().length() + request.getServletPath().length());
            LOGGER.debug("prefix:{}", prefix);
        }
        return prefix;
    }

    /**
     * 处理请求。
     *
     * @param request
     * @param path    当为null时，使用request.getRequestURI().substring(request.getContextPath().length()+ request
     *                .getServletPath().length())
     * @throws IOException
     */
    public void doRequest(HttpServletRequest request, @MayNull String path, HttpServletResponse response,
            PortMethod method) throws IOException
    {
        OftenServletResponse wresp = new OftenServletResponse(response);
        doRequest(request, path, response, wresp, method);
    }

    public void doRequest(HttpServletRequest request, @MayNull String path, HttpServletResponse response,
            OftenServletResponse wResponse, PortMethod method) throws IOException
    {
//        if (isHttp2Https && request.getScheme().equals("http"))
//        {
//            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request)
//            {
//                @Override
//                public String getScheme()
//                {
//                    return "https";
//                }
//
//                @Override
//                public StringBuffer getRequestURL()
//                {
//                    StringBuffer buffer = new StringBuffer();
//                    String host = OftenServletRequest.getHost(this, true);
//                    buffer.append(host).append(getRequestURI());
//                    return buffer;
//                }
//            };
//            request = requestWrapper;
//        }

        OftenServletRequest wreq = new OftenServletRequest(request, path, response, method);
        if (wreq.getPath().startsWith("/="))
        {
            wreq.setRequestPath(":" + wreq.getPath().substring(2));
            getBridgeLinker().toAllBridge().request(wreq, lResponse ->
            {
                if (lResponse != null)
                {
                    Object obj = lResponse.getResponse();
                    if (obj != null)
                    {
                        try
                        {
                            wResponse.write(obj);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    OftenTool.close(wResponse);
                }
            });
        } else
        {
            PreRequest req = porterMain.forRequest(wreq, wResponse);
            if (req.classPort != null && req.funPort == null && method == PortMethod.OPTIONS)
            {
                try
                {
                    PortMethod tryMethod = PortMethod.valueOf(request.getHeader("Access-Control-Request-Method"));
                    PorterOfFun funPort = req.classPort.getChild(req.result, tryMethod);
                    if (funPort != null)
                    {
                        CorsType corsType = isCorsForbidden(tryMethod, funPort.getFinalPorter().getClazz(),
                                funPort.getMethod(), request, response);
                        if (corsType != CorsType.Customer)
                        {
                            response.setStatus(HttpServletResponse.SC_OK);
                            return;
                        }
                    }

                } catch (Exception e)
                {
                    LOGGER.warn(e.getMessage(), e);
                    try
                    {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException e1)
                    {
                        LOGGER.warn(e1.getMessage(), e1);
                    }
                    return;
                }
            }

            if (req.isOk())
            {
                String encoding = req.context.getContentEncoding();
                request.setCharacterEncoding(encoding);
                response.setCharacterEncoding(encoding);
                porterMain.doRequest(req, wreq, wResponse, false);
            }
        }


    }

    private static final Pattern MAPPING_PATTERN = Pattern.compile("(/[^/]+/\\*)|(/\\*)");

    /**
     * 先于{@linkplain #init()}调用
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        if (isInit())
        {
            return;//已经初始化
        }
//        WsMain.init(config.getServletContext());
        try
        {
            //检查mapping是否正确
            ServletRegistration servletRegistration = config.getServletContext()
                    .getServletRegistration(config.getServletName());
            if (servletRegistration != null)
            {
                for (String mapping : servletRegistration.getMappings())
                {
                    LOGGER.debug("mapping:{}", mapping);
                    if (!MAPPING_PATTERN.matcher(mapping).find())
                    {
                        throw new ServletException("illegal mapping:" + mapping);
                    }
                }
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        //WebSocketHandle.handleWS(config.getServletContext());
        super.init(config);
    }

    @Override
    public void init() throws ServletException
    {
        super.init();
        if (this.bridgeName == null)
        {
            bridgeName = getInitParameter("bridgeName");
            if (OftenTool.isEmpty(bridgeName))
            {
                bridgeName = PortUtil.getRealClass(this).getSimpleName();
            }
        }

        Logger LOGGER = LoggerFactory.getLogger(OftenServlet.class);
        LOGGER.debug("******Porter-Bridge-Servlet init******");

        if (this.urlEncoding == null)
        {
            this.urlEncoding = getInitParameter("urlEncoding");
            if (OftenTool.isEmpty(this.urlEncoding))
            {
                this.urlEncoding = "utf-8";
            }
        }
        porterMain = new PorterMain(new BridgeName(bridgeName), this);
        if (responseWhenException == null)
        {
            responseWhenException = !"false".equals(getInitParameter("responseWhenException"));
        }
        porterMain.init(new DefaultUrlDecoder(urlEncoding), responseWhenException);
    }

    /**
     * 获取WEB-INF目录的路径
     *
     * @return
     */
    public static String getWebInfDir()
    {
        // file:/D:/JavaWeb/.metadata/.me_tcat/webapps/TestBeanUtils/WEB-INF/classes/
        String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();// .toString();
        path = path.replace('/', File.separatorChar);
        path = path.replace("file:", ""); // 去掉file:
        path = path.replace("classes" + File.separator, ""); // 去掉class\
        // if (path.startsWith(File.separator) && path.indexOf(':') != -1)
        // {
        // return path.substring(1);
        // }
        // else
        // {
        // return path;
        // }
        return path;
    }

    /**
     * 获取Context所在的路径,以File.separatorChar结尾
     *
     * @return
     */
    public static String getContextDir()
    {
        // WEB-INF/
        String path = OftenServlet.getWebInfDir();
        path = path.substring(0, path.length() - 8);
        return path;
    }

    @Override
    public void addGlobalAutoSet(String name, Object object)
    {
        porterMain.addGlobalAutoSet(name, object);
    }

    @Override
    public void addGlobalTypeParser(ITypeParser typeParser)
    {
        porterMain.addGlobalTypeParser(typeParser);
    }

    @Override
    public IListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
    {
        return porterMain.getOnPorterAddListenerAdder();
    }

    @Override
    public void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException
    {
        porterMain.addGlobalCheck(checkPassable);
    }

    public boolean isInit()
    {
        return porterMain != null;
    }

    private Set<OftenServerStateListener> getOftenServerStateListener(ServletContext servletContext)
    {
        Set<OftenServerStateListener> serverStateListenerSet = (Set<OftenServerStateListener>) servletContext
                .getAttribute(OftenServerStateListener.class.getName());
        if (serverStateListenerSet != null)
        {
            return serverStateListenerSet;
        } else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public PorterConf newPorterConf(Class... importers)
    {
        if (!isInit())
        {
            throw new RuntimeException("Not init!");
        }
        ServletContext servletContext = getServletConfig().getServletContext();
        PorterConf porterConf = porterMain.newPorterConf();
        for (OftenServerStateListener serverStateListener : getOftenServerStateListener(servletContext))
        {
            serverStateListener.onNewOne(porterConf);
        }

        porterConf.setArgumentsFactory(new DefaultServletArgumentsFactory());
        servletContext.setAttribute(StartupServlet.class.getName(), this);
        servletContext.setAttribute(WrapperFilterManager.class.getName(), wrapperFilterManager);

        porterConf.addContextAutoSet(ServletContext.class, servletContext);
        porterConf.addContextAutoSet(SERVLET_NAME_NAME, getServletConfig().getServletName());
        porterConf.addContextAutoSet(StartupServlet.class, this);

        porterConf.addAutoSetObjectsForSetter(this);

        try
        {
            porterConf.seekImporter(importers);
        } catch (Throwable e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return porterConf;
    }

    private PorterMain.ForRequestListener forRequestListener = (req, request, response, isInnerRequest) -> {
        Object originRequest = request.getOriginalRequest();
        if (originRequest instanceof HttpServletRequest)
        {
            HttpServletRequest servletRequest = (HttpServletRequest) originRequest;
            CorsType corsType = isCorsForbidden(request.getMethod(), req.classPort.getClass(), req.funPort.getMethod(),
                    servletRequest, request.getOriginalResponse());
            return corsType == CorsType.NotPass;
        }
        return false;
    };

    /**
     * @param response
     * @param corsAccess
     * @param optionalMethod      当corsAccess.allowMethods()为空时，可使用该参数
     * @param optionalAllowOrigin
     */
    private void setCors(HttpServletResponse response, CorsAccess corsAccess, PortMethod optionalMethod,
            String optionalAllowOrigin)
    {
        String[] methods = new String[corsAccess.allowMethods().length];
        PortMethod[] portMethods = corsAccess.allowMethods();

        if (portMethods.length == 0 && optionalMethod != null)
        {
            portMethods = new PortMethod[]{
                    optionalMethod
            };
        }

        for (int i = 0; i < methods.length; i++)
        {
            methods[i] = portMethods[i].name();
        }

        response.setHeader("Access-Control-Allow-Methods", OftenTool.join(",", methods));
        response.setHeader("Access-Control-Allow-Credentials", String.valueOf(corsAccess.allowCredentials()));
        response.setHeader("Access-Control-Allow-Origin",
                optionalAllowOrigin != null ? optionalAllowOrigin : corsAccess.allowOrigin());

        if (OftenTool.notEmpty(corsAccess.exposeHeaders()))
        {
            response.setHeader("Access-Control-Expose-Headers", corsAccess.exposeHeaders());
        }
        if (OftenTool.notEmpty(corsAccess.allowHeaders()))
        {
            response.setHeader("Access-Control-Allow-Headers", corsAccess.allowHeaders());
        }
        if (OftenTool.notEmpty(corsAccess.maxAge()))
        {
            response.setHeader("Access-Control-Max-Age", corsAccess.maxAge());
        }
    }


    public CorsType isCorsForbidden(@MayNull PortMethod method, Class porterClass, Method porterMethod,
            HttpServletRequest request, HttpServletResponse response)
    {
        return isCorsForbidden(null, method, porterClass, porterMethod, request, response);
    }

    public CorsType isCorsForbidden(@MayNull CorsAccess customerCorsAccess, @MayNull PortMethod method,
            Class porterClass, Method porterMethod,
            HttpServletRequest request, HttpServletResponse response)
    {
        if (ignoreCors)
        {
            return CorsType.Ignore;
        }

        if (method == null)
        {
            method = PortMethod.valueOf(request.getMethod());
        }
        if (method == PortMethod.OPTIONS)
        {
            try
            {
                method = PortMethod.valueOf(request.getHeader("Access-Control-Request-Method"));
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
                try
                {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (IOException e1)
                {
                    LOGGER.warn(e1.getMessage(), e1);
                }
                return CorsType.NotPass;
            }
        }
        String origin = request.getHeader("Origin");
        String host;
        if (OftenTool.notEmpty(origin) && !origin.equals((host = OftenServletRequest
                .getHost(request, isHttp2Https))))
        {//跨域请求
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("method={},origin={},host={},uri={}", method, origin, host, request.getRequestURI());
            }

            CorsAccess corsAccess = customerCorsAccess != null ? customerCorsAccess : AnnoUtil
                    .getAnnotation(porterMethod, CorsAccess.class);
            if (corsAccess == null)
            {
                corsAccess = AnnoUtil.getAnnotation(porterClass, CorsAccess.class);
            }
            if (corsAccess == null)
            {
                corsAccess = defaultCorsAccess;
            }

            if (method == PortMethod.GET && Arrays
                    .binarySearch(skipResources, OftenStrUtil.getSuffix(request.getRequestURI())) > 0)
            {
                setCors(response, corsAccess, method, "*");
                return CorsType.Pass;
            }

            if (!corsAccess.enabled())
            {
                try
                {
                    LOGGER.info("send code {} for cors not enable", HttpServletResponse.SC_FORBIDDEN);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                } catch (IOException e)
                {
                    LOGGER.warn(e.getMessage(), e);
                } finally
                {
                    return CorsType.NotPass;//禁止跨域
                }
            } else if (corsAccess.isCustomer())
            {
                return CorsType.Customer;//自定义跨域设置。
            }
            for (PortMethod m : corsAccess.allowMethods())
            {
                if (m == method)
                {//允许跨域
                    setCors(response, corsAccess, null, null);
                    return CorsType.Pass;
                }
            }
            try
            {
                LOGGER.info("send code {} for cors", HttpServletResponse.SC_FORBIDDEN);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e)
            {
                LOGGER.warn(e.getMessage(), e);
            } finally
            {
                return CorsType.NotPass;//禁止跨域
            }
        }
        return CorsType.Pass;
    }

    @Override
    public IAutoSetter startOne(PorterConf porterConf)
    {
        if (multiPartOption != null)
        {
            porterConf.getParamSourceHandleManager()
                    .addByMethod(new MultiPartParamSourceHandle(multiPartOption, addPutDealt), PortMethod.POST,
                            PortMethod.PUT);
        } else if (addPutDealt)
        {
            PutParamSourceHandle.addPutDealt(porterConf);
        }
        for (OftenServerStateListener serverStateListener : getOftenServerStateListener(
                getServletConfig().getServletContext()))
        {
            serverStateListener.onNewOne(porterConf);
        }
        IAutoSetter autoSetter = porterMain.startOne(DefaultPorterBridge.defaultBridge(porterConf));
        if (defaultCorsAccess == null)
        {
            defaultCorsAccess = AnnoUtil.getAnnotation(OftenServlet.class, CorsAccess.class);
            if (!ignoreCors)
            {
                porterMain.setForRequestListener(forRequestListener);
            }
        }
        return autoSetter;
    }

    @Override
    public IAutoSetter getAutoSetter(String context)
    {
        return porterMain.getAutoSetter(context);
    }

    @Override
    public BridgeLinker getBridgeLinker()
    {
        return porterMain.getBridgeLinker();
    }

    @Override
    public void destroyOne(String contextName)
    {
        for (OftenServerStateListener serverStateListener : getOftenServerStateListener(
                getServletConfig().getServletContext()))
        {
            serverStateListener.onDestroyOne(contextName);
        }
        porterMain.destroyOne(contextName);
    }

    @Override
    public void enableOne(String contextName, boolean enable)
    {
        porterMain.enableContext(contextName, enable);
    }

    @Override
    public void destroyAll()
    {
        for (OftenServerStateListener serverStateListener : getOftenServerStateListener(
                getServletConfig().getServletContext()))
        {
            serverStateListener.onDestroyAll();
        }
        porterMain.destroyAll();
    }

    @Override
    public PorterData getPorterData()
    {
        return porterMain.getPorterData();
    }

    @Override
    public IAutoVarGetter getAutoVarGetter(String context)
    {
        return porterMain.getAutoVarGetter(context);
    }

    @Override
    public String getDefaultTypeParserId()
    {
        return null;
    }


    @Override
    public void destroy()
    {
        destroyAll();
        super.destroy();
//        WsMain.destroy();
    }

}
