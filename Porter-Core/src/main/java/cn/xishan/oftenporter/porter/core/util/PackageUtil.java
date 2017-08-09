package cn.xishan.oftenporter.porter.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PackageUtil
{

    /**
     * 用于扫描器扫描类时
     */
    public interface IClassLoader
    {


        /**
         * 得到指定包下的所有类名
         *
         * @param packageOrClassName 包或者类名，""表示默认包
         * @param childPackage       是否得到子包下的类名。
         * @return
         */
        List<String> getClassNames(String packageOrClassName, boolean childPackage);

        /**
         * 设置搜索的包。
         *
         * @param packages 存放的是字符串
         */
        void setPackages(List<String> packages);

        /**
         * 搜索
         */
        void seek();

        /**
         * 最终释放资源
         */
        void release();
    }


    public static final String ENCODING = "utf-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageUtil.class);

    public static Class<?> newClass(String className, ClassLoader classLoader) throws ClassNotFoundException
    {
        Class<?> c = null;
        if (classLoader != null)
        {
            try
            {
                c = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException e)
            {
                //LOGGER.warn(e.getMessage(), e);
            }
        }
        if (c == null)
        {
            try
            {
                classLoader = Thread.currentThread().getContextClassLoader();
                c = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException e)
            {
                //LOGGER.warn(e.getMessage(), e);
            }
        }
        if (c == null)
        {
            c = Class.forName(className);
        }
        return c;
    }

    /**
     * 根据类和相对包名获取最终的包名。
     *
     * @param clazz
     * @param relative  通过“/”分开，如“../util”。“../”表示上一级，“./”表示当前
     * @param separator 最终的分隔符
     * @return
     */
    public static String getPackageWithRelative(Class<?> clazz, String relative, String separator)
    {
        return getPathWithRelative('.',clazz.getPackage().getName(),relative,separator);
    }

    /**
     * 根据类和相对包名获取最终的包名。
     *
     * @param pathSep   如“/”
     * @param path
     * @param relative  通过“/”分开，如“../util”。“../”表示上一级，“./”表示当前
     * @param separator 最终的分隔符
     * @return
     */
    public static String getPathWithRelative(char pathSep, String path, String relative, String separator)
    {
        String[] origins = StrUtil.split(path, pathSep + "");
        Stack<String> stack = new Stack<>();
        for (String s : origins)
        {
            stack.push(s);
        }
        String[] relatives = relative.split("/");
        for (int i = 0; i < relatives.length; i++)
        {
            String str = relatives[i];
            if ("..".equals(str))
            {
                if (stack.empty())
                {
                    throw new RuntimeException("no more upper path!");
                }
                stack.pop();
            } else if (!".".equals(str))
            {
                stack.push(str);
            }
        }
        if (stack.empty())
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int len = stack.size() - 1;
        for (int i = 0; i < len; i++)
        {
            builder.append(stack.get(i)).append(separator);
        }
        builder.append(stack.get(len));
        return builder.toString();
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, ClassLoader classLoader)
    {
        return getClassName(packageName, classLoader, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, ClassLoader classLoader, boolean childPackage)
    {
        try
        {
            return _getClassName(packageName, classLoader, childPackage);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取某包下所有类.
     * URLDecoder.decode(url.getFile(),ENCODING)：对中文的支持
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> _getClassName(String packageName, ClassLoader classLoader,
            boolean childPackage) throws UnsupportedEncodingException
    {
        List<String> fileNames = null;
        ClassLoader loader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace('.', '/');
        URL url = loader.getResource(packagePath);
        if (url != null)
        {
            String type = url.getProtocol();
            //LogUtil.printLnPos(url);
            if (type.equals("file"))
            {
                fileNames = getClassNameByFile(packageName, URLDecoder.decode(url.getFile(), ENCODING), childPackage);
            } else if (type.equals("jar"))
            {
                fileNames = getClassNameByJar(URLDecoder.decode(url.getFile(), ENCODING), childPackage);
            }
        } else
        {
            if (loader instanceof IClassLoader)
            {
                IClassLoader iClassLoader = (IClassLoader) loader;
                iClassLoader.seek();
                fileNames = iClassLoader.getClassNames(packageName, childPackage);
                iClassLoader.release();
            } else
            {
                fileNames = getClassNameByJars(getUrls(loader, packageName), packagePath, childPackage);
            }

        }
        return fileNames;
    }

    private static URL[] getUrls(ClassLoader classLoader, String packageName)
    {
        try
        {
            Enumeration<URL> enumeration = classLoader.getResources(packageName);
            ArrayList<URL> list = new ArrayList<URL>();
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
     * @param packageName
     * @param filePath     文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String packageName, String filePath,
            boolean childPackage)
    {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);

        File[] childFiles = file.listFiles();
        for (File childFile : childFiles)
        {
            if (childFile.isDirectory())
            {
                if (childPackage)
                {
                    myClassName.addAll(getClassNameByFile(
                            packageName + "." + childFile.getName(), childFile.getPath(), childPackage));
                }
            } else
            {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class"))
                {
                    int prefixLen = file.getPath().length() - packageName.length();
                    String className = childFilePath.substring(prefixLen, childFilePath.lastIndexOf('.'))
                            .replace(File.separatorChar, '.');
                    myClassName.add(className);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage)
    {
        List<String> myClassName = new ArrayList<String>();

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
                if (entryName.endsWith(".class"))
                {
                    if (entryName.charAt(0) == '/')
                    {
                        entryName = entryName.substring(1);
                    }
                    if (childPackage)
                    {
                        if (entryName.startsWith(packagePath))
                        {
                            entryName = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                            myClassName.add(entryName);
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
                            entryName = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                            myClassName.add(entryName);
                        }
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
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage)
    {
        List<String> myClassName = new ArrayList<String>();
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
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}
