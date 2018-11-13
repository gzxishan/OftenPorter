package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class OftenServletRequest extends BridgeRequest// implements IAttributeFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenServletRequest.class);

    private WeakReference<HttpServletRequest> request;
    private WeakReference<HttpServletResponse> response;

    /**
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length() + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     */
    OftenServletRequest(HttpServletRequest request, String path,
            HttpServletResponse response, PortMethod method)
    {
        super(null, method, OftenTool.notNullAndEmpty(path) ? path : OftenServlet.getOftenPath(request),
                false);
        this.request = new WeakReference<>(request);
        this.response = new WeakReference<>(response);
    }

    @Override
    public HttpServletRequest getOriginalRequest()
    {
        return request.get();
    }

    @Override
    public HttpServletResponse getOriginalResponse()
    {
        return response.get();
    }


    public AsyncContext startAsync()
    {
        return request.get().startAsync(request.get(), response.get());
    }

    @Override
    public Map<String, Object> getParameterMap()
    {
        if (super.params == null)
        {
            params = new HashMap<>();
            Enumeration<String> e = request.get().getParameterNames();

            while (e.hasMoreElements())
            {
                String name = e.nextElement();
                Object value = getParameter(name);
                if (OftenTool.notNullAndEmpty(value))
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
        String[] values = request.get().getParameterValues(name);
        Object v = values == null || values.length == 0 ? null : values[0];
        return v;
    }


    @Override
    public PortMethod getMethod()
    {
        return method;
    }


    /**
     * 获得接口地址。见{@linkplain #getPortUrl(OftenObject, String, String, String, String, String, boolean)}。
     *
     * @param oftenObject
     * @param funTied 若为null，则使用当前的。
     * @return
     */
    public static String getPortUrl(OftenObject oftenObject, String funTied, boolean http2Https)
    {
        return getPortUrl(oftenObject, null, null, null, null, funTied, http2Https);
    }

    /**
     * 获得接口地址。见{@linkplain #getPortUrl(OftenObject, String, String, String, String, String, boolean)}。
     *
     * @param oftenObject
     * @param funTied 若为null，则使用当前的。
     * @return
     */
    public static String getPortUrl(OftenObject oftenObject, String classTied, String funTied, boolean http2Https)
    {
        return getPortUrl(oftenObject, null, null, null, classTied, funTied, http2Https);
    }


    /**
     * 获得接口地址。<strong>注意：</strong>Servlet转接会出错。
     *
     * @param oftenObject
     * @param pname       若为null，则不含pname部分。
     * @param contextName 若为null，则使用当前的。
     * @param classTied   若为null，则使用当前的。
     * @param funTied     若为null，则使用当前的。
     * @param http2Https  是否http变成成https
     * @return
     */
    public static String getPortUrl(OftenObject oftenObject, String urlPrefix, String pname, String contextName,
            String classTied, String funTied,
            boolean http2Https)
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
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
        stringBuilder.append(urlPrefix != null ? urlPrefix : OftenServlet.getUriPrefix(request));
        if (pname != null)
        {
            stringBuilder.append("/=").append(pname);
        }
        UrlDecoder.Result result = oftenObject.url();
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
        String host = (isHttp2Https ? "https" : request.getScheme()) + "://" + request.getServerName() + getPort(
                request);
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
        HttpServletRequest request = this.request.get();
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
     * 得到路径：
     * 1.当urlPattern="/Test/*",uri="/ServletContext/Test/porter/User/"时，返回/porter/User/
     * 2.当urlPattern="*.html",uri="/ServletContext/index.html"(或"/ServletContext/")时，返回"/index.html"
     *
     * @param request
     * @return
     */
    public static String getPath(HttpServletRequest request)
    {
        return OftenServlet.getPath(request);
    }

    public static String getOftenPath(HttpServletRequest request)
    {
        return OftenServlet.getOftenPath(request);
    }

}
