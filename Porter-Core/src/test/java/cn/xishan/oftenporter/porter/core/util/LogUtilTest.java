package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.slf4j.LoggerFactory;

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
        LogUtil.setOrRemoveOnGetLoggerListener(name -> LoggerFactory.getLogger(name));
        Assert.assertNotNull(LogUtil.logger("hello"));
        LogUtil.setOrRemoveOnGetLoggerListener(name -> LoggerFactory.getLogger(name));

        LogUtil.setOrRemoveOnGetLoggerListener(null);
    }

    @Test
    public void testListCodePos()
    {
        String[] pos = LogUtil.listCodePos(0, 10);
        LogUtil.printPosLn(OftenStrUtil.join("\n", pos));
    }

}
