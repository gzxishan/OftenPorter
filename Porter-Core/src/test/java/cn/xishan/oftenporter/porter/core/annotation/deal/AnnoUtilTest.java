package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.config.ChangeableProperty;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/5.
 */
public class AnnoUtilTest
{

    abstract class A<T>
    {
        public ChangeableProperty<String> property;
        public ChangeableProperty<T> property2;
        Function<String, Integer> function;

        public void fun1(ChangeableProperty<String> property)
        {

        }

        public void fun2(ChangeableProperty<T> property)
        {

        }

        public void fun3(Function<String, Integer> function)
        {

        }
    }

    class ClassB
    {
        public Map sameMethod1(String a, int b, Boolean c)
        {
            return null;
        }

    }

    class ClassA extends A<ClassB>
    {

    }

    static Field getField(Class clazz, String field)
    {
        try
        {
            return clazz.getDeclaredField(field);
        } catch (Exception e)
        {
            try
            {
                return clazz.getField(field);
            } catch (Exception e2)
            {
            }
            return null;
        }
    }

    static Method getMethod(Class clazz, String name)
    {
        try
        {
            //先获取此类定义的函数
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods)
            {
                if (method.getName().equals(name))
                {
                    return method;
                }
            }

            methods = OftenTool.getAllMethods(clazz);
            for (Method method : methods)
            {
                if (method.getName().equals(name))
                {
                    return method;
                }
            }
            return null;
        } catch (Exception e)
        {
            return null;
        }
    }

    @Test
    public void testRealTypeInField()
    {
        Assert.assertEquals(String.class,
                AnnoUtil.Advance.getRealTypeInField(A.class, getField(A.class, "property")));

        Assert.assertEquals(Integer.class,
                AnnoUtil.Advance.getRealTypeInField(A.class, getField(A.class, "function"), 1));

        Assert.assertEquals(ClassB.class,
                AnnoUtil.Advance.getRealTypeInField(ClassA.class, getField(ClassA.class, "property2")));
    }

    @Test
    public void testRealTypeInMethodParameter()
    {
        Assert.assertEquals(String.class,
                AnnoUtil.Advance.getRealTypeInMethodParameter(A.class, getMethod(A.class, "fun1"), 0));

        Assert.assertEquals(ClassB.class,
                AnnoUtil.Advance.getRealTypeInMethodParameter(ClassA.class, getMethod(ClassA.class, "fun2"), 0));

        Assert.assertEquals(Integer.class,
                AnnoUtil.Advance.getRealTypeInMethodParameter(A.class, getMethod(A.class, "fun3"), 0, 1));
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

    interface InterfaceB
    {
        List sameMethod2(String a, int b, Boolean c);
    }

    class SubB extends ClassB implements InterfaceB
    {
        @Override
        public HashMap sameMethod1(String a, int b, Boolean c)
        {
            return null;
        }

        @Override
        public ArrayList sameMethod2(String a, int b, Boolean c)
        {
            return null;
        }
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
    public void testGetSameMethod()
    {
        Assert.assertNotNull(AnnoUtil.Advance.getSameMethodOfParent(ClassB.class,
                getMethod(SubB.class, "sameMethod1")));
        Assert.assertNotNull(AnnoUtil.Advance.getSameMethodOfChild(SubB.class,
                getMethod(ClassB.class, "sameMethod1")));

        Assert.assertNotNull(AnnoUtil.Advance.getSameMethodOfParent(InterfaceB.class,
                getMethod(SubB.class, "sameMethod2")));
        Assert.assertNotNull(AnnoUtil.Advance.getSameMethodOfChild(SubB.class,
                getMethod(InterfaceB.class, "sameMethod2")));
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
