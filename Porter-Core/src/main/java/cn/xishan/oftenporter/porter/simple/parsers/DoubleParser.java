package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 */
public class DoubleParser extends TypeParser
{
    @Override
    public ParseResult parse(OftenObject oftenObject, @NotNull String name, @NotNull Object value, @MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Double)
            {
                v = value;
            } else
            {
                v = Double.parseDouble(value.toString());
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
