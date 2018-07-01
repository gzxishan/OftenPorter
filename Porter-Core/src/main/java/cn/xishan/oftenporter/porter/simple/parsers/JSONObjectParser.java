package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.WObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 把json格式的字符串转换为{@linkplain JSONObject}
 */
public class JSONObjectParser extends TypeParser
{

    @Override
    public ParseResult parse(WObject wObject, @NotNull String name, @NotNull Object value, @MayNull Object dealt)
    {
        return parse(this, value);
    }

    static ParseResult parse(ITypeParser typeParser, @NotNull Object value)
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
            result = ParserUtil.failed(typeParser, e.getMessage());
        }
        return result;
    }
}
