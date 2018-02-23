package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.PreRequest;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
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

/**
 * 请使用{@linkplain StartupServlet}
 */
@Deprecated
public class WMainServlet extends HttpServlet implements CommonMain
{
    private static final long serialVersionUID = 1L;
    private PorterMain porterMain;
    private String pname, urlEncoding;
    private Boolean responseWhenException;
    protected MultiPartOption multiPartOption = null;
    protected ResponseHandle responseHandle;

    /**
     * 是否添加put参数处理,见{@linkplain PutParamSourceHandle PutParamSourceHandle}。
     */
    protected boolean addPutDealt = true;

    private IAttributeFactory attributeFactory = new IAttributeFactory()
    {
        @Override
        public IAttribute getIAttribute(WObject wObject)
        {
            Object req = wObject.getRequest().getOriginalRequest();
            if(req==null||!(req instanceof HttpServletRequest)){
                return null;
            }
            HttpServletRequest request = (HttpServletRequest) req;
            return new IAttribute()
            {
                @Override
                public IAttribute setAttribute(String key, Object value)
                {
                    request.setAttribute(key, value);
                    return this;
                }

                @Override
                public <T> T getAttribute(String key)
                {
                    Object obj = request.getAttribute(key);
                    return (T) obj;
                }

                @Override
                public <T> T removeAttribute(String key)
                {
                    Object obj = request.getAttribute(key);
                    request.removeAttribute(key);
                    return (T) obj;
                }
            };
        }
    };

    public WMainServlet()
    {

    }

    /**
     * 会添加POST与PUT的{@linkplain ParamSourceHandle},用于处理数据。
     *
     * @param multiPartOption
     */
    public WMainServlet(MultiPartOption multiPartOption)
    {
        this.multiPartOption = multiPartOption;
    }

    public WMainServlet(String pname, boolean responseWhenException)
    {
        //this.urlPatternPrefix = urlPatternPrefix;
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
        String prefix = request.getRequestURI()
                .substring(0, request.getContextPath().length() + request.getServletPath().length());
        return prefix;
    }

    /**
     * 处理请求
     *
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length()
     *                 + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     * @throws IOException
     */
    public void doRequest(HttpServletRequest request, @MayNull String path, HttpServletResponse response,
            PortMethod method) throws IOException
    {

        WServletRequest wreq = new WServletRequest(attributeFactory, request, path, response, method);
        final WServletResponse wresp = new WServletResponse(response);

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
                            wresp.write(obj);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    WPTool.close(wresp);
                }
            });
        } else
        {
            PreRequest req = porterMain.forRequest(wreq, wresp);
            if (req != null)
            {
                request.setCharacterEncoding(req.context.getContentEncoding());
                response.setCharacterEncoding(req.context.getContentEncoding());
                porterMain.doRequest(req, wreq, wresp,false);
            }
        }


    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        WebSocketHandle.handleWS(config.getServletContext());
        super.init(config);
    }

    /**
     * 先调用。
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException
    {
        super.init();
        if (this.pname == null)
        {
            pname = getInitParameter("pname");
            if (WPTool.isEmpty(pname))
            {
                pname = getClass().getSimpleName();
            }
        }

        Logger LOGGER = LoggerFactory.getLogger(WMainServlet.class);
        LOGGER.debug("******Porter-Bridge-Servlet init******");

        if (this.urlEncoding == null)
        {
            urlEncoding = getInitParameter("urlEncoding");
            if (urlEncoding == null)
            {
                urlEncoding = "utf-8";
            }

        }

//
//        PBridge inner = (request, callback) ->
//        {
//            LocalResponse resp = new LocalResponse(callback);
//            ABOption abOption = request._getABOption_();
//            if (abOption == null)
//            {
//                abOption = new ABOption(null, PortFunType.INNER, ABInvokeOrder.OTHER);
//                request._setABOption_(abOption);
//            }
//            PreRequest req = porterMain.forRequest(request, resp);
//            if (req != null)
//            {
//                porterMain.doRequest(req, request, resp);
//            }
//        };
//
//        PBridge current = (request, callback) ->
//        {
//            LocalResponse resp = new LocalResponse(callback);
//            PreRequest req = porterMain.forRequest(request, resp);
//            if (req != null)
//            {
//                porterMain.doRequest(req, request, resp);
//            }
//        };
        porterMain = new PorterMain(new PName(pname), this);
        if (responseWhenException == null)
        {
            responseWhenException = !"false".equals(getInitParameter("responseWhenException"));
        }
        porterMain.init(responseHandle, new DefaultUrlDecoder(urlEncoding), responseWhenException);
        porterMain.setIAttributeFactory(attributeFactory);
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
        String path = WMainServlet.getWebInfDir();
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
    public ListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
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

        porterConf.addContextAutoSet(ServletContext.class, getServletContext());

        return porterConf;
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
