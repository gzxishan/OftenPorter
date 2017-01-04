package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import com.alibaba.fastjson.JSONArray;

import java.util.Date;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class Article
{
    @PortInObj.Nece("title")
    private String title;
    @PortInObj.Nece("time")
    private long time;
    @PortInObj.UnNece("content")
    private String content;
    @PortInObj.UnNece("comments")
    private JSONArray comments;

    @Override
    public String toString()
    {
        return title + "," + new Date(time) + "," + content + "," + comments;
    }
}
