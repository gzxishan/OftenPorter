package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chenyg on 2017-04-18.
 */
public class PackageUtilTest
{
    @Test
    public void testGetPackageWithRelative(){
        Class<?> clazz = Object.class;
        Assert.assertEquals("java.util.Set",PackageUtil.getPackageWithRelative(clazz,"../util/Set","."));
        Assert.assertEquals("java",PackageUtil.getPackageWithRelative(clazz,"..","."));

    }

}
