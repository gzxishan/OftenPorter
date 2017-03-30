package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.slf4j.Logger;
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

    /**
     * Method: getCodePos()
     */
    @Test
    public void testGetCodePos() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: getCodePos(int n)
     */
    @Test
    public void testGetCodePosN() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: toString(StackTraceElement stackTraceElement)
     */
    @Test
    public void testToString() throws Exception
    {
//TODO: Test goes here... 
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

    /**
     * Method: methodAndClass(int n)
     */
    @Test
    public void testMethodAndClass() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: printPosLn(Object... objects)
     */
    @Test
    public void testPrintPosLn() throws Exception
    {
        LogUtil.printPosLn("haha");
    }

    /**
     * Method: printErrPosLn(Object... objects)
     */
    @Test
    public void testPrintErrPosLn() throws Exception
    {
        LogUtil.printErrPosLn("errHaha");
    }

    /**
     * Method: printErrPosLnS(int stack, Object... objects)
     */
    @Test
    public void testPrintErrPosLnS() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: printPosLnS(int stack, Object... objects)
     */
    @Test
    public void testPrintPosLnS() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: setSimpleDateFormat(SimpleDateFormat simpleDateFormat)
     */
    @Test
    public void testSetSimpleDateFormat() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: getTime()
     */
    @Test
    public void testGetTime() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: getTime(Calendar calendar)
     */
    @Test
    public void testGetTimeCalendar() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: getTime(long mills)
     */
    @Test
    public void testGetTimeMills() throws Exception
    {
//TODO: Test goes here... 
    }

    /**
     * Method: getTime(Date date)
     */
    @Test
    public void testGetTimeDate() throws Exception
    {
//TODO: Test goes here... 
    }


} 
