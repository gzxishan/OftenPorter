package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.annotation.sth.KeyLockHandle;
import cn.xishan.oftenporter.porter.core.util.ConcurrentKeyLock;

import java.lang.annotation.*;

/**
 * 使用的是{@linkplain ConcurrentKeyLock}加锁.
 *
 * @author Created by https://github.com/CLovinr on 2017/11/14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@AspectFunOperation(handle = KeyLockHandle.class)
public @interface KeyLock
{

    enum LockType
    {
        LOCKS, NECE_LOCKS, UNECE_LOCKS
    }

    enum LockRange
    {
        STATIC, PNAME, CONTEXT, PORTER, FUN
    }

    /**
     * 默认为{LockType.LOCKS, LockType.NECE_LOCKS, LockType.UNECE_LOCKS}
     *
     * @return
     */
    LockType[] types() default {LockType.LOCKS, LockType.NECE_LOCKS, LockType.UNECE_LOCKS};

    /**
     * 锁控制器的实例范围，默认{@linkplain LockRange#CONTEXT}
     *
     * @return
     */
    LockRange range() default LockRange.CONTEXT;

    /**
     * 给每一个加上前缀。
     *
     * @return
     */
    String lockPrefix() default "";

    /**
     * 输入指定的key。
     *
     * @return
     */
    String[] locks() default {};

    /**
     * 获取必须参数key
     *
     * @return
     */
    String[] neceLocks() default {};

    /**
     * 获取非必须参数中不为空的key。
     *
     * @return
     */
    String[] uneceLocks() default {};

}
