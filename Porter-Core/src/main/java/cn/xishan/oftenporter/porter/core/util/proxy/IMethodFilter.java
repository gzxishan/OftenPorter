package cn.xishan.oftenporter.porter.core.util.proxy;

import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.lang.reflect.Method;

/**
 * 对于同一个Class代理，如果处理一样，可以通过{@linkplain #getHashCode(Class)}与{@linkplain #isEquals(Class, IMethodFilter, Class)}
 * 避免产生大量代理类、而出现内存泄漏。
 *
 * @author Created by https://github.com/CLovinr on 2018/9/20.
 */
public interface IMethodFilter
{
    boolean contains(Class clazz, Method method);

    int getHashCode(Class clazz);

    boolean isEquals(Class mineClazz, IMethodFilter methodFilter, Class otherClazz);

    abstract class Adapater implements IMethodFilter
    {

        @Override
        public int getHashCode(Class clazz)
        {
            return clazz.hashCode();
        }

        @Override
        public boolean isEquals(Class mineClazz, IMethodFilter methodFilter, Class otherClazz)
        {
            return OftenTool.subclassOf(getClass(), methodFilter.getClass()) == 0 && mineClazz.equals(otherClazz);
        }
    }
}
