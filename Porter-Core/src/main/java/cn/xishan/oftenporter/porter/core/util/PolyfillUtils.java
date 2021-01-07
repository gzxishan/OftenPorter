package cn.xishan.oftenporter.porter.core.util;

import java.util.function.Supplier;

/**
 * @author Created by https://github.com/CLovinr on 2021/1/7.
 */
public class PolyfillUtils
{
    public static <T> ThreadLocal<T> withInitial(Supplier<? extends T> supplier)
    {
        ThreadLocal<T> threadLocal = new ThreadLocal<T>()
        {
            @Override
            public T get()
            {
                T t = super.get();
                if (t == null)
                {
                    t = supplier.get();
                }
                return t;
            }
        };
        return threadLocal;
    }
}
