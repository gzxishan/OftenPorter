package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class WServletRequest extends PRequest// implements IAttributeFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WServletRequest.class);

    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length() + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     */
    WServletRequest(HttpServletRequest request, String path,
            HttpServletResponse response, PortMethod method)
    {
        super(null, method, WPTool.notNullAndEmpty(path) ? path : OPServlet.getPath(request),
                false);
        this.request = request;
        this.response = response;
    }

    @Override
    public HttpServletRequest getOriginalRequest()
    {
        return request;
    }

    @Override
    public HttpServletResponse getOriginalResponse()
    {
        return response;
    }


    public AsyncContext startAsync()
    {
        return request.startAsync(request, response);
    }

    @Override
    public Map<String, Object> getParameterMap()
    {
        if (super.params == null)
        {
            params = new HashMap<>();
            Enumeration<String> e = request.getParameterNames();

            while (e.hasMoreElements())
            {
                String name = e.nextElement();
                Object value = getParameter(name);
                if (WPTool.notNullAndEmpty(value))
                {
                    params.put(name, value);
                }
            }
        }
        return super.getParameterMap();
    }

    @Override
    public Object getParameter(String name)
    {
        String[] values = request.getParameterValues(name);
        Object v = values == null || values.length == 0 ? null : values[0];
        return v;
    }


    @Override
    public PortMethod getMethod()
    {
        return method;
    }


    /**
     * 获得接口地址。见{@linkplain #getPortUrl(WObject, String, String, String, String, String, boolean)}。
     *
     * @param wObject
     * @param funTied 若为null，则使用当前的。
     * @return
     */
    public static String getPortUrl(WObject wObject, String funTied, boolean http2Https)
    {
        return getPortUrl(wObject, null, null, null, null, funTied, http2Https);
    }

    /**
     * 获得接口地址。见{@linkplain #getPortUrl(WObject, String, String, String, String, String, boolean)}。
     *
     * @param wObject
     * @param funTied 若为null，则使用当前的。
     * @return
     */
    public static String getPortUrl(WObject wObject, String classTied, String funTied, boolean http2Https)
    {
        return getPortUrl(wObject, null, null, null, classTied, funTied, http2Https);
    }


    /**
     * 获得接口地址。<strong>注意：</strong>Servlet转接会出错。
     *
     * @param wObject
     * @param pname       若为null，则不含pname部分。
     * @param contextName 若为null，则使用当前的。
     * @param classTied   若为null，则使用当前的。
     * @param funTied     若为null，则使用当前的。
     * @param http2Https  是否http变成成https
     * @return
     */
    public static String getPortUrl(WObject wObject, String urlPrefix, String pname, String contextName,
            String classTied, String funTied,
            boolean http2Https)
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        StringBuilder stringBuilder = new StringBuilder();
        String host;
        if (http2Https && request.getScheme().equals("http"))
        {
            host = "https://" + request.getServerName() + getPort(request);
        } else
        {
            host = request.getScheme() + "://" + request.getServerName() + getPort(request);
        }
        stringBuilder.append(host);
        stringBuilder.append(urlPrefix != null ? urlPrefix : OPServlet.getUriPrefix(request));
        if (pname != null)
        {
            stringBuilder.append("/=").append(pname);
        }
        UrlDecoder.Result result = wObject.url();
        stringBuilder.append('/').append(contextName == null ? result.contextName() : contextName);
        stringBuilder.append('/').append(classTied == null ? result.classTied() : classTied);
        stringBuilder.append('/').append(funTied == null ? result.funTied() : funTied);
        return stringBuilder.toString();
    }

    private static final Pattern PATTERN_HOST_PORT = Pattern.compile("^(http|https)://([^/]+)");

    /**
     * 得到host，包含协议。如http://localhost:8080/hello得到的是http://localhost:8080
     *
     * @param url
     * @return
     */
    public static String getHostFromURL(CharSequence url)
    {
        return getHostFromURL(url, false);
    }

    public static String getPathFromURL(CharSequence url)
    {
        if (url == null)
        {
            return null;
        }
        String host = getHostFromURL(url);
        String path = url.subSequence(host.length(), url.length()).toString();
        int index = path.lastIndexOf("?");
        if (index >= 0)
        {
            path = path.substring(0, index);
        }
        return path;
    }

    /**
     * 得到host，包含协议。如http://localhost:8080/hello得到的是http://localhost:8080
     *
     * @param url
     * @return
     */
    public static String getHostFromURL(CharSequence url, boolean http2Https)
    {
        Matcher matcher = PATTERN_HOST_PORT.matcher(url);
        if (matcher.find())
        {
            String host = matcher.group();
            if (http2Https && host.startsWith("http:"))
            {
                host = "https:" + host.substring(5);
            }
            return host;
        } else
        {
            return "";
        }
    }


    private static String getPort(HttpServletRequest request)
    {
        if (request.getServerPort() == 80 || request.getServerPort() == 443)
        {
            return "";
        } else
        {
            return ":" + request.getServerPort();
        }
    }

    /**
     * @return
     */
    public static String getHost(HttpServletRequest request, boolean isHttp2Https)
    {
        String host = (isHttp2Https ? "https" : request.getScheme()) + "://" + request.getServerName() + getPort(request);
        return host;
    }

    /**
     * @return
     */
    public String getHost()
    {
        return getHost(false);
    }

    /**
     * @return
     */
    public String getHost(boolean http2Https)
    {
        String host;
        if (http2Https && request.getScheme().equals("http"))
        {
            host = "https://" + request.getServerName() + getPort(request);
        } else
        {
            host = getHost(request, false);
        }
        return host;
    }

    /**
     * 获得请求的路径，不包括/ContextPath
     */
    public static String getPath(HttpServletRequest request)
    {
        return OPServlet.getPath(request);
    }

}
