package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import com.alibaba.fastjson.JSONArray;

import java.util.Date;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class Article {
    @Nece("title")
    private String title;
    @Nece("time")
    private long time;
    @Unece("content")
    private String content;
    @Unece("comments")
    private JSONArray comments;

    @Unece(varName = "requestData", requestData = true)
    public String requestData;

    @Override
    public String toString() {
        return title + "," + new Date(time) + "," + content + "," + comments;
    }
}
