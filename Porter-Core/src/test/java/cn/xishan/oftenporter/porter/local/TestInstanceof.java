package cn.xishan.oftenporter.porter.local;

import org.junit.Test;

/**
 * Created by https://github.com/CLovinr on 2016/9/5.
 */
public class TestInstanceof
{

    interface IA{

    }

    class A implements IA{

    }

    interface IB extends IA{

    }

    @Test
    public void main()
    {
        isInstance(Object.class,TestInstanceof.class);
        isInstance(IA.class,A.class);
        isInstance(IA.class,IB.class);
        isInstance(IA.class,IA.class);
        isInstance(Object.class,Object.class);
    }

    void isInstance(Class<?> clazz1, Class<?> clazz2)
    {
        System.out.format("%s\t%s%n", clazz1.isAssignableFrom(clazz2), clazz2.isAssignableFrom(clazz1));
    }
}
