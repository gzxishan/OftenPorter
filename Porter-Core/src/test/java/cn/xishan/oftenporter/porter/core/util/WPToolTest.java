package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/9.
 */
public class WPToolTest
{
    @Test
    public void testIsEmpty(){
        Assert.assertTrue(WPTool.isEmpty(""));
        Assert.assertTrue(WPTool.isEmpty(new StringBuilder()));
        Assert.assertFalse(WPTool.isEmpty(new Object()));
    }
}
