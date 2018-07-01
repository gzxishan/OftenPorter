package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.math.BigDecimal;

/**
 */
public class BigDecimalParser extends TypeParser
{
    @Override
    public ParseResult parse(WObject wObject, @NotNull String name, @NotNull Object value, @MayNull Object dealt)
    {
        ParseResult result;

        try
        {
            Object v;
            if (value instanceof BigDecimal)
            {
                v = value;
            } else
            {
                v = new BigDecimal(value.toString());
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
