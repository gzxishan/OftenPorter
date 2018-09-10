package cn.xishan.oftenporter.porter.core.init;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/8.
 */
public interface IOtherStartDestroy
{
    void addOtherStarts(Object object, Method[] starts);

    void addOtherDestroys(Object object, Method[] destroys);
}
