package cn.xishan.oftenporter.porter.simple.parsers;


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
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt("in{'123','456':'-456'}");
        Assert.assertEquals("123", varConfigDealt.getValue(new StringParser(),"123").getValue());
        Assert.assertEquals("-456", varConfigDealt.getValue(new StringParser(),"456").getValue());

        varConfigDealt = new StringParser.VarConfigDealt("in{123,'456':-456}");
        Assert.assertEquals("123", varConfigDealt.getValue(new StringParser(),"123").getValue());
        Assert.assertEquals("-456", varConfigDealt.getValue(new StringParser(),"456").getValue());
    }

    @Test
    public void testInEx()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt("in{'123','456':'-456'}");
       Assert.assertFalse( varConfigDealt.getValue(new StringParser(),"abc").isLegal());

        varConfigDealt = new StringParser.VarConfigDealt("in{'123',456:-456}");
        Assert.assertFalse( varConfigDealt.getValue(new StringParser(),"abc").isLegal());
    }

    @Test
    public void testNin()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt("-in{'1234','4567':'-456'}");
        Assert.assertEquals("123", varConfigDealt.getValue(new StringParser(),"123").getValue());
        Assert.assertEquals("-456", varConfigDealt.getValue(new StringParser(),"-456").getValue());

        varConfigDealt = new StringParser.VarConfigDealt("-in{1234,4567:-456}");
        Assert.assertEquals("123", varConfigDealt.getValue(new StringParser(),"123").getValue());
        Assert.assertEquals("-456", varConfigDealt.getValue(new StringParser(),"-456").getValue());
    }

    @Test
    public void testNinEx()
    {
        StringParser.VarConfigDealt varConfigDealt = new StringParser.VarConfigDealt("-in{'123','456':'-456'}");
        Assert.assertFalse( varConfigDealt.getValue(new StringParser(),"123").isLegal());
    }
}
