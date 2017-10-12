package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface AspectFunOperation
{
    public static interface Handle
    {

        /**
         * 返回false，表示不进行操作。
         * @param porterOfFun
         * @return
         */
        boolean initWith(PorterOfFun porterOfFun);

        /**
         *
         * @param wObject
         * @param lastReturn 上一个处理返回的对象。
         * @return
         * @throws Exception
         */
        Object invoke(WObject wObject,Object lastReturn)throws Exception;

        OutType getOutType();
    }

    Class<? extends Handle> handle();
}
