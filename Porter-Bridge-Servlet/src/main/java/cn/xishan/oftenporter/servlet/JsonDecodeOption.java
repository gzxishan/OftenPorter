package cn.xishan.oftenporter.servlet;

import java.lang.annotation.*;

/**
 * 单独解析处理。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface JsonDecodeOption {
    enum Type {
        /**
         * 不论ContentType是什么，忽略解析。
         */
        Ignore,
        /**
         * 不论ContentType是什么，强制按json将进行解析。
         */
        Force,
        /**
         * 当ContentType为{@linkplain ContentType#APP_JSON}时，进行解析。
         */
        Auto,
    }

    /**
     * 解析类型。
     */
    Type decodeType() default Type.Auto;
}
