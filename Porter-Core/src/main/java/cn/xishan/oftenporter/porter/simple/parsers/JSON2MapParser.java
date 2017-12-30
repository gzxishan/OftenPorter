package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 把json字符串或对象转换为Map&#60;String,Object&#62;
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/10.
 */
public class JSON2MapParser extends JSONObjectParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value, @NotNull ITypeParserOption parserOption)
    {
        ParseResult result = super.parse(name, value,parserOption);
        if (result.isLegal())
        {
            JSONObject jsonObject = (JSONObject) result.getValue();
            Map<String, Object> map = jsonObject;
            result.setValue(map);
        }
        return result;
    }
}
