package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import java.io.File;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/24.
 */
public class FileParser extends TypeParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof File)
            {
                v = value;
            } else
            {
                v = new File(String.valueOf(value));
            }

            result = new ParseResult(v);
        } catch (NumberFormatException e)
        {
            result = ParserUtil.failed(this,e.getMessage());
        }
        return result;
    }
}
