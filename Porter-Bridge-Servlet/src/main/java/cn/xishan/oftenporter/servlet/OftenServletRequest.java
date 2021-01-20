package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
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
    private String encoding;

    /**
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length() + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     */
    OftenServletRequest(HttpServletRequest request, String path,
            HttpServletResponse response, PortMethod method, String encoding)
    {
        super(null, method, OftenTool.notEmpty(path) ? path : OftenServlet.getOftenPath(request),
                false);
        this.request = new WeakReference<>(request);
        this.response = new WeakReference<>(response);
        this.encoding = encoding;
    }

    @Override
    public HttpServletRequest getOriginalRequest()
    {
        HttpServletRequest request = this.request.get();
        if (request == null)
        {
            LOGGER.warn("released weak ref of request");
        }
//        if (request == null)
//        {
//            throw new OftenCallException("request weak reference is released!");
//        }
        return request;
    }

    @Override
    public HttpServletResponse getOriginalResponse()
    {
        HttpServletResponse response = this.response.get();
        if (response == null)
        {
            LOGGER.warn("released weak ref of response");
        }
//        if (response == null)
//        {
//            throw new OftenCallException("response weak reference is released!");
//        }
        return response;
    }


    public AsyncContext startAsync()
    {
        HttpServletRequest request = getOriginalRequest();
        if (request == null)
        {
            throw new NullPointerException("request is null.");
        }
        return request.startAsync(request, response.get());
    }

//    private boolean hasParsedQuery = false;
//    //解决post请求时，表单参数类型，body参数为空，query参数未被解析的问题
//    private void parseQueryParams()
//    {
//        if (!hasParsedQuery)
//        {
//            hasParsedQuery = true;
//
//            if (super.params == null)
//            {
//                super.params = new HashMap<>();
//            }
//
//            HttpServletRequest request = getOriginalRequest();
//            if (request != null)
//            {
//                String query = request.getQueryString();
//                if (query != null)
//                {
//                    try
//                    {
//                        super.params.putAll(BodyParamSourceHandle.fromEncoding(query, encoding));
//                    } catch (UnsupportedEncodingException e)
//                    {
//                        throw new OftenCallException(e);
//                    }
//                }
//            }
//        }
//    }

    @Override
    public Map<String, Object> getParameterMap()
    {
        if (super.params == null)
        {
            super.params = new HashMap<>();
            HttpServletRequest request = getOriginalRequest();
            if (request != null)
            {
//                parseQueryParams();
                Enumeration<String> e = request.getParameterNames();
                while (e.hasMoreElements())
                {
                    String name = e.nextElement();
                    Object value = getParameter(name);
                    if (OftenTool.notNullAndEmptyCharSequence(value))
                    {
                        params.put(name, value);
                    }
                }
            }
        }
        return super.getParameterMap();
    }

    @Override
    public Object getParameter(String name)
    {
        HttpServletRequest request = getOriginalRequest();
        Object value = null;
        if (request != null)
        {
            value = request.getParameter(name);
        }

//        if (OftenTool.isNullOrEmptyCharSequence(value))
//        {
//            parseQueryParams();
//            value = super.params.get(name);
//        }

        return value;
    }


    @Override
    public PortMethod getMethod()
    {
        return method;
    }


    public static String getRequestUrl(OftenObject oftenObject, boolean http2Https)
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        return getRequestUrl(request, http2Https);
    }

    public static String getRequestUrl(HttpServletRequest request, boolean http2Https)
    {
        String host = getHost(request, http2Https);
        String uri = request.getRequestURI();
        return host + uri;
    }


    public static String getRequestUrlWithQuery(OftenObject oftenObject, boolean http2Https)
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        return getRequestUrlWithQuery(request, http2Https);
    }

    public static String getRequestUrlWithQuery(HttpServletRequest request, boolean http2Https)
    {
        String url = getRequestUrl(request, http2Https);

        String query = request.getQueryString();
        if (query != null)
        {
            url += "?" + query;
        }
        return url;
    }

    /**
     * 获得接口地址。见{@linkplain #getPortUrl(OftenObject, String, String, String, String, String, boolean)}。
     *
     * @param oftenObject
     * @param funTied     若为null，则使用当前的。
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
     * @param funTied     若为null，则使用当前的。
     * @return
     */
    public static String getPortUrl(OftenObject oftenObject, String classTied, String funTied, boolean http2Https)
    {
        return getPortUrl(oftenObject, null, null, null, classTied, funTied, http2Https);
    }


    /**
     * 获得接口地址,不含"**="与"*=*"参数。<strong>注意：</strong>Servlet转接会出错。
     *
     * @param oftenObject
     * @param bridgeName  若为null，则不含bridgeName部分。
     * @param contextName 若为null，则使用当前的。
     * @param classTied   若为null，则使用当前的。
     * @param funTied     若为null，则使用当前的。
     * @param http2Https  是否http变成成https
     * @return
     */
    public static String getPortUrl(OftenObject oftenObject, String urlPrefix, String bridgeName, String contextName,
            String classTied, String funTied,
            boolean http2Https)
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        StringBuilder stringBuilder = new StringBuilder();
        String host = getHost(request, http2Https);

        stringBuilder.append(host);
        stringBuilder.append(urlPrefix != null ? urlPrefix : OftenServlet.getUriPrefix(request));
        if (bridgeName != null)
        {
            stringBuilder.append("/=").append(bridgeName);
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

    public static String removePort(String host)
    {
        int index = host.indexOf(":", host.indexOf(":") + 1);
        if (index == -1)
        {
            return host;
        } else
        {
            return host.substring(0, index);
        }
    }


    public static String getPathFromURLWithQuery(CharSequence url)
    {
        return getPathFromURL(url, true);
    }

    public static String getPathFromURL(CharSequence url)
    {
        return getPathFromURL(url, false);
    }

    public static String getPathFromURL(CharSequence url, boolean withQuery)
    {
        if (url == null)
        {
            return null;
        }
        String host = getHostFromURL(url);
        String path = url.subSequence(host.length(), url.length()).toString();
        if (!withQuery)
        {
            int index = path.lastIndexOf("?");
            if (index >= 0)
            {
                path = path.substring(0, index);
            }
        }
        return path;
    }


    private static final Pattern HOST_PATTERN = Pattern.compile("^([^:]+:/+)([^/]+)");

    /**
     * 得到host，包含协议。如http://localhost:8080/hello得到的是http://localhost:8080
     *
     * @param url
     * @return
     */
    public static String getHostFromURL(CharSequence url, boolean http2Https)
    {
        Matcher matcher = HOST_PATTERN.matcher(url);
        String host;
        if (matcher.find())
        {
            if (http2Https && matcher.group(1).equals("http://"))
            {
                host = "https://" + matcher.group(2);
            } else
            {
                host = matcher.group(1) + matcher.group(2);
            }
        } else
        {
            host = "";
        }

        return host;
    }


//    private static String getPort(HttpServletRequest request)
//    {
//        if (request.getServerPort() == 80 || request.getServerPort() == 443)
//        {
//            return "";
//        } else
//        {
//            return ":" + request.getServerPort();
//        }
//    }

    private static final Pattern PROTO_PATTERN = Pattern.compile("^([^:]+:)(/+[\\s\\S]+)");

    /**
     * 返回格式包含：协议、地址、端口。会尝试从host头获取host内容、从X-FORWARDED-PROTO头判断是否为https协议。
     */
    public static String getHost(HttpServletRequest request, boolean isHttp2Https)
    {
        String hostHeader = request.getHeader("Host");
        String host;
        if (OftenTool.isEmpty(hostHeader))
        {
            String url = request.getRequestURL().toString();
            host = getHostFromURL(url);
        } else
        {
            host = hostHeader;
        }

        if (!isHttp2Https && "https".equals(request.getHeader("X-FORWARDED-PROTO")))
        {
            isHttp2Https = true;
        }

        if (isHttp2Https && host.endsWith(":443"))
        {
            host = host.substring(0, host.length() - 4);
        } else if (!isHttp2Https && host.endsWith(":80"))
        {
            host = host.substring(0, host.length() - 3);
        }

        Matcher matcher = PROTO_PATTERN.matcher(host);
        if (matcher.find())
        {
            if (isHttp2Https && matcher.group(1).equals("http:"))
            {
                host = "https:" + matcher.group(2);
            }
        } else
        {
            host = (isHttp2Https ? "https://" : "http://") + host;
        }

        return host;
//
//        String host = (isHttp2Https ? "https" : request.getScheme()) + "://" + request.getServerName() + getPort(
//                request);
//        return host;
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
        HttpServletRequest request = getOriginalRequest();
        return getHost(request, http2Https);

//        String host;
//        if (http2Https && request.getScheme().equals("http"))
//        {
//            host = "https://" + request.getServerName() + getPort(request);
//        } else
//        {
//            host = getHost(request, false);
//        }
//        return host;
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
