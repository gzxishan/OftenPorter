package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class ParserUtil
{

    static ITypeParser.ParseResult failed(ITypeParser typeParser, String failedDesc)
    {
        return ITypeParser.ParseResult.failed(typeParser.getClass().getSimpleName() + ":" + failedDesc);
    }

    /**
     * 根据类型得到转换类。
     *
     * @param type 当前的类型
     * @return 转类型
     * @throws ClassNotFoundException 未找到匹配的。
     */
    public static Class<? extends ITypeParser> getTypeParser(Class<?> type) throws ClassNotFoundException
    {
        Class<? extends ITypeParser> clazz = null;
        if (type.isPrimitive())
        {
            if (type == Integer.TYPE)
            {
                clazz = IntParser.class;
            } else if (type == Boolean.TYPE)
            {
                clazz = BooleanParser.class;
            } else if (type == Character.TYPE)
            {
                clazz = CharParser.class;
            } else if (type == Short.TYPE)
            {
                clazz = ShortParser.class;
            } else if (type == Byte.TYPE)
            {
                clazz = ByteParser.class;
            } else if (type == Long.TYPE)
            {
                clazz = LongParser.class;
            } else if (type == Float.TYPE)
            {
                clazz = FloatParser.class;
            } else if (type == Double.TYPE)
            {
                clazz = DoubleParser.class;
            }
        } else
        {
            if (type == ArrayList.class)
            {
                clazz = ArrayListArrParser.class;
            } else if (type == BigDecimal.class)
            {
                clazz = BigDecimalParser.class;
            } else if (type == JSONArray.class)
            {
                clazz = JSONArrayParser.class;
            } else if (type == JSONObject.class)
            {
                clazz = JSONObjectParser.class;
            } else if (type == String.class)
            {
                clazz = StringParser.class;
            } else if (type == Map.class)
            {
                clazz = JSON2MapParser.class;
            } else if (type == File.class)
            {
                clazz = FileParser.class;
            }else if(type== Date.class){
                clazz = DateTime2MinitueParser.class;
            }
            //////////
            else if (type == Character.class)
            {
                clazz = CharParser.class;
            } else if (type == Integer.class)
            {
                clazz = IntParser.class;
            } else if (type == Boolean.class)
            {
                clazz = BooleanParser.class;
            } else if (type == Short.class)
            {
                clazz = ShortParser.class;
            } else if (type == Byte.class)
            {
                clazz = ByteParser.class;
            } else if (type == Long.class)
            {
                clazz = LongParser.class;
            } else if (type == Float.class)
            {
                clazz = FloatParser.class;
            } else if (type == Double.class)
            {
                clazz = DoubleParser.class;
            }
        }
        if (clazz == null)
        {
            throw new ClassNotFoundException(TypeParser.class.getName() + " for " + type + " is not found!");
        } else
        {
            return clazz;
        }
    }
}
