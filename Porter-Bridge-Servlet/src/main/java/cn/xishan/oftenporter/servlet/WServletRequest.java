package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WServletRequest extends PRequest
{
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * @param request
     * @param path     当为null时，使用request.getRequestURI().substring(request.getContextPath().length() + request
     *                 .getServletPath().length())
     * @param response
     * @param method
     */
    WServletRequest(HttpServletRequest request, String path, HttpServletResponse response, PortMethod method)
    {
        super(method, path != null ? path : WMainServlet.getPath(request),
                false);
        this.request = request;
        this.response = response;
    }

    @Override
    public Object getOriginalRequest()
    {
        return request;
    }

    @Override
    public HttpServletResponse getOriginalResponse()
    {
        return response;
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
                String value = request.getParameter(name);
                if (WPTool.notNullAndEmpty(value))
                {
                    params.put(name, value);
                }
            }
        }
        return super.getParameterMap();
    }

    @Override
    public String getParameter(String name)
    {
        return request.getParameter(name);
    }


    @Override
    public PortMethod getMethod()
    {
        return method;
    }


//    public HttpServletRequest getServletRequest()
//    {
//        return request;
//    }


    private static final Pattern PATTERN_HOST_PORT = Pattern.compile("^(http|https)://([^/]+)");

    /**
     * 得到host，包含协议。如http://localhost:8080/hello得到的是http://localhost:8080
     *
     * @param url
     * @return
     */
    public static String getHostFromURL(CharSequence url)
    {

        Matcher matcher = PATTERN_HOST_PORT.matcher(url);
        if (matcher.find())
        {
            return matcher.group();
        } else
        {
            return "";
        }
    }

    public String getHost()
    {
        return getHostFromURL(request.getRequestURL());
    }

}
