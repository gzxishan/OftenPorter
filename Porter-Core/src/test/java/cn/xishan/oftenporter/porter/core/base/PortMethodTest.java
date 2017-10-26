package cn.xishan.oftenporter.porter.core.base;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/23.
 */
public class PortMethodTest
{
    static final Logger LOGGER = LoggerFactory.getLogger(PortMethodTest.class);

    @Test
    public void testSort()
    {
        PropertyConfigurator.configure(getClass().getResource("/log4j.properties"));
        PortMethod[] methods = {
                PortMethod.POST, PortMethod.GET, PortMethod.PUT, PortMethod.DELETE
        };
        Arrays.sort(methods);

        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.POST) >= 0);
        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.GET) >= 0);
        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.PUT) >= 0);
        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.DELETE) >= 0);

        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.OPTIONS) < 0);
        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.TARCE) < 0);
        Assert.assertTrue(Arrays.binarySearch(methods, PortMethod.HEAD) < 0);


    }
}
