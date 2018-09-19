package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 被注解的注解可以注解在类或函数上（类上的对所有函数有效）。
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
public @interface AspectOperationOfNormal
{
    interface Invoker
    {
        /**
         * 是否已经调用过{@linkplain #invoke(Object[])}
         *
         * @return
         */
        boolean hasInvoked();

        Object invoke(Object[] args) throws Throwable;

        <T> T getAttr(String key);

        /**
         * @param key
         * @param t
         * @param <T>
         * @return 返回上一个值
         */
        <T> T putAttr(String key, T t);
    }

    abstract class DefaultInvoker implements Invoker
    {
        private Map attrMap = null;

        @Override
        public <T> T getAttr(String key)
        {
            Object rs = null;
            if (attrMap != null)
            {
                rs = attrMap.get(key);
            }
            return (T) rs;
        }


        @Override
        public <T> T putAttr(String key, T t)
        {
            Object last = null;
            if (attrMap == null)
            {
                attrMap = new HashMap();
            }
            last = attrMap.put(key, t);
            return (T) last;
        }
    }

    class HandleAdapter<T extends Annotation> implements Handle<T>
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(HandleAdapter.class);

        @Override
        public boolean init(T current, IConfigData configData, Object originObject,
                Method originMethod) throws Exception
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public boolean preInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args, boolean hasInvoked, Object lastReturn) throws Throwable
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public Object doInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args, Object lastReturn) throws Throwable
        {
            LOGGER.debug("not Override.");
            return invoker.invoke(args);
        }

        @Override
        public Object afterInvoke(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args, Object lastReturn)throws Throwable
        {
            LOGGER.debug("not Override.");
            return lastReturn;
        }

        @Override
        public void onException(WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args,Throwable throwable) throws Throwable
        {

        }

    }


    /**
     * <p>
     * 针对多个切面拦Handle，如顺序为handleA、handleB、handleC，则调用顺序为：
     * <ol>
     * <li>
     * init:handleA--handleB--handleC
     * </li>
     * <li>
     * preInvoke,doInvoke:handleA--handleB--handleC
     * </li>
     * <li>
     * afterInvoke或onException:handleC--handleB--handleA
     * </li>
     * </ol>
     * </p>
     *
     * @param <T>
     */
    interface Handle<T extends Annotation>
    {
        /**
         * 不支持@AutoSet
         *
         * @param current
         * @param originObject
         * @param originMethod
         * @return true表示添加，false不添加。
         */
        boolean init(T current, IConfigData configData, Object originObject, Method originMethod) throws Exception;

        /**
         * 是否会调用{@linkplain #doInvoke(WObject, boolean, Object, Method, Invoker, Object[], Object)}
         */
        boolean preInvoke(@MayNull WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                boolean hasInvoked,
                Object lastReturn) throws Throwable;


        Object doInvoke(@MayNull WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                Object lastReturn) throws Throwable;

        Object afterInvoke(@MayNull WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                Object lastReturn) throws Throwable;

        void onException(@MayNull WObject wObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args,Throwable throwable) throws Throwable;

    }

    Class<? extends Handle> handle();
}
