package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.servlet.render.RenderPage;
import org.jsoup.nodes.Document;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxEndFilter implements Filter
{
    private String id;

    HtmlxEndFilter()
    {
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
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                HtmlxDoc.ResponseType responseType = (HtmlxDoc.ResponseType) request
                        .getAttribute(HtmlxDoc.ResponseType.class.getName());
                if (responseType == HtmlxDoc.ResponseType.Normal || responseType == HtmlxDoc.ResponseType.Next)
                {
                    RenderData renderData = (RenderData) request.getAttribute(RenderData.class.getName());
                    HtmlxDoc htmlxDoc = renderData.getHtmlxDoc();
                    Document document = (Document) htmlxDoc.getDocument();
                    String htmlStr = document.outerHtml();
                    for (Object rs : renderData.getReturnObjects())
                    {
                        Map<String, ?> map;
                        if (rs instanceof RenderPage)
                        {
                            map = ((RenderPage) rs).getData();
                        } else
                        {
                            map = (Map) rs;
                        }
                        htmlStr = OftenStrUtil.replaceSharpProperties(htmlStr, map, "");
                    }

                    byte[] bytes = htmlStr.getBytes(Charset.forName(htmlxDoc.getEncoding()));
                    response.setContentLength(bytes.length);
                    try (OutputStream os = response.getOutputStream())
                    {
                        os.write(bytes);
                        os.flush();
                    }
                } else if (responseType != HtmlxDoc.ResponseType.Break)
                {
                    chain.doFilter(servletRequest, servletResponse);
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
