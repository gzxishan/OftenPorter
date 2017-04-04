package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/5.
 */
public interface PorterData
{
    Enumeration<String> ofContexts();

    Enumeration<Porter> ofContextPorters(String contextName);
}
