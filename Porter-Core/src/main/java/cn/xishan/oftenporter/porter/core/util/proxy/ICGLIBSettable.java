package cn.xishan.oftenporter.porter.core.util.proxy;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author Created by https://github.com/CLovinr on 2020/3/13.
 */
public interface ICGLIBSettable
{
    void doSet(Enhancer enhancer);
}
