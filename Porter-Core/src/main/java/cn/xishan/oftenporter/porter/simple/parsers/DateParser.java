package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenyg on 2017-04-19.
 */
@Deprecated
public class DateParser extends TypeParser {
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ParseResult parse(String name, Object value,@MayNull Object dealt) {
        ParseResult result;
        try {
            Object v;
            if (value instanceof Date) {
                v = value;
            } else if (value instanceof Long) {
                v = new Date((long) value);
            } else {
                synchronized (SIMPLE_DATE_FORMAT) {
                    v = SIMPLE_DATE_FORMAT.parse(String.valueOf(value));
                }
            }

            result = new ParseResult(v);
        } catch (Exception e) {
            result = ParserUtil.failed(this, e.getMessage());
        }
        return result;
    }
}
