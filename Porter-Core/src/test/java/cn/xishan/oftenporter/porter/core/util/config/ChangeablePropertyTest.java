package cn.xishan.oftenporter.porter.core.util.config;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ChangeablePropertyTest
{
    @Test
    public void testBase()
    {
        ChangeableProperty<String> property = new ChangeableProperty<>("a");

        boolean[] rs = {false};
        property.addListener((property1, newValue, oldValue) -> rs[0] = OftenTool.notEqual(newValue, oldValue));
        property.submitValue("b");

        assertTrue(rs[0]);
    }
}