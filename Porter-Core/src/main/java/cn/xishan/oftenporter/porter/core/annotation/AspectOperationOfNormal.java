package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 被注解的注解可以注解在类或函数上（类上的对所有函数有效）,且只对成员函数且非private的有效。
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
@AdvancedAnnotation(enableAdvancedAnnotation = true)
public @interface AspectOperationOfNormal
{
    /**
     * 忽略切面
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    @Inherited
    @interface IgnoreAspect
    {
        boolean willIgnore() default true;
    }

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
        public boolean init(T current, IConfigData configData, @MayNull Object originObject, Class originClass,
                Method originMethod) throws Exception
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public boolean preInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args, boolean hasInvoked, Object lastReturn) throws Exception
        {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("not Override:url={}",oftenObject.url());
            }
            return false;
        }

        @Override
        public Object doInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args, Object lastReturn) throws Throwable
        {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("not Override:url={}",oftenObject.url());
            }
            return invoker.invoke(args);
        }

        @Override
        public Object afterInvoke(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args, Object lastReturn) throws Exception
        {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("not Override:url={}",oftenObject.url());
            }
            return lastReturn;
        }

        @Override
        public void onException(OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args, Throwable throwable) throws Throwable
        {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("not Override:url={}",oftenObject.url());
            }
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
         * @param current      当为null时、表示为{@linkplain AutoSetObjForAspectOfNormal.AdvancedHandle}类型的切面处理。
         * @param originObject
         * @param originMethod
         * @return true表示添加，false不添加。
         */
        boolean init(@MayNull T current, IConfigData configData, @MayNull Object originObject, Class originClass,
                Method originMethod) throws Exception;

        /**
         * 是否会调用{@linkplain #doInvoke(OftenObject, boolean, Object, Method, Invoker, Object[], Object)}.
         *
         * @param isTop 是否是顶层调用。
         */
        boolean preInvoke(@MayNull OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                boolean hasInvoked,
                Object lastReturn) throws Exception;


        Object doInvoke(@MayNull OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                Object lastReturn) throws Throwable;

        Object afterInvoke(@MayNull OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker,
                Object[] args,
                Object lastReturn) throws Exception;

        void onException(@MayNull OftenObject oftenObject, boolean isTop, Object originObject, Method originMethod,
                Invoker invoker, Object[] args, Throwable throwable) throws Throwable;

    }

    Class<? extends Handle> handle();
}
