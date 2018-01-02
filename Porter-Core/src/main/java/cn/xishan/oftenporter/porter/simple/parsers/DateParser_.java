package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Created by https://github.com/CLovinr on 2017/5/14.
 */
abstract class DateParser_ extends TypeParser
{

    private SimpleDateFormat format;

    public DateParser_(SimpleDateFormat format)
    {
        this.format = format;
    }

    public DateParser_(String format)
    {
        this(new SimpleDateFormat(format));
    }

    @Override
    public ParseResult parse(String name, Object value,@MayNull Object dealt)
    {
        ParseResult result;
        try
        {
            Object v;
            if (value instanceof Date)
            {
                v = value;
            } else if (value instanceof Long)
            {
                v = new Date((long) value);
            } else
            {
                synchronized (format){
                    v = format.parse(String.valueOf(value));
                }
            }

            result = new ParseResult(v);
        } catch (Exception e)
        {
            result = ParserUtil.failed(this, e.getMessage());
        }
        return result;
    }
}
