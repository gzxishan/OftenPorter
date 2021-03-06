package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public class DefaultAnnotationConfigableTest
{
    @Test
    public void testGetValue()
    {
        Properties properties = new Properties();
        properties.setProperty("name", "tom");
        properties.setProperty("age", "12");

        IAnnotationConfigable annotationConfigable = new DefaultAnnotationConfigable();
        annotationConfigable.getConfigData().putAll(properties);

        String value = "${name}";
        Assert.assertEquals("tom", annotationConfigable.getAnnotationStringValue(value));

        value = " ${name} age is ${age} ";
        Assert.assertEquals("tom age is 12", annotationConfigable.getAnnotationStringValue(value));
    }
}
