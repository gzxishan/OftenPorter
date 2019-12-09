package cn.xishan.oftenporter.porter.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    public static abstract class RFile
    {
        private String path;
        private boolean isDir;

        public RFile(String path, boolean isDir)
        {
            this.path = path;
            this.isDir = isDir;
        }

        public boolean isDir()
        {
            return isDir;
        }

        public String getPath()
        {
            return path;
        }

        public abstract InputStream getInputStream() throws IOException;
    }

    public static class RFileOfFile extends RFile
    {
        private File file;

        public RFileOfFile(String path, File file)
        {
            super(path, file.isDirectory());
            this.file = file;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            return new FileInputStream(file);
        }
    }

    public static class RFileInJar extends RFile
    {
        private String jarFilePath;
        private JarEntry jarEntry;

        public RFileInJar(String jarFile, JarEntry jarEntry)
        {
            super(jarEntry.getName(), jarEntry.isDirectory());
            this.jarFilePath = jarFile;
            this.jarEntry = jarEntry;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            JarFile jarFile = new JarFile(jarFilePath);

            InputStream jin = jarFile.getInputStream(jarEntry);
            InputStream inputStream = new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    return jin.read();
                }

                @Override
                public void close() throws IOException
                {
                    OftenTool.close(jin);
                    OftenTool.close(jarFile);
                }
            };
            return inputStream;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);
    private static ClassLoader[] theClassLoaders;
    private static Map<String, String> suffixMimeTypeMap, mimeTypeSuffixMap;

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
            String str = null;
            if (inputStream != null)
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
        if (url == null)
        {
            throw new IOException("not found resource:" + path);
        }
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

    /**
     * 获取某包下所有资源，以"/"结尾的表示目录
     *
     * @param pathName  包名
     * @param recursive 是否遍历子目录
     */
    public static List<RFile> listResources(String pathName, ClassLoader classLoader, boolean recursive)
    {
        try
        {
            return _listResources(pathName, classLoader, recursive);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取某包下所有类.
     * URLDecoder.decode(url.getFile(),ENCODING)：对中文的支持
     *
     * @param pathName  路径
     * @param recursive 是否遍历子目录
     * @return 类的完整名称
     */
    private static List<RFile> _listResources(String pathName, ClassLoader classLoader,
            boolean recursive) throws UnsupportedEncodingException
    {
        List<RFile> fileNames = null;
        ClassLoader loader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(pathName);
        if (url != null)
        {
            String type = url.getProtocol();
            //LogUtil.printLnPos(url);
            if (type.equals("file"))
            {
                fileNames = getResourcesByFile(pathName, URLDecoder.decode(url.getFile(), PackageUtil.ENCODING),
                        recursive);
            } else if (type.equals("jar"))
            {
                fileNames = getResourcesByJar(URLDecoder.decode(url.getFile(), PackageUtil.ENCODING), recursive);
            }
        } else
        {
            fileNames = getResourcesByJars(getUrls(loader, pathName), pathName, recursive);
        }
        return fileNames;
    }

    private static URL[] getUrls(ClassLoader classLoader, String pathName)
    {
        try
        {
            Enumeration<URL> enumeration = classLoader.getResources(pathName);
            ArrayList<URL> list = new ArrayList<>();
            while (enumeration.hasMoreElements())
            {
                URL url = enumeration.nextElement();
                list.add(url);
            }
            return list.toArray(new URL[0]);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param pathName
     * @param filePath  文件路径
     * @param recursive 是否遍历子包
     * @return 类的完整名称
     */
    private static List<RFile> getResourcesByFile(String pathName, String filePath,
            boolean recursive)
    {
        List<RFile> list = new ArrayList<>();
        File file = new File(filePath);

        File[] childFiles = file.listFiles();
        if (childFiles == null)
        {
            return list;
        }
        for (File childFile : childFiles)
        {
            String name = pathName + "/" + childFile.getName();

            list.add(new RFileOfFile(name, childFile));
            if (childFile.isDirectory())
            {
                if (recursive)
                {
                    list.addAll(getResourcesByFile(name, childFile.getPath(), recursive));
                }
            }
        }

        return list;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath   jar文件路径
     * @param recursive 是否遍历子包
     * @return 类的完整名称
     */
    private static List<RFile> getResourcesByJar(String jarPath, boolean recursive)
    {
        List<RFile> list = new ArrayList<>();

        int index = jarPath.indexOf('!');

        //String[] jarInfo = {jarPath.substring(0, index), jarPath.substring(index + 1)};
        String jarFilePath;//jarInfo[0].substring(jarInfo[0].indexOf("/"));
        try
        {
            jarFilePath = new URL(jarPath.substring(0, index)).getFile();
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        String packagePath = jarPath.substring(index + 1);// jarInfo[1].substring(1);
        if (packagePath.startsWith("/"))
        {
            packagePath = packagePath.substring(1);
        }
        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements())
            {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();

                if (recursive)
                {
                    if (entryName.startsWith(packagePath))
                    {
                        list.add(new RFileInJar(jarFilePath, jarEntry));
                    }
                } else
                {
                    index = entryName.lastIndexOf("/");
                    String myPackagePath;
                    if (index != -1)
                    {
                        myPackagePath = entryName.substring(0, index);
                    } else
                    {
                        myPackagePath = entryName;
                    }
                    if (myPackagePath.equals(packagePath))
                    {
                        list.add(new RFileInJar(jarFilePath, jarEntry));
                    }
                }
            }

        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        } finally
        {
            if (jarFile != null)
            {
                try
                {
                    jarFile.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls      URL集合
     * @param pathName  包路径
     * @param recursive 是否遍历子包
     * @return 类的完整名称
     */
    private static List<RFile> getResourcesByJars(URL[] urls, String pathName, boolean recursive)
    {
        List<RFile> list = new ArrayList<>();
        if (urls != null)
        {
            for (URL url : urls)
            {
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes" + File.separator))
                {
                    continue;
                }
                String jarPath = urlPath + "!/" + pathName;
                list.addAll(getResourcesByJar(jarPath, recursive));
            }
        }
        return list;
    }

    private static void initMimeTypeMap()
    {
        Map<String, String> suffixMimeTypeMap = ResourceUtil.suffixMimeTypeMap;
        Map<String, String> mimeTypeSuffixMap;
        if (suffixMimeTypeMap == null)
        {
            synchronized (ResourceUtil.class)
            {
                String content = FileTool.getString(ResourceUtil.class.getResourceAsStream("/often/minetypes.txt"));
                suffixMimeTypeMap = new HashMap<>();
                mimeTypeSuffixMap = new HashMap<>();
                if (content != null)
                {
                    String[] types = OftenStrUtil.split(content, "\n");
                    for (String type : types)
                    {
                        int index = type.indexOf("=");
                        String suffix = type.substring(0, index).trim();
                        String mime = type.substring(index + 1).trim();
                        suffixMimeTypeMap.put(suffix, mime);
                        mimeTypeSuffixMap.put(mime, suffix);
                    }
                }
                ResourceUtil.suffixMimeTypeMap = suffixMimeTypeMap;
                ResourceUtil.mimeTypeSuffixMap = mimeTypeSuffixMap;
            }
        }
    }

    public static String getSuffixByMimeType(String type)
    {
        initMimeTypeMap();
        if (OftenTool.isEmpty(type))
        {
            return null;
        }
        String suffix = mimeTypeSuffixMap.get(type);
        return suffix;
    }

    public static String getMimeType(String filename)
    {
        if (OftenTool.isEmpty(filename))
        {
            return null;
        }
        String suffix = OftenStrUtil.getSuffix(filename, '.');
        initMimeTypeMap();
        String mineType = suffixMimeTypeMap.get(suffix);
        if (mineType != null)
        {
            return mineType;
        } else
        {
            return "application/octet-stream";
        }
    }

}
