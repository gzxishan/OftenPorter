package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;


/**
 */
public class FloatParser extends TypeParser
{


    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Float)
            {
                v = value;
            } else
            {
                v = Float.parseFloat(value.toString());
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
