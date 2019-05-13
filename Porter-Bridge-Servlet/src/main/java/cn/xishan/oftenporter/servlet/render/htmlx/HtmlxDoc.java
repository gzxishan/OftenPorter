package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxDoc
{
    public enum PageType
    {
        Normal, OtherwisePage, OtherwiseHtml
    }

    public enum ResponseType
    {
        /**
         * 进行正常处理
         */
        Normal,
        /**
         * 执行后续servlet的过滤器
         */
        ServletDefault,
        /**
         * 中断后续操作，这种情况一般是开发者自己进行的输出响应。
         */
        Break,
        /**
         * 执行下一个@{@linkplain Htmlx},同时会保留当前的操作。
         */
        Next
    }

    private Object document;

    private String encoding;
    private String contentType;
    private int cacheSeconds;
    private PageType pageType;
    private ResponseType responseType = ResponseType.Normal;
    private boolean willCache = true;

    private HtmlxHandle htmlxHandle;
    private String path;
    private long lastModified;

    public HtmlxDoc(HtmlxHandle htmlxHandle, String path, Object document, PageType pageType, long lastModified)
    {
        this.htmlxHandle = htmlxHandle;
        this.path = path;
        this.document = document;
        this.pageType = pageType;
        this.lastModified = lastModified;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * 获取当前请求路径。
     *
     * @return
     */
    public String getPath()
    {
        return path;
    }

    /**
     * 获得当前请求路径文件简单名。
     *
     * @return
     */
    public String getPageName()
    {
        return OftenStrUtil.getNameFormPath(path);
    }

    /**
     * 见{@linkplain #getRelativeResource(String, String)},编码方式为{@linkplain Htmlx#otherwisePageEncoding()}。
     *
     * @return
     */
    public String getRelativeResource(String path) throws IOException
    {
        return getRelativeResource(path, htmlxHandle.getOtherwisePageEncoding());
    }

    /**
     * 获取相对于{@linkplain Htmlx#baseDir()}的文件内容。
     *
     * @return
     */
    public String getRelativeResource(String path, String encoding) throws IOException
    {
        ServletContext servletContext = htmlxHandle.getServletContext();
        String filepath = servletContext.getRealPath(PackageUtil.getPathWithRelative(htmlxHandle.getBaseDir(), path));
        if (OftenTool.isEmpty(filepath))
        {
            throw new FileNotFoundException("not found:" + path);
        }
        File file = new File(filepath);
        String str = FileTool.getString(file, encoding);
        return str;
    }

    public ResponseType getResponseType()
    {
        return responseType;
    }

    public void setResponseType(ResponseType responseType)
    {
        this.responseType = responseType;
    }

    public boolean willCache()
    {
        return willCache;
    }

    public void setWillCache(boolean willCache)
    {
        this.willCache = willCache;
    }


    public PageType getPageType()
    {
        return pageType;
    }

    /**
     * 设置页面标题。
     *
     * @param title
     */
    public void title(String title)
    {
        Document document = (Document) this.document;
        if (title != null)
        {
            document.title(title);
        }
    }

    public void setMetaDescription(String description)
    {
        this.setMeta("description", description);
    }

    public void setMetaKeywords(String keywords)
    {
        this.setMeta("keywords", keywords);
    }

    public void setMeta(String name, String content)
    {
        Document document = (Document) this.document;
        Element element = document.head().selectFirst("meta[name='" + name + "']");
        if (element == null)
        {
            element = new Element("meta");
            element.attr("name", name);
            document.head().appendChild(element);
        }
        element.attr("content", content);
    }

    public String getText(String selector)
    {
        Document document = (Document) this.document;
        Element element = document.selectFirst(selector);
        return element == null ? null : element.text();
    }

    /**
     * 设置元素文本内容。
     *
     * @param selector
     * @param text
     */
    public void setText(String selector, String text)
    {
        Document document = (Document) this.document;
        Elements elements = document.select(selector);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext())
        {
            it.next().text(text);
        }
    }

    /**
     * 得到页面的标题
     *
     * @return
     */
    public String title()
    {
        Document document = (Document) this.document;
        return document.title();
    }

    public Object getDocument()
    {
        return document;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * 设置找到的第一个元素的text。
     *
     * @param selector
     * @param text
     */
    public void text(String selector, String text)
    {
        Document document = (Document) this.document;
        Element element = document.selectFirst(selector);
        if (element != null)
        {
            element.text(text);
        }
    }

    public String text(String selector)
    {
        Document document = (Document) this.document;
        Element element = document.selectFirst(selector);
        if (element != null)
        {
            return element.text();
        } else
        {
            return null;
        }
    }

    public int getCacheSeconds()
    {
        return cacheSeconds;
    }

    public void setCacheSeconds(int cacheSeconds)
    {
        this.cacheSeconds = cacheSeconds;
    }
}
