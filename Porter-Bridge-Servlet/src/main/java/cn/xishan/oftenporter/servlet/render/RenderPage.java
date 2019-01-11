package cn.xishan.oftenporter.servlet.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/27.
 */
public class RenderPage
{
    private String page;
    private Map<String, Object> data;

    public RenderPage(String page)
    {
        this.page = page;
        this.data = new HashMap<>();
    }

    public String getPage()
    {
        return page;
    }

    public void setPage(String page)
    {
        this.page = page;
    }

    public RenderPage putData(String key, Object value)
    {
        data.put(key, value);
        return this;
    }

    public void clearData()
    {
        data.clear();
    }

    public Object getData(String key)
    {
        return data.get(key);
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public Set<Map.Entry<String, Object>> dataEntrySet()
    {
        return data.entrySet();
    }

}
