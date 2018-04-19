package cn.xishan.oftenporter.servlet;


import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;

import javax.servlet.ServletContext;

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
        PorterConf newPorterConf();

        void startOne(PorterConf porterConf);

        void setCustomServletPaths(CustomServletPath... customServletPaths);

        PLinker getPLinker();

    }

    void beforeStart(ServletContext servletContext,BuilderBefore builderBefore);

    void onStart(ServletContext servletContext, Builder builder);
}
