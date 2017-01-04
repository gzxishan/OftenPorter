package cn.xishan.oftenporter.porter.core;

/**
 * @author Created by https://github.com/CLovinr on 2016/12/11.
 */
public interface PorterAttr
{
    /**
     * 得到指定context的类加载器。
     * @param contextName
     * @return
     */
    ClassLoader getClassLoader(String contextName);
}
