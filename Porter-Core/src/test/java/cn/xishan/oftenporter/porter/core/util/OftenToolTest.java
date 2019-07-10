package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/9.
 */
public class OftenToolTest
{
    @Test
    public void testIsEmpty()
    {
        Assert.assertTrue(OftenTool.isEmpty(null));
        Assert.assertTrue(OftenTool.isEmpty(""));
        Assert.assertTrue(OftenTool.isEmpty(new StringBuilder()));
        Assert.assertFalse(OftenTool.isNullOrEmptyCharSequence(new Object()));

        Assert.assertTrue(OftenTool.isEmptyOf(new ArrayList<>()));
        Assert.assertTrue(OftenTool.isEmptyOf(new HashSet<>()));
        Assert.assertTrue(OftenTool.isEmptyOf(new HashMap<>()));

        Assert.assertTrue(OftenTool.isEmptyOf(new Object[0]));
//        Assert.assertTrue(OftenTool.isEmptyOf(new int[0]));
        Assert.assertTrue(OftenTool.isEmptyOf(new Object[0][0]));
    }


    interface A
    {
        void a1();

        void a2();
    }

    interface A_1 extends A
    {
        @Override
        void a1();
    }

    class C implements A
    {

        @Override
        public void a1()
        {

        }

        @Override
        public void a2()
        {

        }
    }

    class C2 implements A_1, A
    {

        @Override
        public void a2()
        {

        }

        @Override
        public void a1()
        {

        }
    }


    abstract class C3 implements A_1, A
    {

    }

    interface B1
    {
        void b1();
    }

    interface B2
    {
        void b2();
    }

    interface B3 extends B1, B2
    {

    }

    @Test
    public void testGetAllMethods()
    {
        Method[] methods1 = OftenTool.getAllMethods(A.class);
        print(methods1);

        Method[] methods2 = OftenTool.getAllMethods(A_1.class);
        print(methods2);

        Method[] methods3 = OftenTool.getAllMethods(C.class);
        print(methods3);

        Method[] methods4 = OftenTool.getAllMethods(C2.class);
        print(methods4);

        Method[] methods5 = OftenTool.getAllMethods(C3.class);
        print(methods5);

        Method[] methods6 = OftenTool.getAllMethods(B3.class);
        print(methods6);
    }

    static void print(Method[] methods)
    {
        for (Method method : methods)
        {
            if (method.getDeclaringClass().equals(Object.class))
            {
                continue;
            }
            System.out.print(method.getDeclaringClass().getSimpleName() + "." + method.getName() + " ");
        }
        System.out.println();
    }

    @Test
    public void testIsAssignable()
    {
        Assert.assertTrue(OftenTool.isAssignable(Object.class, Object.class));
        Assert.assertTrue(OftenTool.isAssignable(ArrayList.class, Collection.class));
        Assert.assertTrue(OftenTool.isAssignableForOneOf(String[].class, Object[].class));
    }
}
