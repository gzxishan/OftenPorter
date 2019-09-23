package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * LogUtil Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre> 10-13, 2016</pre>
 */
public class LogUtilTest
{

    @Before
    public void before() throws Exception
    {
    }

    @After
    public void after() throws Exception
    {
    }

    @Test()
    public void testSetOrRemoveOnGetLoggerListener()
    {
        LogUtil.LogKey logKey = new LogUtil.LogKey("123456");
        LogUtil.setOrRemoveOnGetLoggerListener(logKey, name -> LoggerFactory.getLogger(name));
        Assert.assertNotNull(LogUtil.logger("hello"));
        LogUtil.setOrRemoveOnGetLoggerListener(logKey, name -> LoggerFactory.getLogger(name));

        LogUtil.setOrRemoveOnGetLoggerListener(logKey, null);
    }

    @Test(expected = RuntimeException.class)
    public void testSetOrRemoveOnGetLoggerListenerEx()
    {
        LogUtil.LogKey logKey = new LogUtil.LogKey("123456");
        LogUtil.LogKey logKey2 = new LogUtil.LogKey("1234562");
        LogUtil.setOrRemoveOnGetLoggerListener(logKey, name -> LoggerFactory.getLogger(name));

        Assert.assertNotNull(LogUtil.logger("hello"));

        LogUtil.setOrRemoveOnGetLoggerListener(logKey, name -> LoggerFactory.getLogger(name));

        LogUtil.setOrRemoveOnGetLoggerListener(logKey2, null);
    }

    @Test
    public void testListCodePos(){
        String[] pos=LogUtil.listCodePos(0,10);
        LogUtil.printPosLn(OftenStrUtil.join("\n",pos));
    }

}
