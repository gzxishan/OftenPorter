package cn.xishan.oftenporter.servlet.render.jsp;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.ConcurrentKeyLock;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.servlet.render.RenderPage;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/27.
 */
class JspHandle extends AspectOperationOfPortIn.HandleAdapter<Jsp>
{

    static class Cache
    {
        long lastModified;
        long lastAdd = System.currentTimeMillis();
    }

    @AutoSet(nullAble = true)
    JspOption jspOption;
    @AutoSet
    ServletContext servletContext;

    @AutoSet(range = AutoSet.Range.New)
    ConcurrentKeyLock<String> keyLock;

    @AutoSet
    Logger LOGGER;
    Jsp jsp;

    String pageEncoding;

    private String stdJsp;

    private Map<String, Cache> newFiles = new ConcurrentHashMap<>();
    private String prefix, suffix;
    private boolean useStdTag = false;

    @AutoSet.SetOk
    public void setOk()
    {
        if (jspOption == null)
        {
            jspOption = JspOption.DEFAULT;
        }


        prefix = jsp.prefix().equals("") ? jspOption.prefix : jsp.prefix();
        suffix = jsp.suffix().equals("") ? jspOption.suffix : jsp.suffix();
        useStdTag = (jsp.useStdTag() == -1 && jspOption.useStdTag || jsp.useStdTag() != -1 && jsp
                .useStdTag() != 0) && suffix
                .equals(".jsp");

        if (useStdTag)
        {
            stdJsp = FileTool.getString(
                    PortUtil.getRealClass(this)
                            .getResourceAsStream("/" + PackageUtil
                                    .getPackageWithRelative(PortUtil.getRealClass(this), "std.jsp", '/')));

            pageEncoding = jsp.pageEncoding().equals("") ? jspOption.pageEncoding : jsp.pageEncoding();
            if (pageEncoding.equals(""))
            {
                pageEncoding = "utf-8";
            }

            stdJsp = stdJsp.replace("#{pageEncoding}", pageEncoding);

            if (jsp.enableEL() == -1 && jspOption.enableEL || jsp.enableEL() != -1 && jsp
                    .enableEL() != 0)
            {
                //启用EL表达式
                stdJsp += "<%@ page isELIgnored=\"false\"%>\n";
            }

            if (OftenTool.notNullAndEmpty(jspOption.appendJspContent))
            {
                stdJsp += jspOption.appendJspContent;
            }
            if (OftenTool.notNullAndEmpty(jsp.appendJspContent()))
            {
                stdJsp += jsp.appendJspContent();
            }
        }


    }


    @Override
    public boolean init(Jsp current, IConfigData configData, PorterOfFun porterOfFun)
    {
        this.jsp = current;

        return true;
    }


    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        Object obj = porterOfFun.invokeByHandleArgs(oftenObject, lastReturn);

        String _page;
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();


        if (obj instanceof RenderPage)
        {
            RenderPage renderPage = (RenderPage) obj;
            _page = renderPage.getPage();
            for (Map.Entry<String, Object> entry : renderPage.dataEntrySet())
            {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        } else if (obj instanceof String)
        {
            _page = (String) obj;
        } else
        {
            throw new OftenCallException("unknown return type:" + obj);
        }


        String path = prefix + _page + suffix;
        if (useStdTag)
        {
            int index = path.lastIndexOf("/");
            String newPath = path.substring(0, index) + "/.jsp-handle" + path.substring(index);
            String realPathOfNewFile = servletContext.getRealPath(newPath);
            if (realPathOfNewFile == null)
            {
                LOGGER.warn("realPath is null for:" + newPath);
            } else
            {
                try
                {
                    keyLock.lock(realPathOfNewFile);
                    File file = new File(realPathOfNewFile);
                    File originFile = new File(servletContext.getRealPath(path));
                    if (!file.exists())
                    {
                        boolean rs = file.getParentFile().mkdirs();
                        if (!rs)
                        {
                            throw new IOException("mkdirs failed:" + file.getAbsolutePath());
                        }
                    }
                    Cache cache;
                    if ((cache = newFiles.get(realPathOfNewFile)) == null || (System
                            .currentTimeMillis() - cache.lastAdd > 5000 && originFile
                            .lastModified() != cache.lastModified))
                    {
                        if (cache == null)
                        {
                            cache = new Cache();
                            newFiles.put(realPathOfNewFile, cache);
                        }
                        cache.lastModified = originFile.lastModified();
                        cache.lastAdd = System.currentTimeMillis();

                        String content = FileTool
                                .getString(originFile, 1024, pageEncoding);
                        FileTool.write2File(stdJsp + content, pageEncoding, file, true);
                    }
                    path = newPath;
                } catch (Exception e)
                {
                    LOGGER.warn("create jsp file error:{}", realPathOfNewFile);
                    LOGGER.warn(e.getMessage(), e);
                } finally
                {
                    keyLock.unlock(realPathOfNewFile);
                }

            }
        }


        request.getRequestDispatcher(path)
                .forward(request, oftenObject.getRequest().getOriginalResponse());

        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }
}
