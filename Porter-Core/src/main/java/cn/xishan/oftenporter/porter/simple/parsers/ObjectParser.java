package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

/**
 * 不做任何类型转换操作。
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class ObjectParser extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value)
    {
        return new ParseResult(value);
    }
}
