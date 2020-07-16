package cn.xishan.oftenporter.servlet.render.mapping;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.servlet.StartupServlet;
import cn.xishan.oftenporter.servlet._AllFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class PathMappingFilter extends _AllFilter.FilterX
{
    static final String FILTER_NAME = "__servlet.render.mapping.PathMappingFilter__";

    private StartupServlet startupServlet;
    private String oftenPath;
    private String id;
    private Set<PortMethod> methods;

    PathMappingFilter(StartupServlet startupServlet, String oftenPath, Set<PortMethod> methods)
    {
        this.startupServlet = startupServlet;
        this.oftenPath = oftenPath;
        this.id = getClass().getSimpleName() + "@" + this.hashCode() + "@" + OftenKeyUtil.randomUUID();
        this.methods = methods;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }


    @Override
    public void destroy()
    {

    }

    @Override
    public void doSelf(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        PortMethod method = PortMethod.valueOf(request.getMethod());
        if (methods.contains(method))
        {
            if (request.getAttribute(id) == null)
            {
                request.setAttribute(id, true);
                request.setAttribute(FILTER_NAME, chain);
                startupServlet.doRequest(request, oftenPath, response, method);
                request.removeAttribute(FILTER_NAME);
            } else
            {
                //已经执行过
                chain.doFilter(request, response);
            }
        } else
        {
            chain.doFilter(request, response);
        }
    }
}
