package cn.xishan.oftenporter.porter.simple.parsers;

import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2017/12/31.
 */
public class IntParserTest
{
    @Test
    public void testIn()
    {
        IntParser.VarConfigInDealt varConfigInDealt = new IntParser.VarConfigInDealt(new IntParser(), "in{123,456}");
        Assert.assertEquals(123, varConfigInDealt.getValue(123));
        Assert.assertEquals(456, varConfigInDealt.getValue(456));
    }

    @Test
    public void testInEx()
    {
        IntParser.VarConfigInDealt varConfigInDealt = new IntParser.VarConfigInDealt(new IntParser(), "in{123,456}");
        Assert.assertTrue(varConfigInDealt.getValue(789) instanceof ITypeParser.ParseResult);
    }

    @Test
    public void testNin()
    {
        IntParser.VarConfigInDealt varConfigInDealt = new IntParser.VarConfigInDealt(new IntParser(), "-in{123,456}");
        Assert.assertEquals(12, varConfigInDealt.getValue(12));
        Assert.assertEquals(-5, varConfigInDealt.getValue(-5));
    }

    @Test
    public void testNinEx()
    {
        IntParser.VarConfigInDealt varConfigInDealt = new IntParser.VarConfigInDealt(new IntParser(), "-in{123,456}");

        Assert.assertTrue(varConfigInDealt.getValue(123) instanceof ITypeParser.ParseResult);
    }

    @Test
    public void testRange()
    {
        IntParser.VarConfigRangeDealt
                varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range(0,)");
        Assert.assertEquals(12, varConfigRangeDealt.getValue(12));
        Assert.assertTrue(varConfigRangeDealt.getValue(0) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(-1) instanceof ITypeParser.ParseResult);

        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range(-10,)");
        Assert.assertEquals(-1, varConfigRangeDealt.getValue(-1));
        Assert.assertTrue(varConfigRangeDealt.getValue(-10) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(-15) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range[-10,]");
        Assert.assertEquals(-1, varConfigRangeDealt.getValue(-1));
        Assert.assertEquals(-10,varConfigRangeDealt.getValue(-10));
        Assert.assertTrue(varConfigRangeDealt.getValue(-15) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range(,100)");
        Assert.assertEquals(65, varConfigRangeDealt.getValue(65));
        Assert.assertTrue(varConfigRangeDealt.getValue(100) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(101) instanceof ITypeParser.ParseResult);

        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range(,100]");
        Assert.assertEquals(65, varConfigRangeDealt.getValue(65));
        Assert.assertEquals(100, varConfigRangeDealt.getValue(100));
        Assert.assertTrue(varConfigRangeDealt.getValue(101) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "range[50,100]");
        Assert.assertEquals(65, varConfigRangeDealt.getValue(65));
        Assert.assertEquals(100, varConfigRangeDealt.getValue(100));
        Assert.assertTrue(varConfigRangeDealt.getValue(49) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(101) instanceof ITypeParser.ParseResult);

    }

    @Test
    public void testNrange()
    {
        IntParser.VarConfigRangeDealt
                varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range(0,)");
        Assert.assertEquals(-12, varConfigRangeDealt.getValue(-12));
        Assert.assertEquals(0, varConfigRangeDealt.getValue(0));
        Assert.assertTrue(varConfigRangeDealt.getValue(1) instanceof ITypeParser.ParseResult);

        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range(-10,)");
        Assert.assertEquals(-10, varConfigRangeDealt.getValue(-10));
        Assert.assertEquals(-11, varConfigRangeDealt.getValue(-11));
        Assert.assertTrue(varConfigRangeDealt.getValue(-9) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(100) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range[-10,]");
        Assert.assertEquals(-11, varConfigRangeDealt.getValue(-11));
        Assert.assertTrue(varConfigRangeDealt.getValue(-10) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(100) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range(,100)");
        Assert.assertEquals(100, varConfigRangeDealt.getValue(100));
        Assert.assertEquals(120, varConfigRangeDealt.getValue(120));
        Assert.assertTrue(varConfigRangeDealt.getValue(99) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(-100) instanceof ITypeParser.ParseResult);

        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range(,100]");
        Assert.assertEquals(101, varConfigRangeDealt.getValue(101));
        Assert.assertTrue(varConfigRangeDealt.getValue(100) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(-100) instanceof ITypeParser.ParseResult);


        varConfigRangeDealt = new IntParser.VarConfigRangeDealt(new IntParser(), "-range[50,100]");
        Assert.assertEquals(49, varConfigRangeDealt.getValue(49));
        Assert.assertEquals(-100, varConfigRangeDealt.getValue(-100));
        Assert.assertEquals(101, varConfigRangeDealt.getValue(101));
        Assert.assertEquals(1000, varConfigRangeDealt.getValue(1000));
        Assert.assertTrue(varConfigRangeDealt.getValue(50) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(60) instanceof ITypeParser.ParseResult);
        Assert.assertTrue(varConfigRangeDealt.getValue(100) instanceof ITypeParser.ParseResult);

    }
}
