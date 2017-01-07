package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.PreRequest;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import cn.xishan.oftenporter.porter.simple.DefaultPorterBridge;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 用于servlet,请求地址格式为:http://host[:port]/ServletContextPath[/=pname]/contextName/ClassTied/[funTied|restValue][?name1
 * =value1
 * &name2=value2...]
 * <pre>
 *     初始参数有：
 *     urlPatternPrefix:必须值。
 *     pname:框架实例名称，默认为"WMainServlet".
 *     urlEncoding:地址参数的字符编码,默认为utf-8
 *     responseWhenException:默认为true。
 * </pre>
 */
public class WMainServlet extends HttpServlet implements CommonMain {
    private static final long serialVersionUID = 1L;
    private PorterMain porterMain;
    private String pname, urlEncoding;
    private Boolean responseWhenException;
    private String urlPatternPrefix;
    private static final Logger LOGGER = LoggerFactory.getLogger(WMainServlet.class);

    public WMainServlet() {

    }

    public WMainServlet(String urlPatternPrefix, String pname, String urlEncoding, boolean responseWhenException) {
        this.urlPatternPrefix = urlPatternPrefix;
        this.pname = pname;
        this.urlEncoding = urlEncoding;
        this.responseWhenException = responseWhenException;
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest(req, resp, PortMethod.TARCE);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest(req, resp, PortMethod.PUT);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest(req, resp, PortMethod.HEAD);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doRequest(req, resp, PortMethod.DELETE);
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response, PortMethod.OPTIONS);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response, PortMethod.POST);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response, PortMethod.GET);
    }

    /**
     * 处理请求
     *
     * @param request
     * @param response
     * @param method
     * @throws IOException
     */
    private void doRequest(HttpServletRequest request, HttpServletResponse response,
                           PortMethod method) throws IOException {

        WServletRequest wreq = new WServletRequest(request, response, urlPatternPrefix, method);
        final WServletResponse wresp = new WServletResponse(response);

        if (wreq.getPath().startsWith("/=")) {
            wreq.setRequestPath(":" + wreq.getPath().substring(2));
            getPLinker().toAllBridge().request(wreq, lResponse -> {
                if (lResponse != null) {
                    Object obj = lResponse.getResponse();
                    if (obj != null) {
                        try {
                            wresp.write(obj);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    WPTool.close(wresp);
                }
            });
        } else {
            PreRequest req = porterMain.forRequest(wreq, wresp);
            if (req != null) {
                request.setCharacterEncoding(req.context.getContentEncoding());
                response.setCharacterEncoding(req.context.getContentEncoding());
                porterMain.doRequest(req, wreq, wresp);
            }
        }


    }


    @Override
    public void init() throws ServletException {
        LOGGER.debug("******Porter-Bridge-Servlet init******");
        super.init();

        if (this.pname == null) {
            pname = getInitParameter("pname");
            if (WPTool.isEmpty(pname)) {
                pname = WMainServlet.class.getSimpleName();
            }
        }

        if (this.urlEncoding == null) {
            urlEncoding = getInitParameter("urlEncoding");
            if (urlEncoding == null) {
                urlEncoding = "utf-8";
            }

        }

        if (urlPatternPrefix == null) {
            urlPatternPrefix = getInitParameter("urlPatternPrefix");
        }

        PBridge bridge = (request, callback) -> {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = porterMain.forRequest(request, resp);
            if (req != null) {
                porterMain.doRequest(req, request, resp);
            }
        };
        porterMain = new PorterMain(new PName(pname), bridge);
        if (responseWhenException == null) {
            responseWhenException = !"false".equals(getInitParameter("responseWhenException"));
        }
        porterMain.init(new DefaultUrlDecoder(urlEncoding), responseWhenException);

    }

    /**
     * 获取WEB-INF目录的路径
     *
     * @return
     */
    public static String getWebInfDir() {
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
    public static String getContextDir() {
        // WEB-INF/
        String path = WMainServlet.getWebInfDir();
        path = path.substring(0, path.length() - 8);
        return path;
    }

    @Override
    public void addGlobalAutoSet(String name, Object object) {
        porterMain.addGlobalAutoSet(name, object);
    }

    @Override
    public void addGlobalTypeParser(ITypeParser typeParser) {
        porterMain.addGlobalTypeParser(typeParser);
    }

    @Override
    public void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException {
        porterMain.addGlobalCheck(checkPassable);
    }

    @Override
    public PorterConf newPorterConf() {
        return porterMain.newPorterConf();
    }

    @Override
    public void startOne(PorterConf porterConf) {
        porterMain.startOne(DefaultPorterBridge.defaultBridge(porterConf));
    }

    @Override
    public PLinker getPLinker() {
        return porterMain.getPLinker();
    }

    @Override
    public void destroyOne(String contextName) {
        porterMain.destroyOne(contextName);
    }

    @Override
    public void enableOne(String contextName, boolean enable) {
        porterMain.enableContext(contextName, enable);
    }

    @Override
    public void destroyAll() {
        porterMain.destroyAll();
    }


    @Override
    public void destroy() {
        destroyAll();
        super.destroy();
    }

}
