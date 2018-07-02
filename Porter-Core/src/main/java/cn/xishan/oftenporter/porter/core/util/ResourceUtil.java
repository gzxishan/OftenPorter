package cn.xishan.oftenporter.porter.core.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-02.
 */
public class ResourceUtil
{
    private static ClassLoader[] theClassLoaders;

    static
    {
        setClassLoaders(Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader());
    }

    public static void setClassLoaders(ClassLoader... classLoaders)
    {
        theClassLoaders = classLoaders;
    }

    public static List<String> getAbsoluteResourcesString(String path, String encoding)
    {
        try
        {
            Enumeration<URL> enumeration = getAbsoluteResources(path);
            List<String> list = new ArrayList<>();
            while (enumeration.hasMoreElements())
            {
                URL url = enumeration.nextElement();
                list.add(FileTool.getString(url.openStream(), 1024, encoding));
            }
            return list;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Enumeration<URL> getAbsoluteResources(String path) throws IOException
    {
        return _getAbsoluteResources(path, true);
    }

    private static Enumeration<URL> _getAbsoluteResources(String path, boolean recursive) throws IOException
    {
        Enumeration<URL> enumeration = EnumerationImpl.getEMPTY();
        for (ClassLoader classLoader : theClassLoaders)
        {
            enumeration = classLoader.getResources(path);
            if (enumeration.hasMoreElements())
            {
                break;
            }
        }
        if (recursive && !enumeration.hasMoreElements())
        {
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            } else
            {
                path = "/" + path;
            }
            enumeration = _getAbsoluteResources(path, false);
        }
        return enumeration;
    }

    public static String getAbsoluteResourceString(String path, String encoding)
    {
        try
        {
            URL url = getAbsoluteResource(path);
            String str = null;
            if (url != null)
            {
                str = FileTool.getString(url.openStream(), 1024, encoding);
            }
            return str;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static URL getAbsoluteResource(String path)
    {
        return _getAbsoluteResource(path, true);
    }

    private static URL _getAbsoluteResource(String path, boolean recursive)
    {
        URL url = null;
        for (ClassLoader classLoader : theClassLoaders)
        {
            url = classLoader.getResource(path);
            if (url != null)
            {
                break;
            }
        }
        if (url == null && recursive)
        {
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            } else
            {
                path = "/" + path;
            }
            url = _getAbsoluteResource(path, false);
        }
        return url;
    }
}
