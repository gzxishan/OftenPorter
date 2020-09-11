package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.servlet.websocket.IContainerResource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 所有的often servlet请求都通过该过滤器进行转接。
 *
 * @author Created by https://github.com/CLovinr on 2019-06-04.
 */
public final class _AllFilter
{
    private static final String KEY = "---Wrapper--Filter--Manager--";

    static class RequestWrapper extends HttpServletRequestWrapper implements IContainerResource<HttpServletRequest>
    {
        private boolean hasDispatcher = false;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        public RequestWrapper(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path)
        {
            hasDispatcher = true;
            removeAttribute(KEY);
            return super.getRequestDispatcher(path);
        }

        public boolean isHasDispatcher()
        {
            return hasDispatcher;
        }

        @Override
        public HttpServletRequest containerRes(HttpServletRequest res)
        {
            return (HttpServletRequest) super.getRequest();
        }
    }

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
        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);
        HttpServletRequest request = requestWrapper;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getAttribute(KEY) == null)
        {
            request.setAttribute(KEY, true);
            WrapperFilterManager wrapperFilterManager = WrapperFilterManager.getWrapperFilterManager(
                    request.getServletContext());
            for_outer:
            for (WrapperFilterManager.WrapperFilter wrapperFilter : wrapperFilterManager.wrapperFilters())
            {
                WrapperFilterManager.Wrapper wrapper = wrapperFilter.doFilter(request, response);

                if (requestWrapper.isHasDispatcher())
                {//已经转发了请求，则终止后面的操作
                    return;
                }

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

        if (requestWrapper.isHasDispatcher())
        {//已经转发了请求，则终止后面的操作
            return;
        } else
        {
            callback.doSelf(request, response, chain);
        }
    }

}
