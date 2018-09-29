package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;

/**
 * 注解在注解上,最终对PortIn接口函数进行处理。
 * <ol>
 * <li>被修饰的注解，可以注解在函数或类上。</li>
 * <li>被修饰的注解、且注解在类上的，所有接口函数都将加上该注解。</li>
 * <li>
 * {@linkplain Handle#init(Annotation, IConfigData, Porter) init(Annotation, IConfigData, Porter)}和
 * {@linkplain Handle#init(Annotation, IConfigData, PorterOfFun) init(Annotation, IConfigData, PorterOfFun)}被调用时，
 * {@linkplain AutoSet}并没有被执行、而是在{@linkplain Handle#onStart(WObject)}时已经被执行。
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
@Advancable(enableAdvancedAnnotation = true)
public @interface AspectOperationOfPortIn
{

    class HandleAdapter<T extends Annotation> implements Handle<T>
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(HandleAdapter.class);

        @Override
        public boolean init(T current, IConfigData configData, Porter porter)
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public boolean init(T current, IConfigData configData, PorterOfFun porterOfFun)
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public void onStart(WObject wObject)
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public void onDestroy()
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public void beforeInvokeOfMethodCheck(WObject wObject, PorterOfFun porterOfFun)
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public void beforeInvoke(WObject wObject, PorterOfFun porterOfFun)
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public Object invoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
        {
            LOGGER.debug("default invoke.");
            return porterOfFun.invokeByHandleArgs(wObject, lastReturn);
        }

        @Override
        public void afterInvoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn)
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public void onFinal(WObject wObject, PorterOfFun porterOfFun, Object lastReturn, Object failedObject)
        {
            LOGGER.debug("not Override.");
        }

        @Override
        public OutType getOutType()
        {
            LOGGER.debug("not Override.");
            return null;
        }

        @Override
        public PortFunType getPortFunType()
        {
            LOGGER.debug("not Override.");
            return null;
        }

        @Override
        public TiedType getTiedType()
        {
            LOGGER.debug("not Override.");
            return null;
        }

        @Override
        public PortMethod[] getMethods()
        {
            return null;
        }

        @Override
        public boolean needInvoke(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn)
        {
            return true;
        }
    }

    /**
     * 执行的顺序:{@linkplain #beforeInvokeOfMethodCheck(WObject, PorterOfFun)}--
     * {@linkplain #beforeInvoke(WObject, PorterOfFun)}--{@linkplain #invoke(WObject, PorterOfFun, Object)}--
     * {@linkplain #afterInvoke(WObject, PorterOfFun, Object)}--
     * {@linkplain #onFinal(WObject, PorterOfFun, Object, Object)}
     * <p>
     * 其中除了{@linkplain #afterInvoke(WObject, PorterOfFun, Object)}与
     * {@linkplain #onFinal(WObject, PorterOfFun, Object, Object)}是逆序调用，其余的是顺序调用。
     * </p>
     *
     * @param <T>
     */
    interface Handle<T extends Annotation>
    {

        /**
         * 注解在类上的。
         * 返回false，表示忽略当前注解。
         *
         * @param porter
         * @return
         */
        boolean init(T current, IConfigData configData, Porter porter);

        /**
         * 注解在函数上的。
         * 返回false，表示忽略当前注解。
         *
         * @param porterOfFun
         * @return
         */
        boolean init(T current, IConfigData configData, PorterOfFun porterOfFun);

        void onStart(WObject wObject);

        void onDestroy();

        /**
         * 在函数上的检测{@linkplain CheckPassable}执行之前。
         *
         * @param wObject
         * @param porterOfFun
         */
        void beforeInvokeOfMethodCheck(WObject wObject, PorterOfFun porterOfFun);

        /**
         * 在函数执行之前。
         *
         * @param wObject
         * @param porterOfFun
         */
        void beforeInvoke(WObject wObject, PorterOfFun porterOfFun);

        /**
         * 调用函数时触发，且注解在类上的先调用。
         *
         * @param wObject
         * @param lastReturn 上一个处理返回的对象。
         * @return
         * @throws Exception
         */
        Object invoke(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn) throws Exception;


        /**
         * 函数执行完成后.
         *
         * @param wObject
         * @param porterOfFun
         */
        void afterInvoke(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn);


        /**
         * 函数上的检测执行完成或执行错误后都会调用,但在响应之前调用.
         *
         * @param wObject
         * @param porterOfFun
         */
        void onFinal(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn,
                @MayNull Object failedObject);

        /**
         * 修改类或函数的输出类型。
         *
         * @return
         */
        @MayNull
        OutType getOutType();

        /**
         * 修改类或函数的。
         *
         * @return
         */
        @MayNull
        PortFunType getPortFunType();

        @MayNull
        TiedType getTiedType();

        @MayNull
        PortMethod[] getMethods();

        /**
         * 是否需要调用{@linkplain #invoke(WObject, PorterOfFun, Object)}
         *
         * @param wObject
         * @return
         */
        boolean needInvoke(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn);
    }

    Class<? extends Handle> handle();
}
