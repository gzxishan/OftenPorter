package cn.xishan.oftenporter.porter.simple.parsers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class ParserUtilTest
{
    @Test
    public void testGetTypeParser() throws ClassNotFoundException
    {
        assertEquals(BooleanParser.class, ParserUtil.getTypeParser(Boolean.class));
        assertEquals(CharParser.class, ParserUtil.getTypeParser(Character.class));
        assertEquals(ByteParser.class, ParserUtil.getTypeParser(Byte.class));
        assertEquals(ShortParser.class, ParserUtil.getTypeParser(Short.class));
        assertEquals(IntParser.class, ParserUtil.getTypeParser(Integer.class));
        assertEquals(LongParser.class, ParserUtil.getTypeParser(Long.class));
        assertEquals(FloatParser.class, ParserUtil.getTypeParser(Float.class));
        assertEquals(DoubleParser.class, ParserUtil.getTypeParser(Double.class));

        assertEquals(BooleanParser.class, ParserUtil.getTypeParser(boolean.class));
        assertEquals(CharParser.class, ParserUtil.getTypeParser(char.class));
        assertEquals(ByteParser.class, ParserUtil.getTypeParser(byte.class));
        assertEquals(ShortParser.class, ParserUtil.getTypeParser(short.class));
        assertEquals(IntParser.class, ParserUtil.getTypeParser(int.class));
        assertEquals(LongParser.class, ParserUtil.getTypeParser(long.class));
        assertEquals(FloatParser.class, ParserUtil.getTypeParser(float.class));
        assertEquals(DoubleParser.class, ParserUtil.getTypeParser(double.class));
        System.out.println(int.class);

        assertEquals(FileParser.class, ParserUtil.getTypeParser(File.class));
        assertEquals(ArrayListArrParser.class, ParserUtil.getTypeParser(ArrayList.class));
        assertEquals(BigDecimalParser.class, ParserUtil.getTypeParser(BigDecimal.class));
        assertEquals(JSONArrayParser.class, ParserUtil.getTypeParser(JSONArray.class));
        assertEquals(JSONObjectParser.class, ParserUtil.getTypeParser(JSONObject.class));
        assertEquals(StringParser.class, ParserUtil.getTypeParser(String.class));
        assertEquals(JSON2MapParser.class, ParserUtil.getTypeParser(Map.class));

        Class<?> type1 = int.class;
        Class<?> type2 = Integer.class;
        assertNotSame(type1, type2);

        int n = 0;
        Object v = n;
        assertFalse(v.getClass().isPrimitive());
        assertEquals(type2, v.getClass());
        assertNotSame(type1, v.getClass());
        v = Integer.valueOf(1);
        assertEquals(type2, v.getClass());
        assertNotSame(type1, v.getClass());

        v = true;
        assertTrue(v instanceof Boolean);
        v = '1';
        assertTrue(v instanceof Character);
        v = (byte) 1;
        assertTrue(v instanceof Byte);
        v = (short) 1;
        assertTrue(v instanceof Short);
        v = (int) 1;
        assertTrue(v instanceof Integer);
        v = 1L;
        assertTrue(v instanceof Long);
        v = 1.0f;
        assertTrue(v instanceof Float);
        v = 1.0d;
        assertTrue(v instanceof Double);

    }
}
