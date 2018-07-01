package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.init.IAnnotationConfigable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public class DefaultAnnotationConfigableTest
{
    @Test
    public void testGetValue(){
        Properties properties = new Properties();
        properties.setProperty("name","tom");
        properties.setProperty("age","12");
        IAnnotationConfigable<Properties> annotationConfigable = new DefaultAnnotationConfigable();
        String value = "${name}";
        Assert.assertEquals("tom",annotationConfigable.getValue(properties,value));

        value = " ${name} age is ${age} ";
        Assert.assertEquals("tom age is 12",annotationConfigable.getValue(properties,value));
    }
}