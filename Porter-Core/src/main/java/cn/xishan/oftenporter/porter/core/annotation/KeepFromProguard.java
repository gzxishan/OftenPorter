package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于标记不用混淆名称的、需要保留的部分。
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Documented
public @interface KeepFromProguard
{
}
