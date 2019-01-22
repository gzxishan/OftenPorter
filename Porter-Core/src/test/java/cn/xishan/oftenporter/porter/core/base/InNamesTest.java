package cn.xishan.oftenporter.porter.core.base;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Created by https://github.com/CLovinr on 2019-01-22.
 */
public class InNamesTest
{
    @Test
   public void testName(){
        InNames.Name name = new InNames.Name("varname(array)[\\[\\]]");
        assertEquals("[]",name.getDefaultValue());

        name = new InNames.Name("varname(\\(\\))[\\[\\]]");
        assertEquals("()",name.getParserOption().getNameConfig());
   }
}