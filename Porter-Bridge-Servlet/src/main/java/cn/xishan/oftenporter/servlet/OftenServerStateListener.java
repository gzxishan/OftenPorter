package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.init.PorterConf;

import javax.servlet.ServletContext;

/**
 * @author Created by https://github.com/CLovinr on 2019-08-08.
 */
public interface OftenServerStateListener
{
    void beforeInit(ServletContext servletContext);

    void afterInit(ServletContext servletContext);

    default void onNewOne(PorterConf porterConf)
    {

    }

    default void onStartOne(PorterConf porterConf)
    {

    }

    default void onDestroyOne(String oftenContext)
    {

    }

    default void onDestroyAll()
    {

    }

    void onDestroyed();
}
