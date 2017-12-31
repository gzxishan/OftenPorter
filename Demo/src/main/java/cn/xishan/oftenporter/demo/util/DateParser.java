package cn.xishan.oftenporter.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;

public class DateParser implements ITypeParser {


    public DateParser() {

    }

    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption) {
        ParseResult result;
        try {
            Object v;
            if (value instanceof Date) {
                v = value;
            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = format.parse(String.valueOf(value));
                v = date;
            }
            result = new ParseResult(v);
        } catch (Exception e) {
            result = ParseResult
                    .failed(getClass().getSimpleName() + ":" + e.getMessage());
        }

        return result;
    }

    @Override
    public <T> T dealtFor(ITypeParserOption parserOption)
    {
        return null;
    }

    @Override
    public String id() {
        return getClass().getName();
    }

}
