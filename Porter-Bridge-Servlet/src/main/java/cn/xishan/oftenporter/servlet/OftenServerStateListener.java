package cn.xishan.oftenporter.servlet;

import javax.servlet.ServletContext;

/**
 * @author Created by https://github.com/CLovinr on 2019-08-08.
 */
public interface OftenServerStateListener
{
    void beforeInit(ServletContext servletContext);

    void afterInit(ServletContext servletContext);

    void onDestroyed();
}
