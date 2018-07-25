package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/9.
 */
public class WPToolTest
{
    @Test
    public void testIsEmpty(){
        Assert.assertTrue(WPTool.isEmpty(""));
        Assert.assertTrue(WPTool.isEmpty(new StringBuilder()));
        Assert.assertFalse(WPTool.isEmpty(new Object()));
    }


    interface A{
        void a1();
        void a2();
    }

    interface A_1 extends A{
        @Override
        void a1();
    }

    class C implements A{

        @Override
        public void a1()
        {

        }

        @Override
        public void a2()
        {

        }
    }

    class C2 implements A_1,A{

        @Override
        public void a2()
        {

        }

        @Override
        public void a1()
        {

        }
    }


    abstract class C3 implements A_1,A{

    }

    interface B1{
        void b1();
    }

    interface B2{
        void b2();
    }

    interface B3 extends B1,B2{

    }

    @Test
    public void testGetAllMethods(){
        Method[] methods1 = WPTool.getAllMethods(A.class);
        print(methods1);

        Method[] methods2 = WPTool.getAllMethods(A_1.class);
        print(methods2);

        Method[] methods3 = WPTool.getAllMethods(C.class);
        print(methods3);

        Method[] methods4 = WPTool.getAllMethods(C2.class);
        print(methods4);

        Method[] methods5 = WPTool.getAllMethods(C3.class);
        print(methods5);

        Method[] methods6 = WPTool.getAllMethods(B3.class);
        print(methods6);
    }

    static void print(Method[] methods){
        for (Method method:methods)
        {
            if(method.getDeclaringClass().equals(Object.class))
            {
                continue;
            }
            System.out.print(method.getDeclaringClass().getSimpleName()+"."+method.getName()+" ");
        }
        System.out.println();
    }
}
