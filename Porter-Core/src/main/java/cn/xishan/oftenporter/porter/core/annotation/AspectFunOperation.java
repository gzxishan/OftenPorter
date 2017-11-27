package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;

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
    /**
     * @param <T>
     */
    public static interface Handle<T extends Annotation>
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
         * 调用函数时触发，且注解在类上的先调用。
         *
         * @param wObject
         * @param lastReturn 上一个处理返回的对象。
         * @return
         * @throws Exception
         */
        Object invokeMethod(WObject wObject, PorterOfFun porterOfFun, @MayNull Object lastReturn) throws Exception;

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
