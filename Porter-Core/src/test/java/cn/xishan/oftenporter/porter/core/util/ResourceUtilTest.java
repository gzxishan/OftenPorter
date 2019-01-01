package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import static org.junit.Assert.*;

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

        assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString(pathFile, "utf-8"));
        assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("/test.txt", "utf-8"));
        assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("classpath:/test.txt", "utf-8"));
        assertEquals("HelloWorld", ResourceUtil.getAbsoluteResourceString("classpath:test.txt", "utf-8"));
    }

    @Test
    public void testListResources() throws IOException
    {
        List<ResourceUtil.RFile> list = ResourceUtil.listResources("rfiles",null,true);
        assertTrue(list.size()>0);
        for(ResourceUtil.RFile rFile:list){
            if(!rFile.isDir()){
                String content=FileTool.getString(rFile.getInputStream());
                assertEquals(rFile.getPath(),content);
            }
        }
    }
}
