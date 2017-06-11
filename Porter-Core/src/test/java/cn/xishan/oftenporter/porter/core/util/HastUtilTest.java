package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/11.
 */

public class HastUtilTest
{
    @Test
    public void testSha1(){
        String sha1=HashUtil.sha1("1234567".getBytes());
        LogUtil.printPosLn(sha1.length(),":",sha1);
        Assert.assertEquals(40,sha1.length());
    }
}
