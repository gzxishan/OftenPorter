package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;


/**
 * Created by 宇宙之灵 on 2015/9/14.
 */
public class StringParser extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value)
    {
        return new ParseResult(String.valueOf(value));
    }
}
