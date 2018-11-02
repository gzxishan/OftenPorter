package cn.xishan.oftenporter.porter.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * <p>
 * path支持的格式：
 * <ol>
 * <li>
 * classpath:或省略，表示java资源
 * </li>
 * <li>
 * file等：表示其他协议的资源
 * </li>
 * </ol>
 * </p>
 *
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

    public static InputStream getAbsoluteResourceStream(String path) throws IOException
    {
        URL url = getAbsoluteResource(path);
        String protocol = url.getProtocol();
        if ("file".equals(protocol))
        {
            File file = new File(url.getFile());
            return new FileInputStream(file);
        } else
        {
            return url.openStream();
        }
    }


    public static URL getAbsoluteResource(String path)
    {
        return _getAbsoluteResource(path, true);
    }

    private static URL _getAbsoluteResource(String path, boolean recursive)
    {
        if (path.startsWith("classpath:"))
        {
            path = path.substring("classpath:".length());
        }
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

    private static Enumeration<URL> _getAbsoluteResources(String path, boolean recursive) throws IOException
    {
        if (path.startsWith("classpath:"))
        {
            path = path.substring("classpath:".length());
        }
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
}
