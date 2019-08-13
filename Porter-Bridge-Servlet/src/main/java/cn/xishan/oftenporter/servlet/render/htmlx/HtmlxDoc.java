package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
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
         * 进行正常处理，如果还有后续的@{@linkplain Htmlx}匹配、则会继续进行匹配。
         */
        Normal,
        /**
         * 执行后续servlet的过滤器
         */
        ServletDefault,
        /**
         * 中断后续操作，这种情况一般是开发者自己进行了输出或状态响应。
         */
        Break,
        /**
         * 执行下一个@{@linkplain Htmlx}，不会设置当前的缓存信息等，如果还有后续的@{@linkplain Htmlx}匹配、则会继续进行匹配。
         */
        Next
    }

    private Object _document;
    private IDocGetter docGetter;

    private String encoding;
    private String contentType;
    private int cacheSeconds;
    private PageType pageType;
    private ResponseType responseType = ResponseType.Normal;
    private boolean willCache = true;

    private HtmlxHandle htmlxHandle;
    private String path;
    private long lastModified;

    private String toSetTitle;
    private String toSetDescription;
    private String toSetKeywords;


    public HtmlxDoc(HtmlxHandle htmlxHandle, String path, PageType pageType, ResponseType responseType,
            long lastModified, IDocGetter docGetter)
    {
        this.htmlxHandle = htmlxHandle;
        this.path = path;
        this.docGetter = docGetter;
        this.pageType = pageType;
        this.responseType = responseType;
        this.lastModified = lastModified;
    }

    //只有在需要document时，才进行解析；例如在开发者进行自定义响应的情况下、无需进行解析。
    private synchronized Document getDoc()
    {
        if (this._document == null)
        {
            try
            {
                this._document = docGetter.getDoc();
            } catch (Exception e)
            {
                throw new OftenCallException(e);
            }
        }
        Document document = (Document) _document;
        return document;
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
        this.toSetTitle = title;
    }

    /**
     * 得到页面的标题
     *
     * @return
     */
    public String title()
    {
        return toSetTitle;
    }

    public void setMetaDescription(String description)
    {
        this.toSetDescription = description;
    }

    public String getMetaDescription()
    {
        return toSetDescription;
    }

    public void setMetaKeywords(String keywords)
    {
        this.toSetKeywords = toSetDescription;
    }

    public String getMetaKeywords()
    {
        return toSetKeywords;
    }

    public void setMeta(String name, String content)
    {
        Document document = getDoc();
        Element element = document.head().selectFirst("meta[name='" + name + "']");
        if (element == null)
        {
            element = new Element("meta");
            element.attr("name", name);
            document.head().appendChild(element);
        }
        element.attr("content", content);
    }

    /**
     * 添加style到head中。
     *
     * @param styleContent style内容
     */
    public void addStyleContent(String styleContent)
    {
        Document document = getDoc();
        Element head = document.head();
        Element cssEle = new Element("style");
        cssEle.html(styleContent);
        cssEle.attr("type", "text/css");
        head.appendChild(cssEle);
    }

    public void addStyleWithPath(String relativePath) throws IOException
    {
        addStyleWithPath(relativePath, htmlxHandle.getOtherwisePageEncoding());
    }

    /**
     * 从文件中加载style内容，添加到head中。
     *
     * @param relativePath
     * @param encoding
     * @throws IOException
     */
    public void addStyleWithPath(String relativePath, String encoding) throws IOException
    {
        addStyleContent(getRelativeResource(relativePath, encoding));
    }


    public void addScriptWithPath(String relativePath) throws IOException
    {
        addScriptContent(getRelativeResource(relativePath, htmlxHandle.getOtherwisePageEncoding()));
    }

    /**
     * 从文件中加载js脚本内容，添加到head中。
     *
     * @param relativePath
     * @param encoding
     * @throws IOException
     */
    public void addScriptWithPath(String relativePath, String encoding) throws IOException
    {
        addScriptContent(getRelativeResource(relativePath, encoding));
    }

    /**
     * 添加script到head中。
     *
     * @param scriptContent JavaScript脚本内容
     */
    public void addScriptContent(String scriptContent)
    {
        Document document = getDoc();
        Element head = document.head();
        head.append("\n<script type='text/javascript'>\n" + scriptContent + "\n</script>\n");
    }

    public String getText(String selector)
    {
        Document document = getDoc();
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
        Document document = getDoc();
        Elements elements = document.select(selector);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext())
        {
            it.next().text(text);
        }
    }


    public Object getDocument()
    {
        return getDoc();
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
        Document document = getDoc();
        Element element = document.selectFirst(selector);
        if (element != null)
        {
            element.text(text);
        }
    }

    public String text(String selector)
    {
        Document document = getDoc();
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

    public void doSettings()
    {
        Document document = getDoc();
        if (OftenTool.notEmpty(toSetTitle))
        {
            document.title(toSetTitle);
        }

        if (OftenTool.notEmpty(toSetDescription))
        {
            setMeta("description", toSetDescription);
        }

        if (OftenTool.notEmpty(toSetKeywords))
        {
            setMeta("keywords", toSetKeywords);
        }
    }
}
