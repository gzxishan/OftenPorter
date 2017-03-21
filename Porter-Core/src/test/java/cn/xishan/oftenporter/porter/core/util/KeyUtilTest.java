package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * KeyUtil Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>2-7, 2017</pre>
 */
public class KeyUtilTest
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
     * Method: randomUUID()
     */
    @Test
    public void testRandomUUID() throws Exception
    {
//TODO: Test goes here...
        Assert.assertEquals(32, KeyUtil.randomUUID().length());
    }

    @Test
    public void testRandom48Key()
    {
        String key = KeyUtil.random48Key();
        LogUtil.printPosLn(key);
        Assert.assertEquals(48,key.length());
    }

    /**
     * Method: secureRandomKey(int length)
     */
    @Test
    public void testSecureRandomKey() throws Exception
    {
//TODO: Test goes here... 
    }


} 
