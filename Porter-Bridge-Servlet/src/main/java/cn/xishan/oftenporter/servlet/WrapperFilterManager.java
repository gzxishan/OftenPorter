package cn.xishan.oftenporter.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-28.
 */
public interface WrapperFilterManager
{
    class Wrapper{
        private HttpServletRequest request;
        private HttpServletResponse response;

        public Wrapper(HttpServletRequest request, HttpServletResponse response)
        {
            this.request = request;
            this.response = response;
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
        public Wrapper doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
    }

    public static WrapperFilterManager getWrapperFilterManager(ServletContext servletContext)
    {
        return (WrapperFilterManager) servletContext.getAttribute(WrapperFilterManager.class.getName());
    }

    void addWrapperFilter(WrapperFilter wrapperFilter);

    List<WrapperFilter> wrapperFilters();
}