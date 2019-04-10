package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chenyg on 2017-04-18.
 */
public class PackageUtilTest
{
    @Test
    public void testGetPackageWithRelative()
    {
        Class<?> clazz = Object.class;
        Assert.assertEquals("java.util.Set", PackageUtil.getPackageWithRelative(clazz, "../util/Set", '.'));
        Assert.assertEquals("java", PackageUtil.getPackageWithRelative(clazz, "..", '.'));

        LogUtil.printPosLn(PackageUtil.getPathWithRelative('.', clazz.getName(), false, "../util/Set", '.'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "/mybatis/", null, "test.xml", '/'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "mybatis/", null, "test.xml", '/'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "/mybatis", null, "test.xml", '/'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "/mybatis", true, "test.xml", '/'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "/mybatis", true, "/test.xml", '.'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative('/', "/mybatis", true, "/test.xml", '/'));
        LogUtil.printPosLn(PackageUtil.getPathWithRelative("/","./"));
    }

}
