package cn.xishan.oftenporter.porter.core.annotation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;



/**
 * Created by chenyg on 2018-02-27.
 */
@PortComment
@Mixin
@PortIn
public class AnnotationTest
{
    /**
     * 用于测试注解顺序。
     */
    @Test
    public void testAnnotationOrder()
    {
        Annotation[] annotations = getClass().getAnnotations();
        Assert.assertEquals(PortComment.class, annotations[0].annotationType());
        Assert.assertEquals(Mixin.class, annotations[1].annotationType());
        Assert.assertEquals(PortIn.class, annotations[2].annotationType());
        Assert.assertTrue(Void.TYPE.equals(void.class));
        Assert.assertTrue(Void.TYPE==void.class);
    }
}
