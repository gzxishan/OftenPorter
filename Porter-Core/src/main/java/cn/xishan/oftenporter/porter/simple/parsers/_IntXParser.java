package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 *
 */
class _IntXParser extends TypeParser
{
    private int radix;

    public _IntXParser(int radix)
    {
        this.radix = radix;
    }

    @Override
    public ParseResult parse(OftenObject oftenObject, @NotNull String name, @NotNull Object value,@MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Integer)
            {
                v = value;
            } else
            {
                int x = (int) Long.parseLong(value.toString(), radix);
                v = x;
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
