package cn.xishan.oftenporter.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 用于在服务端WebSocket解析之前进行过滤操作。
 * @author Created by https://github.com/CLovinr on 2018-11-28.
 */
public interface WrapperFilterManager
{
    enum FilterResult
    {
        /**
         * 终止后面的
         */
        BREAK,
        /**
         * 继续
         */
        CONTINUE,
        /**
         * 返回并终止servlet过滤器的执行
         */
        RETURN
    }

    class Wrapper
    {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private FilterResult filterResult = FilterResult.CONTINUE;

        public Wrapper(HttpServletRequest request, HttpServletResponse response)
        {
            this.request = request;
            this.response = response;
        }

        public FilterResult getFilterResult()
        {
            return filterResult;
        }

        public void setFilterResult(FilterResult filterResult)
        {
            this.filterResult = filterResult;
        }

        public HttpServletRequest getRequest()
        {
            return request;
        }

        public void setRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        public HttpServletResponse getResponse()
        {
            return response;
        }

        public void setResponse(HttpServletResponse response)
        {
            this.response = response;
        }
    }

    public interface WrapperFilter
    {
        public Wrapper doFilter(HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException;
    }

    public static WrapperFilterManager getWrapperFilterManager(ServletContext servletContext)
    {
        return (WrapperFilterManager) servletContext.getAttribute(WrapperFilterManager.class.getName());
    }

    void addWrapperFilter(WrapperFilter wrapperFilter);

    /**
     * 添加到第一个。
     * @param wrapperFilter
     */
    void addFirstWrapperFilter(WrapperFilter wrapperFilter);

    List<WrapperFilter> wrapperFilters();
}
