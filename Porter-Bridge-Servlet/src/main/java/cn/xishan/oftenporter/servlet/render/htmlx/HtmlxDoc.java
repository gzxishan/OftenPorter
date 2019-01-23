package cn.xishan.oftenporter.servlet.render.htmlx;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
public class HtmlxDoc
{
    private Object document;

    private String encoding;
    private String contentType;
    private int cacheSeconds;

    public HtmlxDoc(Object document)
    {
        this.document = document;
    }

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

    public int getCacheSeconds()
    {
        return cacheSeconds;
    }

    public void setCacheSeconds(int cacheSeconds)
    {
        this.cacheSeconds = cacheSeconds;
    }
}
