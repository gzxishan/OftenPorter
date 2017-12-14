package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;

/**
 * 注解在类上时，无法控制混入接口。
 *
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface AspectFunOperation
{

    class HandleAdapter<T extends Annotation> implements Handle<T>
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(HandleAdapter.class);

        @Override
        public boolean init(T current, Porter porter)
        {
            LOGGER.debug("not Override.");
            return false;
        }

        @Override
        public boolean init(T current, PorterOfFun porterOfFun)
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
            return porterOfFun.invoke(wObject,null);
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
    }

    /**
     * 执行的顺序:{@linkplain #beforeInvokeOfMethodCheck(WObject, PorterOfFun)}--{@linkplain #beforeInvoke(WObject, PorterOfFun)}--{@linkplain #invoke(WObject, PorterOfFun, Object)}--{@linkplain #afterInvoke(WObject, PorterOfFun, Object)}--{@linkplain #onFinal(WObject, PorterOfFun, Object, Object)}
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
        boolean init(T current, Porter porter);

        /**
         * 注解在函数上的。
         * 返回false，表示忽略当前注解。
         *
         * @param porterOfFun
         * @return
         */
        boolean init(T current, PorterOfFun porterOfFun);

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
    }

    Class<? extends Handle> handle();
}
