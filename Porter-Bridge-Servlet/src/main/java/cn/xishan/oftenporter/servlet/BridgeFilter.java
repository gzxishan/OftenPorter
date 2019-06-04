//package cn.xishan.oftenporter.servlet;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.util.EnumSet;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 所有的often servlet请求都通过该过滤器进行转接。
// *
// * @author Created by https://github.com/CLovinr on 2019-06-04.
// */
//public final class BridgeFilter implements Filter
//{
//    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeFilter.class);
//
//    private static Map<String, HttpServlet> oftenContext2Servlet = new HashMap<>();
//
//
//    static void bind(String oftenContext, HttpServlet servlet)
//    {
//        if (oftenContext2Servlet.containsKey(oftenContext))
//        {
//            LOGGER.warn("already bind:{},last={}", oftenContext, oftenContext2Servlet.get(oftenContext));
//        }
//        oftenContext2Servlet.put(oftenContext, servlet);
//    }
//
//    static void init(ServletContext servletContext)
//    {
//        BridgeFilter bridgeFilter = new BridgeFilter();
//        FilterRegistration.Dynamic dynamic = servletContext.addFilter(BridgeFilter.class.getName(), bridgeFilter);
//        dynamic.setAsyncSupported(true);
//        dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true,
//                "/*");
//        servletContext.setAttribute(BridgeFilter.class.getName(), bridgeFilter);
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException
//    {
//        LOGGER.debug("{}", filterConfig);
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
//            FilterChain chain) throws IOException, ServletException
//    {
//        if (doFilterJustForOften(servletRequest, servletResponse))
//        {
//            return;
//        }
//        chain.doFilter(servletRequest, servletResponse);
//    }
//
//    public boolean doFilterJustForOften(ServletRequest servletRequest,
//            ServletResponse servletResponse) throws IOException, ServletException
//    {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        String path = OftenServletRequest.getPath(request);
//        if (path.startsWith("/"))
//        {
//            int index = path.indexOf('/', 1);
//            if (index > 0)
//            {
//                String oftenContext = path.substring(1, index);
//                HttpServlet servlet = oftenContext2Servlet.get(oftenContext);
//                if (servlet != null)
//                {
//                    servlet.service(servletRequest, servletResponse);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void destroy()
//    {
//        oftenContext2Servlet.clear();
//    }
//}
