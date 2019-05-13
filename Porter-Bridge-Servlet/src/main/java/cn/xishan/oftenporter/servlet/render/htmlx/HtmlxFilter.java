package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.servlet.OftenServlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxFilter implements Filter
{
    private OftenServlet oftenServlet;
    private String oftenPath;
    private String id;

    HtmlxFilter(OftenServlet oftenServlet, String oftenPath)
    {
        this.oftenServlet = oftenServlet;
        this.oftenPath = oftenPath;
        this.id = getClass().getSimpleName() + "@" + this.hashCode() + "@" + OftenKeyUtil.randomUUID();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if ("GET".equals(request.getMethod()))
        {
            if (request.getAttribute(id) == null)
            {
                request.setAttribute(id, true);
                HtmlxDoc.ResponseType responseType = (HtmlxDoc.ResponseType) request
                        .getAttribute(HtmlxDoc.ResponseType.class.getName());

                if (responseType == HtmlxDoc.ResponseType.ServletDefault)
                {
                    chain.doFilter(servletRequest, servletResponse);
                } else
                {
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    oftenServlet.doRequest(request, oftenPath, response, PortMethod.GET);

                    if (responseType != HtmlxDoc.ResponseType.Break)
                    {
                        chain.doFilter(servletRequest, servletResponse);
                    }
                }
            } else
            {
                //已经执行过
                chain.doFilter(servletRequest, servletResponse);
            }

        } else
        {
            chain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy()
    {

    }
}
