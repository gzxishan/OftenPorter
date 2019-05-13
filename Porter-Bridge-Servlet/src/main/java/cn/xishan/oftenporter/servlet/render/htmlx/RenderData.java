package cn.xishan.oftenporter.servlet.render.htmlx;


import cn.xishan.oftenporter.servlet.render.RenderPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2019-05-13.
 */
class RenderData
{
    private HtmlxDoc htmlxDoc;
    private List<Object> rtObjects = new ArrayList<>(1);

    public RenderData(HtmlxDoc htmlxDoc)
    {
        this.htmlxDoc = htmlxDoc;
    }

    public void addReturnObject(Object object)
    {
        if (object == null)
        {
            return;
        }
        if (!(object instanceof RenderPage || object instanceof Map))
        {
            throw new RuntimeException("unknown return:" + object);
        }
        rtObjects.add(object);
    }

    public HtmlxDoc getHtmlxDoc()
    {
        return htmlxDoc;
    }

    public List<Object> getReturnObjects()
    {
        return rtObjects;
    }
}
