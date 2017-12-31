package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.base.ITypeParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/31.
 */
public class StringParserTest
{
    @Test
    public void testIn()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "in{'123','456':'-456'}");
        Assert.assertEquals("123", varConfigDealt.getValue("123"));
        Assert.assertEquals("-456", varConfigDealt.getValue("456"));

        varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "in{123,'456':-456}");
        Assert.assertEquals("123", varConfigDealt.getValue("123"));
        Assert.assertEquals("-456", varConfigDealt.getValue("456"));
    }

    @Test
    public void testInEx()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "in{'123','456':'-456'}");
       Assert.assertTrue( varConfigDealt.getValue("abc") instanceof ITypeParser.ParseResult);

        varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "in{'123',456:-456}");
        Assert.assertTrue( varConfigDealt.getValue("abc") instanceof ITypeParser.ParseResult);
    }

    @Test
    public void testNin()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "-in{'1234','4567':'-456'}");
        Assert.assertEquals("123", varConfigDealt.getValue("123"));
        Assert.assertEquals("-456", varConfigDealt.getValue("-456"));

        varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "-in{1234,4567:-456}");
        Assert.assertEquals("123", varConfigDealt.getValue("123"));
        Assert.assertEquals("-456", varConfigDealt.getValue("-456"));
    }

    @Test
    public void testNinEx()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt(new StringParser(),
                "-in{'123','456':'-456'}");
        Assert.assertTrue( varConfigDealt.getValue("123") instanceof ITypeParser.ParseResult);
    }
}
