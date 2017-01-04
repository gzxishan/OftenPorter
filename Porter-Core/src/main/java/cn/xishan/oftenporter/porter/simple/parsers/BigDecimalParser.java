package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import java.math.BigDecimal;

/**
 */
public class BigDecimalParser extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value)
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
            result = ParserUtil.failed(this,e.getMessage());;
        }
        return result;
    }
}
