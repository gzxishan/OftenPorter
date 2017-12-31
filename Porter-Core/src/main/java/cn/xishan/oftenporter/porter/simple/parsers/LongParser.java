package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;


/**
 */
public class LongParser extends TypeParser
{

    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Long)
            {
                v = value;
            }else{
                v = Long.parseLong(value.toString());
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
