package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.annotation.Importer;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 通过实现该接口，可以启动框架。
 * <p>
 * 访问地址格式为：http(s)://host:port/servletContextPath/opContext/classTied/funTied
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/4/18.
 */
public interface OPServletInitializer
{
    interface BuilderBefore
    {
        void setPName(String pName);

        String getPName();

        void setDoPUT(boolean willDo);

        boolean isDoPUT();

        void setMultiPartOption(MultiPartOption multiPartOption);
    }

    interface Builder
    {
        /**
         * 另见:{@linkplain Importer}
         *
         * @param importers
         * @return
         */
        PorterConf newPorterConfWithImporterClasses(Class... importers);

        /**
         * 见{@linkplain #newPorterConfWithImporterClasses(Class[])}
         *
         * @return
         */
        default PorterConf newPorterConf()
        {
            return newPorterConfWithImporterClasses();
        }

        void startOne(PorterConf porterConf);

        void setCustomServletPaths(CustomServletPath... customServletPaths);

        PLinker getPLinker();

    }

    void beforeStart(ServletContext servletContext, BuilderBefore builderBefore) throws Exception;

    void onStart(ServletContext servletContext, Builder builder) throws Exception;

    default void onDestroyed()
    {

    }

    default void onDoRequest(StartupServlet startupServlet, HttpServletRequest request, HttpServletResponse response,
            PortMethod method) throws IOException, ServletException
    {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        startupServlet.doRequest(request, path, response, method);
    }
}
