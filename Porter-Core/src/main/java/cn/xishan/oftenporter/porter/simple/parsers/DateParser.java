package cn.xishan.oftenporter.porter.simple.parsers;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenyg on 2017-04-19.
 */
public class DateParser extends TypeParser
{
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

    @Override
    public ParseResult parse(String name, Object value)
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
                v = SIMPLE_DATE_FORMAT.parse(String.valueOf(value));
            }

            result = new ParseResult(v);
        } catch (Exception e)
        {
            result = ParserUtil.failed(this, e.getMessage());
        }
        return result;
    }
}
