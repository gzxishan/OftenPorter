package cn.xishan.oftenporter.porter.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        try (InputStream inputStream = getAbsoluteResourceStream(path))
        {
            URL url = getAbsoluteResource(path);
            String str = null;
            if (url != null)
            {
                str = FileTool.getString(inputStream, 1024, encoding);
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
        return url.openStream();
    }


    public static URL getAbsoluteResource(String path)
    {
        return _getAbsoluteResource(path, true);
    }

    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^([a-zA-Z0-9]{2,}):");

    private static URL _fromNotClassResource(String path)
    {
        Matcher matcher = PROTOCOL_PATTERN.matcher(path);
        if (matcher.find())
        {
            try
            {
                return new URL(path);
            } catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }
        } else
        {
            return null;
        }
    }

    private static URL _getAbsoluteResource(String path, boolean recursive)
    {
        URL url = null;
        if (path.startsWith("classpath:"))
        {
            path = path.substring("classpath:".length());
        } else
        {
            url = _fromNotClassResource(path);
            if (url != null)
            {
                return url;
            }
        }

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
        } else
        {
            URL url = _fromNotClassResource(path);
            if (url != null)
            {
                List<URL> list = new ArrayList<>(1);
                list.add(url);
                return new EnumerationImpl<>(list);
            }
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
