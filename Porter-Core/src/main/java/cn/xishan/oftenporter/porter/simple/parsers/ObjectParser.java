package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.WObject;

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
    public ParseResult parse(WObject wObject, @NotNull String name, @NotNull Object value, @MayNull StringDealt stringDealt)
    {
        ParseResult parseResult;
        if (stringDealt != null)
        {
            parseResult = stringDealt.getValue(this, value);
        } else
        {
            parseResult = new ParseResult(value);
        }
        return parseResult;
    }


}
