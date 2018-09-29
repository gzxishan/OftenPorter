package cn.xishan.oftenporter.porter.core.annotation.deal;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/5.
 */
public class AnnoUtilTest
{

    abstract class A<T>
    {

    }

    class ClassB
    {

    }

    class ClassA extends A<ClassB>
    {

    }


    @Test
    public void testGetDirectGenericRealTypeAt1()
    {
        Assert.assertEquals(ClassA.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassA.class, 0));
        Assert.assertEquals(ClassB.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassA.class, 1));
    }

    class ClassC<T>
    {

    }

    class AA<S, T>
    {

    }

    class ClassD
    {

    }

    class ClassAA extends AA<ClassB, ClassC<ClassD>>
    {

    }

    @Test
    public void testGetDirectGenericRealTypeAt2()
    {
        Assert.assertEquals(ClassAA.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassAA.class, 0));
        Assert.assertEquals(ClassB.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassAA.class, 1));
        Assert.assertEquals(ClassC.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassAA.class, 2));
        Assert.assertEquals(ClassD.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassAA.class, 3));
    }

    interface IC<T>
    {

    }


    class ClassE extends A<ClassB> implements IC<ClassD>
    {

    }

    @Test
    public void testGetDirectGenericRealTypeAt3()
    {
        Assert.assertEquals(ClassE.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassE.class, 0));
        Assert.assertEquals(ClassB.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassE.class, 1));
        Assert.assertEquals(ClassD.class, AnnoUtil.Advance.getDirectGenericRealTypeAt(ClassE.class, 2));
    }

    class SubB extends ClassB
    {

    }

    class SubB2 extends ClassB
    {

    }

    class ClassF extends A<SubB>
    {

    }

    class ClassF2 extends A<SubB> implements IC<SubB2>
    {

    }

    @Test
    public void testGetDirectGenericRealTypeBySuperType1()
    {
        Assert.assertEquals(ClassF.class,
                AnnoUtil.Advance.getDirectGenericRealTypeBySuperType(ClassF.class, ClassF.class));
        Assert.assertEquals(SubB.class,
                AnnoUtil.Advance.getDirectGenericRealTypeBySuperType(ClassF.class, ClassB.class));
    }

    @Test(expected = Exception.class)
    public void testGetDirectGenericRealTypeBySuperType2()
    {
        Assert.assertEquals(SubB2.class,
                AnnoUtil.Advance.getDirectGenericRealTypeBySuperType(ClassF2.class, ClassB.class));
    }
}
