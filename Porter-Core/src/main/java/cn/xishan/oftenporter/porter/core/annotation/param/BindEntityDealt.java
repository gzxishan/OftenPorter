package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface BindEntityDealt
{
    /**
     * 每一个绑定对应一个此handle。
     *
     * @param <T>
     */
    interface IHandle<T>
    {
        /**
         * 初始化,在接口开始({@linkplain PortIn.PortStart})前调用。
         *
         * @param option
         */
        void init(String option, Method method);

        /**
         * 初始化,在接口开始({@linkplain PortIn.PortStart})前调用。
         *
         * @param option
         */
        void init(String option, Class<?> clazz);

        /**
         * 可以返回{@linkplain ParamDealt.FailedReason}.
         *
         * @param porter
         * @param object
         * @return 返回最终对象
         */
        Object deal(WObject wObject, Porter porter, @NotNull T object);

        /**
         * * 可以返回{@linkplain ParamDealt.FailedReason}.
         *
         * @param fun
         * @param object
         * @return 返回最终对象
         */
        Object deal(WObject wObject, PorterOfFun fun, @NotNull T object);
    }

    /**
     * 选项参数
     *
     * @return
     */
    String option() default "";

    /**
     * 处理类。
     *
     * @return
     */
    Class<? extends IHandle> handle();

}
