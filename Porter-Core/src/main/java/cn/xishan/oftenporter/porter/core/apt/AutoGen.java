package cn.xishan.oftenporter.porter.core.apt;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 加在接口上，可用于自动生成代码(当{@linkplain #value()}和{@linkplain #classValue()}都为默认值时),新生成的类与接口在同一包，名字为接口名加"AP"结尾。加注解{@linkplain
 * PortInObj.Nece
 * }或不加任何注解表示必需值，加{@linkplain PortInObj.UnNece}表示非必需值。
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AutoGen
{
    /**
     * 不为""的情况下，根据名称查找在{@linkplain PorterConf#getContextAutoGenImplMap()} (String, Class)}中添加的实现类
     *
     * @return
     */
    String value() default "";

    /**
     * 做为{@linkplain #value()}的补充，当其不为默认值时，则使用Class.getName()作为查找实现类的name。
     *
     * @return
     */
    Class<?> classValue() default AutoGen.class;
}
