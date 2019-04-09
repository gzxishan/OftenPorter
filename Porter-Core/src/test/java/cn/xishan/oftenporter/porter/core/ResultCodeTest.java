package cn.xishan.oftenporter.porter.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2018/12/5.
 */
public class ResultCodeTest
{
    @Test
    public void testCode(){
        ResultCode code10=ResultCode.toResponseCode(10);
        Assert.assertEquals(null,code10);

        ResultCode code20=ResultCode.toResponseCode(20);
        Assert.assertEquals(null,code20);
        Assert.assertEquals(null,code10);
    }
}
