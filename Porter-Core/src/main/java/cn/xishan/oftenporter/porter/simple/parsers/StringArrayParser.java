package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

/**
 * 把json数组转换成字符串数组。
 * Created by chenyg on 2017-06-06.
 */
public class StringArrayParser  extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            JSONArray v;
            if (value instanceof JSONArray)
            {
                v = (JSONArray) value;
            } else
            {
                JSONArray array = JSON.parseArray(value.toString());
                v = array;
            }
            String[] strs = new String[v.size()];
            for (int i = 0; i < strs.length; i++)
            {
                strs[i]=v.getString(i);
            }
            result = new ParseResult(strs);
        } catch (JSONException e)
        {
            result = ParserUtil.failed(this,e.getMessage());;
        }
        return result;
    }
}
