package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * 把json数组转换成字符串数组。
 * Created by chenyg on 2017-06-06.
 */
public class StringSetParser extends TypeParser
{
    @Override
    public ParseResult parse(OftenObject oftenObject, @NotNull String name, @NotNull Object value,
            @MayNull Object dealt)
    {
        return parseArray(this, value);
    }

    static ParseResult parseArray(ITypeParser typeParser, @NotNull Object value)
    {
        ParseResult result;
        try
        {
            if (value instanceof Set)
            {
                result = new ParseResult(value);
            } else
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
                Set<String> strs = new HashSet<>(v.size());
                for (int i = 0; i < v.size(); i++)
                {
                    strs.add(v.getString(i));
                }
                result = new ParseResult(strs);
            }

        } catch (JSONException e)
        {
            result = ParserUtil.failed(typeParser, e.getMessage());
        }
        return result;
    }
}
