package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Created by https://github.com/CLovinr on 2018-08-10.
 */
public class DateParserTest
{
    @Test
    public void testDate()
    {
        DateParser dateParser = new DateParser();

        String t = "2020-05-28T18:30:38.000Z";
        ITypeParser.ParseResult result = dateParser.parse(null, null, t, null);
        printResult(result);

        t = "2018-5-3";
        result = dateParser.parse(null, null, t, null);
        printResult(result);

        t = "2018-5";
        result = dateParser.parse(null, null, t, null);
        printResult(result);

        t = "2018";
        result = dateParser.parse(null, null, t, null);
        printResult(result);

        t = "5-5";
        result = dateParser.parse(null, null, t, null);
        printResult(result);

        ////////////
        t = "1-5 12:55:00";
        result = dateParser.parse(null, null, t, null);
        printResult(result);
        t = "2-6 12:20";
        result = dateParser.parse(null, null, t, null);
        printResult(result);

        t = "2-6 19";
        result = dateParser.parse(null, null, t, null);
        printResult(result);
    }

    private void printResult(ITypeParser.ParseResult result)
    {
        if (result.isLegal())
        {
            Date date = (Date) result.getValue();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LogUtil.printPos(simpleDateFormat.format(date));
        } else
        {
            LogUtil.printPos(result);
        }
    }
}
