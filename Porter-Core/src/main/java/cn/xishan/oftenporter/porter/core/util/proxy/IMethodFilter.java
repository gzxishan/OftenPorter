package cn.xishan.oftenporter.porter.core.util.proxy;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/20.
 */
public interface IMethodFilter
{
    boolean contains(Object object,Method method);
}
