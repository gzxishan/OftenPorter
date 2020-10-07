package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.util.OftenTool;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;

/**
 * @author Created by https://github.com/CLovinr on 2020/10/6.
 */
public class ServletUtils
{
    public static File getResourcesRootDir(ServletContext servletContext)
    {
        File file = null;
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null)
        {
            String f = url.getFile();
            if (OftenTool.notEmpty(f))
            {
                String suffix = "/target/classes/";
                if (f.endsWith(suffix))
                {
                    f = f.substring(0, f.length() - suffix.length())
                            .replace(File.separatorChar, '/') + "/src/main/resources/";
                }
                file = new File(f);
            }
        }

        if (file == null || !file.exists())
        {
            file = new File("src/main/resources/");//idea社区版
            if (!file.exists())
            {
                file = new File(servletContext.getRealPath("/WEB-INF/classes/"));
            }

            if (!file.exists())
            {
                file = null;
            }
        }

        return file;
    }

    /**
     * 路径分隔符为‘/’。
     *
     * @param servletContext
     * @return
     */
    public static String getResourcesRootDirPath(ServletContext servletContext)
    {
        File file = getResourcesRootDir(servletContext);
        return file == null ? null : file.getAbsolutePath().replace(File.separatorChar, '/');
    }
}
