package cn.xishan.oftenporter.porter.simple.parsers;


import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;

/**
 * 把json数组转换为ArrayList&#60;String&#62;.
 */
public class ArrayListArrParser extends JSONArrayParser
{
    @Override
    public ParseResult parse(@NotNull String name, @NotNull Object value,@MayNull Object dealt)
    {
        ParseResult result = super.parse(name, value,dealt);
        try
        {
            if (result.isLegal())
            {
                JSONArray array = (JSONArray) result.getValue();
                ArrayList<String> list = new ArrayList<String>(array.size());
                for (int i = 0; i < array.size(); i++)
                {
                    list.add(array.getString(i));
                }
                result.setValue(list);
            }
        } catch (JSONException e)
        {
            result.setFailedDesc(e.getMessage());
        }
        return result;
    }

}
