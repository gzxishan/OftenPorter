package cn.xishan.oftenporter.servlet.tomcat;

import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Created by https://github.com/CLovinr on 2018/11/3.
 */
public class SimpleInstanceManager implements InstanceManager
{
    @Override
    public Object newInstance(
            Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, IllegalArgumentException, NoSuchMethodException, SecurityException
    {
        return WPTool.newObject(clazz);
    }

    @Override
    public Object newInstance(
            String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException
    {
        return WPTool.newObject(className);
    }

    @Override
    public Object newInstance(Object object) throws Exception
    {
        return object;
    }

    @Override
    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException
    {

    }
}
