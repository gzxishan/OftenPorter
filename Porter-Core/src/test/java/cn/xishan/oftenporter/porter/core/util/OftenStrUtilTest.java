package cn.xishan.oftenporter.porter.core.util;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class OftenStrUtilTest
{
    @Test
    public void testSplit(){
        String src = "name=1&age=2&";
        String[] strs = OftenStrUtil.split(src,"&");
        assertArrayEquals("wrong",new String[]{"name=1","age=2"},strs);
    }

    @Test
    public void testGetSuffix(){
        assertEquals("", OftenStrUtil.getSuffix("no-suffix",'.'));
        assertEquals("java", OftenStrUtil.getSuffix("hello.java",'.'));
        assertEquals("", OftenStrUtil.getSuffix("hello.",'.'));
        assertEquals("java", OftenStrUtil.getSuffix("hello-java",'-'));
    }
}
