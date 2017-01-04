package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 把json格式的字符串转换为{@linkplain JSONObject}
 */
public class JSONObjectParser extends TypeParser
{

    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof JSONObject)
            {
                v = value;
            } else
            {
                JSONObject jsonObject = JSON.parseObject(value.toString());
                v = jsonObject;
            }
            result = new ParseResult(v);
        } catch (JSONException e)
        {
            result = ParserUtil.failed(this,e.getMessage());;
        }
        return result;
    }
}
