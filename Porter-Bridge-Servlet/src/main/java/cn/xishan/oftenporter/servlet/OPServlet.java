package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.PreRequest;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultPorterBridge;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;
import cn.xishan.oftenporter.servlet.websocket.WebSocketHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

@CorsAccess
public abstract class OPServlet extends HttpServlet implements CommonMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OPServlet.class);
    public static final String SERVLET_NAME_NAME = "cn.xishan.oftenporter.servlet.OPServlet.name";

    private static final long serialVersionUID = 1L;
    private PorterMain porterMain;
    protected String pname, urlEncoding;
    protected Boolean responseWhenException;
    protected MultiPartOption multiPartOption = null;
    protected ResponseHandle responseHandle;

    private CorsAccess defaultCorsAccess;

    @Property(value = "op.servlet.cors", defaultVal = "false")
    private Boolean hasCors;
    /**
     * 是否添加put参数处理,见{@linkplain PutParamSourceHandle PutParamSourceHandle}。
     */
    protected boolean addPutDealt = true;

    public OPServlet()
    {

    }

    /**
     * 会添加POST与PUT的{@linkplain ParamSourceHandle},用于处理数据。
     *
     * @param multiPartOption
     */
    public OPServlet(MultiPartOption multiPartOption)
    {
        this.multiPartOption = multiPartOption;
    }

    public OPServlet(String pname, boolean responseWhenException)
    {
        this.pname = pname;
        this.urlEncoding = "utf-8";
        this.responseWhenException = responseWhenException;
    }

    @Override
    protected void service(HttpServletRequest request,
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

    /**
     * 得到路径：
     * 1.当urlPattern="/Test/*",uri="/ServletContext/Test/porter/User/"时，返回/porter/User/
     * 2.当urlPattern="*.html",uri="/ServletContext/index.html"(或"/ServletContext/")时，返回"/index.html"
     *
     * @param request
     * @return
     */
    public static String getPath(HttpServletRequest request)
    {
        String uri = request.getRequestURI();
        int length = request.getContextPath().length() + request.getServletPath().length();
        if (length > uri.length())
        {
            return request.getServletPath();
        } else
        {
            return uri.substring(length);
        }
    }

    static String getUriPrefix(HttpServletRequest request)
    {
        ServletContext servletContext = request.getServletContext();
        String prefix;
        if (servletContext.getAttribute(PBSServletContainerInitializer.FROM_INITIALIZER_ATTR) != null)
        {
            prefix = request.getRequestURI().substring(0, request.getContextPath().length());
        } else
        {
            prefix = request.getRequestURI()
                    .substring(0, request.getContextPath().length() + request.getServletPath().length());
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
        WServletResponse wresp = new WServletResponse(response);
        doRequest(request, path, response, wresp, method);
    }

    public void doRequest(HttpServletRequest request, @MayNull String path, HttpServletResponse response,
            WResponse wResponse, PortMethod method) throws IOException
    {
        WServletRequest wreq = new WServletRequest(request, path, response, method);
        if (wreq.getPath().startsWith("/="))
        {
            wreq.setRequestPath(":" + wreq.getPath().substring(2));
            getPLinker().toAllBridge().request(wreq, lResponse ->
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
                    WPTool.close(wResponse);
                }
            });
        } else
        {
            PreRequest req = porterMain.forRequest(wreq, wResponse);
            if (req != null)
            {
                request.setCharacterEncoding(req.context.getContentEncoding());
                response.setCharacterEncoding(req.context.getContentEncoding());
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
        try
        {
            //检查mapping是否正确
            for (String mapping : config.getServletContext().getServletRegistration(config.getServletName())
                    .getMappings())
            {
                LOGGER.debug("mapping:{}", mapping);
                if (!MAPPING_PATTERN.matcher(mapping).find())
                {
                    throw new ServletException("illegal mapping:" + mapping);
                }
            }
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        WebSocketHandle.handleWS(config.getServletContext());
        super.init(config);
    }

    @Override
    public void init() throws ServletException
    {
        super.init();
        if (this.pname == null)
        {
            pname = getInitParameter("pname");
            if (WPTool.isEmpty(pname))
            {
                pname = PortUtil.getRealClass(this).getSimpleName();
            }
        }

        Logger LOGGER = LoggerFactory.getLogger(OPServlet.class);
        LOGGER.debug("******Porter-Bridge-Servlet init******");

        if (this.urlEncoding == null)
        {
            urlEncoding = getInitParameter("urlEncoding");
            if (urlEncoding == null)
            {
                urlEncoding = "utf-8";
            }

        }
        porterMain = new PorterMain(new PName(pname), this);
        if (responseWhenException == null)
        {
            responseWhenException = !"false".equals(getInitParameter("responseWhenException"));
        }
        porterMain.init(responseHandle, new DefaultUrlDecoder(urlEncoding), responseWhenException);
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
        String path = OPServlet.getWebInfDir();
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

    @Override
    public PorterConf newPorterConf()
    {
        if (porterMain == null)
        {
            throw new RuntimeException("Not init!");
        }
        PorterConf porterConf = porterMain.newPorterConf();
        porterConf.setArgumentsFactory(new DefaultServletArgumentsFactory());
        porterConf.addContextAutoSet(WebSocketHandle.WS_SERVER_CONTAINER_NAME,
                getServletContext().getAttribute(WebSocketHandle.WS_SERVER_CONTAINER_NAME));
        porterConf.addContextAutoSet(ServletContext.class, getServletConfig().getServletContext());
        porterConf.addContextAutoSet(SERVLET_NAME_NAME, getServletConfig().getServletName());
        porterConf.addContextAutoSet(OPServlet.class, this);

        return porterConf;
    }

    private PorterMain.ForRequestListener forRequestListener = (req, request, response, isInnerRequest) -> {
        Object originRequest = request.getOriginalRequest();
        if (originRequest instanceof HttpServletRequest)
        {
            HttpServletRequest servletRequest = (HttpServletRequest) originRequest;
            return isCorsForbidden(request.getMethod(), req.classPort.getClass(), req.funPort.getMethod(),
                    servletRequest,
                    request.getOriginalResponse());
        }
        return false;
    };

    public boolean isCorsForbidden(@MayNull PortMethod method, Class porterClass, Method porterMethod,
            HttpServletRequest request,
            HttpServletResponse response)
    {
        if(hasCors){
            return false;
        }
        if (method == null)
        {
            method = PortMethod.valueOf(request.getMethod());
        }
        String origin = request.getHeader("Origin");
        if (WPTool.notNullAndEmpty(origin) && !origin.equals(WServletRequest.getHost(request)))
        {//跨域请求
            CorsAccess corsAccess = AnnoUtil.getAnnotation(porterMethod, CorsAccess.class);
            if (corsAccess == null)
            {
                corsAccess = AnnoUtil.getAnnotation(porterClass, CorsAccess.class);
            }
            if (corsAccess == null)
            {
                corsAccess = defaultCorsAccess;
            }
            for (PortMethod m : corsAccess.forbiddenMethods())
            {
                if (m == method)
                {
                    try
                    {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    } catch (IOException e)
                    {
                        LOGGER.warn(e.getMessage(), e);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void startOne(PorterConf porterConf)
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
        porterMain.startOne(DefaultPorterBridge.defaultBridge(porterConf));
        if (defaultCorsAccess == null)
        {
            defaultCorsAccess = AnnoUtil.getAnnotation(OPServlet.class, CorsAccess.class);
            if (!hasCors)
            {
                porterMain.setForRequestListener(forRequestListener);
            }
        }
    }

    @Override
    public PLinker getPLinker()
    {
        return porterMain.getPLinker();
    }

    @Override
    public void destroyOne(String contextName)
    {
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
        porterMain.destroyAll();
    }

    @Override
    public PorterData getPorterData()
    {
        return porterMain.getPorterData();
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
    }

}
