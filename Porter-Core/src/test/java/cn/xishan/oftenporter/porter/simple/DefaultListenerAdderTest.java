package cn.xishan.oftenporter.porter.simple;

import org.junit.Assert;
import org.junit.Test;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-02.
 */
public class DefaultListenerAdderTest
{
    @Test
    public void testOrder()
    {
        DefaultListenerAdder<Integer> defaultListenerAdder = new DefaultListenerAdder<>();
        defaultListenerAdder.addListener("a", 1);
        defaultListenerAdder.addListener("x", 2);
        defaultListenerAdder.addListener("m", 3);
        defaultListenerAdder.addListener("2", 4);
        defaultListenerAdder.addListener("e", 5);

        Enumeration<Integer> enumeration = defaultListenerAdder.listeners(1);
        for (int i = 1; i <= 5; i++)
        {
            Assert.assertEquals(i, (int) enumeration.nextElement());
        }
        enumeration = defaultListenerAdder.listeners(-1);
        for (int i = 5; i >=1; i--)
        {
            Assert.assertEquals(i, (int) enumeration.nextElement());
        }

    }
}
