package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public class DefaultDecoderTest
{
    @Test
    public void testDecode() throws UnsupportedEncodingException
    {
        DefaultUrlDecoder defaultDecoder = new DefaultUrlDecoder("utf-8");

        UrlDecoder.Result result = defaultDecoder
                .decode("/C1/Hello/say?age=1&name=" + URLEncoder.encode("火星", "utf-8"));
        assertEquals("C1", result.contextName());
        assertEquals("Hello", result.classTied());
        assertEquals("say", result.funTied());
        assertEquals("1", result.getParam("age"));
        assertEquals("火星", result.getParam("name"));
    }
}
