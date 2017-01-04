package cn.xishan.oftenporter.porter.core.base;

import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/14.
 */
public class PortUtilTest
{

    static class A
    {
        public String fun(String str)
        {
            return null;
        }
    }

    static class B extends A
    {
        @Override
        public String fun(String str)
        {
            return super.fun(str);
        }
    }

    static class C extends B
    {
        @Override
        public String fun(String str)
        {
            return super.fun(str);
        }
    }

    @Test
    public void testRemoveOverrideMethod() throws Exception
    {
//        Method[] methods = C.class.getMethods();
//        System.out.println("************************************");
//        for (int i = 0; i < methods.length; i++)
//        {
//            System.out.println(methods[i]);
//        }
//        methods = PortUtil.removeSuperMethods(C.class.getMethods());
//        System.out.println("\n************************************");
//        for (int i = 0; i < methods.length; i++)
//        {
//            System.out.println(methods[i]);
//        }

    }
}
