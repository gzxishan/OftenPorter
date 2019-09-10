package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.servlet.StartupServlet;
import cn.xishan.oftenporter.servlet._AllFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxFilter extends _AllFilter.FilterX
{
    private StartupServlet startupServlet;
    private String oftenPath;
    private String id;

    HtmlxFilter(StartupServlet startupServlet, String oftenPath)
    {
        this.startupServlet = startupServlet;
        this.oftenPath = oftenPath;
        this.id = getClass().getSimpleName() + "@" + this.hashCode() + "@" + OftenKeyUtil.randomUUID();
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
        if ("GET".equals(request.getMethod()))
        {
            if (request.getAttribute(id) == null)
            {
                request.setAttribute(id, true);
                HtmlxDoc.ResponseType responseType = (HtmlxDoc.ResponseType) request
                        .getAttribute(HtmlxDoc.ResponseType.class.getName());

                if (responseType == HtmlxDoc.ResponseType.ServletDefault)
                {
                    chain.doFilter(request, response);
                } else
                {
                    startupServlet.doRequest(request, oftenPath, response, PortMethod.GET);
                    if (responseType != HtmlxDoc.ResponseType.Break)
                    {
                        chain.doFilter(request, response);
                    }
                }
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
