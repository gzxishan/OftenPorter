package cn.xishan.oftenporter.porter.core.util;

import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class OftenStrUtilTest
{
    @Test
    public void testSplit()
    {
        String src = "name=1&&age=2&";
        String[] strs = OftenStrUtil.split(src, "&");
        assertArrayEquals("wrong", new String[]{"name=1", "age=2"}, strs);
        assertEquals(5, OftenStrUtil.split("/*/*/*/*", "/*", true).length);
        assertEquals(1, OftenStrUtil.split("", "/*", true).length);
        assertEquals(2, OftenStrUtil.split("/*", "/*", true).length);

    }

    @Test
    public void testGetSuffix()
    {
        assertEquals("", OftenStrUtil.getSuffix("no-suffix", '.'));
        assertEquals("java", OftenStrUtil.getSuffix("hello.java", '.'));
        assertEquals("", OftenStrUtil.getSuffix("hello.", '.'));
        assertEquals("java", OftenStrUtil.getSuffix("hello-java", '-'));
    }

    @Test
    public void testFromEncoding() throws Exception
    {
        int n = new Random().nextInt(10) + 10;
        String[][] strings = new String[n][2];

        StringBuilder stringBuilder = new StringBuilder();
        String encoding = "utf-8";
        for (int i = 0; i < strings.length; i++)
        {
            String name = OftenKeyUtil.random48Key();
            String value = OftenKeyUtil.random48Key();
            strings[i][0] = name;
            strings[i][1] = value;
            stringBuilder.append(name).append("=").append(value).append("&");
        }
        Map<String, String> params = OftenStrUtil.fromEncoding(stringBuilder.toString(), encoding);
        int k = 0;
        for (String name : params.keySet())
        {
            assertEquals(strings[k++][0], name);
        }

        k = 0;
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            assertEquals(strings[k++][0], entry.getKey());
        }

        k = 0;
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext())
        {
            assertEquals(strings[k++][0], it.next());
        }

        //重新put,验证顺序为最初put的顺序
        for (int i = 0; i < strings.length; i++)
        {
            params.put(strings[i][0], strings[i][1]);
        }
        k = 0;
        for (String name : params.keySet())
        {
            assertEquals(strings[k++][0], name);
        }

        k = 0;
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            assertEquals(strings[k++][0], entry.getKey());
        }

        k = 0;
        it = params.keySet().iterator();
        while (it.hasNext())
        {
            assertEquals(strings[k++][0], it.next());
        }
    }
}
