package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;

/**
 * boolean类型
 */
public class BooleanParser extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value,@MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Boolean)
            {
                v = value;
            } else
            {
                v = Boolean.parseBoolean(value.toString());
            }
            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
