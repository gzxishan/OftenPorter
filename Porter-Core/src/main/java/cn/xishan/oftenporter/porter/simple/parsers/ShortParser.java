package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;


/**
 * Created by 宇宙之灵 on 2015/9/14.
 */
public class ShortParser extends TypeParser
{
    @Override
    public ParseResult parse(OftenObject oftenObject, @NotNull String name, @NotNull Object value,@MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Short)
            {
                v = value;
            } else
            {
                v = Short.parseShort(value.toString());
            }
            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
