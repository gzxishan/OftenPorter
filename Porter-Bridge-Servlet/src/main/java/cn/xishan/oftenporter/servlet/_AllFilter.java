package cn.xishan.oftenporter.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 所有的often servlet请求都通过该过滤器进行转接。
 *
 * @author Created by https://github.com/CLovinr on 2019-06-04.
 */
public final class _AllFilter
{

    public static abstract class FilterX implements Filter, Callback
    {

        @Override
        public final void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException
        {
            _AllFilter.doFilter(request, response, chain, this);
        }
    }

    public interface Callback
    {
        void doSelf(HttpServletRequest request, HttpServletResponse response,
                FilterChain chain) throws IOException, ServletException;
    }

    public static void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain,
            Callback callback)
            throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String key = "---Wrapper--Filter--Manager--";
        if (request.getAttribute(key) == null)
        {
            request.setAttribute(key, true);
            WrapperFilterManager wrapperFilterManager = WrapperFilterManager.getWrapperFilterManager(
                    request.getServletContext());
            for_outer:
            for (WrapperFilterManager.WrapperFilter wrapperFilter : wrapperFilterManager.wrapperFilters())
            {
                WrapperFilterManager.Wrapper wrapper = wrapperFilter.doFilter(request, response);
                if (wrapper != null)
                {
                    request = wrapper.getRequest();
                    response = wrapper.getResponse();
                    switch (wrapper.getFilterResult())
                    {
                        case RETURN:
                            return;
                        case BREAK:
                            break for_outer;
                        case CONTINUE:
                            break;
                    }
                }
            }
        }


        callback.doSelf(request, response, chain);
    }

}
