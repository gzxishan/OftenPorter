package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.util.WPTool;

/**
 * 不做任何类型转换操作,若加变量参数，则与{@linkplain StringParser}相同。
 *
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class ObjectParser extends StringParser
{

    public static final String ID = ObjectParser.class.getName();

    @Override
    public String id()
    {
        return ID;
    }

    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption)
    {
        if (WPTool.notNullAndEmpty(parserOption.getNameConfig()))
        {
            value = this.dealtFor(parserOption).getValue(String.valueOf(value));
        }
        return new ParseResult(value);
    }


}
