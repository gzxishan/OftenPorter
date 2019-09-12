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

    @Test
    public void testDecode2() throws UnsupportedEncodingException
    {
        DefaultUrlDecoder defaultDecoder = new DefaultUrlDecoder("utf-8");

        UrlDecoder.Result result = defaultDecoder
                .decode("/C1/Hello/aspect/util.js?age=1&name=" + URLEncoder.encode("火星", "utf-8"));
        assertEquals("C1", result.contextName());
        assertEquals("Hello", result.classTied());
        assertEquals("aspect/util.js", result.funTied());
        assertEquals("1", result.getParam("age"));
        assertEquals("火星", result.getParam("name"));
    }

    @Test
    public void testDecode3(){
        String path="/C1/Hello/**=id=031z_c000Pkga01XGHObO/wxConnect2-wx?signature=19bdc1188089a01aeb5746e8f129e42c8939eafe&echostr=4296366052486078462&timestamp=1568282046&nonce=895168135";
        DefaultUrlDecoder defaultDecoder = new DefaultUrlDecoder("utf-8");
        UrlDecoder.Result result= defaultDecoder.decode(path);
        assertEquals("031z_c000Pkga01XGHObO",result.getParam("id"));
        assertEquals("C1", result.contextName());
        assertEquals("Hello", result.classTied());
        assertEquals("wxConnect2-wx",result.funTied());
    }
}
