package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动判断转换格式，支持的格式：(其中日期连接字符“-”可以是“/”)
 * <ol>
 * <li>时间毫秒数</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSS[ZzX]</li>
 * <li>yyyy-MM-dd</li>
 * <li>yyyy-MM</li>
 * <li>yyyy</li>
 * <li>MM-dd</li>
 * <li>yyyy-MM-dd HH:mm:ss</li>
 * <li>yyyy-MM-dd HH:mm</li>
 * <li>yyyy-MM-dd HH</li>
 * <li>MM-dd HH:mm:ss</li>
 * <li>MM-dd HH:mm</li>
 * <li>MM-dd HH</li>
 * </ol>
 * Created by chenyg on 2017-04-19.
 */
public class DateParser extends TypeParser
{
    private static final Pattern[] PATTERNS;
    private static final String[] FORMATS;
    private static final boolean[] SET_YEARS;
    private static final Pattern PATTERN_MILLIS = Pattern.compile("^[0-9]+$");

    static
    {
        PATTERNS = new Pattern[]{
                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}$"),

                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2} [0-9]{2}:[0-9]{1,2}:[0-9]{1,2}$"),
                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2} [0-9]{2}:[0-9]{1,2}$"),
                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2} [0-9]{2}$"),

                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}T[0-9]{2}:[0-9]{1,2}:[0-9]{1,2}\\.[0-9]{1,3}Z$"),
                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}T[0-9]{2}:[0-9]{1,2}:[0-9]{1,2}\\.[0-9]{1,3}z$"),
                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}T[0-9]{2}:[0-9]{1,2}:[0-9]{1,2}\\.[0-9]{1,3}X$"),

                Pattern.compile("^[0-9]{4}[-/][0-9]{1,2}$"),
                Pattern.compile("^[0-9]{4}$"),
                Pattern.compile("^[0-9]{1,2}[-/][0-9]{1,2}$"),


                Pattern.compile("^[0-9]{1,2}[-/][0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$"),
                Pattern.compile("^[0-9]{1,2}[-/][0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}$"),
                Pattern.compile("^[0-9]{1,2}[-/][0-9]{1,2} [0-9]{1,2}$")
        };
        FORMATS = new String[]{
                "yyyy-MM-dd",

                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd HH",

                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'X'",

                "yyyy-MM",
                "yyyy",
                "MM-dd",

                "MM-dd HH:mm:ss",
                "MM-dd HH:mm",
                "MM-dd HH",
        };
        SET_YEARS = new boolean[]{
                false,
                false, false, false,
                false, false, false,
                false, false, true,
                true, true, true
        };
    }


    @Override
    public ParseResult parse(OftenObject oftenObject, @NotNull String name, @NotNull Object value,
            @MayNull Object dealt)
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
            } else if (value instanceof CharSequence && PATTERN_MILLIS.matcher(String.valueOf(value)).find())
            {
                v = new Date(Long.parseLong(String.valueOf(value)));
            } else
            {
                String theStr = String.valueOf(value).trim()
                        .replace("\"", "")
                        .replaceAll("[\\s]{3,}", "")
                        .replaceAll("[\\s]{2}", " ");

                String format = null;
                boolean setYear = false;
                for (int i = 0; i < PATTERNS.length; i++)
                {
                    Matcher matcher = PATTERNS[i].matcher(theStr);
                    if (matcher.find())
                    {
                        format = FORMATS[i];
                        setYear = SET_YEARS[i];
                        break;
                    }
                }
                if (format == null)
                {
                    return ParseResult.failed("illegal date:" + value);
                } else
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    if (format.endsWith("'Z'"))
                    {
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    Date date = simpleDateFormat.parse(theStr);
                    if (setYear)
                    {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        calendar.setTime(date);
                        calendar.set(Calendar.YEAR, year);
                        date = calendar.getTime();
                    }
                    v = date;
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
