package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.annotation.Importer;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeLinker;
import cn.xishan.oftenporter.porter.core.sysset.IAutoVarGetter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 通过实现该接口，可以启动框架。
 * <p>
 * 访问地址格式为：http(s)://host:port/servletContextPath/opContext/classTied/funTied
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/4/18.
 */
public interface OftenInitializer
{
    interface BuilderBefore
    {
        void setBridgeName(String bridgeName);

        String getBridgeName();

        /**
         * 设置是否添加put参数处理,见{@linkplain PutParamSourceHandle PutParamSourceHandle}。
         */
        void setDoPUT(boolean willDo);

        /**
         * 是否添加put参数处理,默认为false,见{@linkplain PutParamSourceHandle PutParamSourceHandle}。
         */
        boolean isDoPUT();

        /**
         * 设置multipart form表单的处理选项,见{@linkplain MultiPartParamSourceHandle}
         *
         * @param multiPartOption
         */
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

        BridgeLinker getBridgeLinker();

        IAutoVarGetter getAutoVarGetter(String context);

    }

    default void beforeStart(ServletContext servletContext, BuilderBefore builderBefore) throws Exception
    {

    }


    /**
     * @param servletContext
     * @param builderBefore
     * @param initializers
     * @return 返回false将会忽略当前初始化对象。
     * @throws Exception
     */
    default boolean beforeStart(ServletContext servletContext, BuilderBefore builderBefore,
            Set<Class<OftenInitializer>> initializers) throws Exception
    {
        beforeStart(servletContext, builderBefore);
        return true;
    }

    /**
     * 在{@linkplain #beforeStart(ServletContext, BuilderBefore, Set)}之后调用。
     *
     * @param servletContext
     * @param initializers   所有被添加的对象。
     */
    default void beforeStart(ServletContext servletContext, List<OftenInitializer> initializers)
    {

    }

    /**
     * 会调用所有的{@linkplain OftenInitializer}，无论
     * {@linkplain OftenInitializer#beforeStart(ServletContext, BuilderBefore, Set)}是否返回true。
     * @param porterConf
     */
    default void beforeStartOneForAll(PorterConf porterConf)
    {

    }

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
