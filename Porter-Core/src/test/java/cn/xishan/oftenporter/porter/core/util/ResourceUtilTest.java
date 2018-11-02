package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;

/**
 * @author Created by https://github.com/CLovinr on 2018/11/2.
 */
public class ResourceUtilTest
{
    @Test
    public void test() throws MalformedURLException
    {
        File file = new File("src/test/resources/test.txt");
        String absolutePath = file.getAbsolutePath().replace('\\', '/');
        LogUtil.printPosLn(absolutePath);

        String pathFile = file.toURI().toURL().toString();

//        LogUtil.printPosLn(ResourceUtil.getAbsoluteResourceString("http://www.baidu.com","utf-8"));

        Assert.assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString(pathFile, "utf-8"));
        Assert.assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("/test.txt", "utf-8"));
        Assert.assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("classpath:/test.txt", "utf-8"));
        Assert.assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("classpath:test.txt", "utf-8"));
    }
}
