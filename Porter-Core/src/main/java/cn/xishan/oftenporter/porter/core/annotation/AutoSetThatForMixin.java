package cn.xishan.oftenporter.porter.core.annotation;


import java.lang.annotation.*;

/**
 * 用于接收对方传过来的变量，默认接收混入者接口实例,
 * 当{@linkplain #value()}不为默认值且{@linkplain #searchChild()}为真时、若没有找到该类型变量、则会尝试查找传过来的子类型变量。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetThatForMixin
{
    Class<?> value() default AutoSet.class;

    String key() default "";

    boolean required() default true;

    /**
     * 是否在指定了{@linkplain #value()}类型且没有找到直接匹配项时、查找所有传过来的变量中的子类型。
     * @return
     */
    boolean searchChild()default true;
}
