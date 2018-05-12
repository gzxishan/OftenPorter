package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.init.IAttribute;
import cn.xishan.oftenporter.porter.core.init.IAttributeFactory;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数支持:{@linkplain #NAME_REQUEST},{@linkplain #NAME_RESPONSE},{@linkplain #NAME_SERVLET_CONTEXT},
 * {@linkplain #NAME_SESSION}
 */
public final class WServletRequest extends PRequest implements IAttributeFactory
{
    private HttpServletRequest request;
    private HttpServletResponse response;
    private IAttributeFactory iAttributeFactory;

    public static final String NAME_SERVLET_CONTEXT = ServletContext.class.getName();
    public static final String NAME_SESSION = HttpSession.class.getName();
    public static final String NAME_REQUEST = HttpServletRequest.class.getName();
    public static final String NAME_RESPONSE = HttpServletResponse.class.getName();

    /**
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length() + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     */
    WServletRequest(IAttributeFactory iAttributeFactory, HttpServletRequest request, String path,
            HttpServletResponse response, PortMethod method)
    {
        super(null, method, WPTool.notNullAndEmpty(path) ? path : WMainServlet.getPath(request),
                false);
        this.iAttributeFactory = iAttributeFactory;
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

        if (v == null)
        {
            if (NAME_REQUEST.equals(name))
            {
                v = request;
            } else if (NAME_RESPONSE.equals(name))
            {
                v = response;
            } else if (NAME_SERVLET_CONTEXT.equals(name))
            {
                v = request.getServletContext();
            } else if (NAME_SESSION.equals(name))
            {
                v = request.getSession();
            }
        }

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
        String host = getHostFromURL(request.getRequestURL());
        if (http2Https && host.startsWith("http:"))
        {
            host = "https:" + host.substring("http:".length());
        }
        stringBuilder.append(host);
        stringBuilder.append(urlPrefix != null ? urlPrefix : WMainServlet.getUriPrefix(request));
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
        return url.subSequence(host.length(), url.length()).toString();
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

    /**
     * 见{@linkplain #getHostFromURL(CharSequence)}
     *
     * @return
     */
    public String getHost()
    {
        return getHostFromURL(request.getRequestURL());
    }

    /**
     * 见{@linkplain #getHostFromURL(CharSequence, boolean)}
     *
     * @return
     */
    public String getHost(boolean http2Https)
    {
        return getHostFromURL(request.getRequestURL(), http2Https);
    }

    @Override
    public IAttribute getIAttribute(WObject wObject)
    {
        return iAttributeFactory == null ? null : iAttributeFactory.getIAttribute(wObject);
    }
}
