//package cn.xishan.oftenporter.oftendb.annotation.tx;
//
///**
// * 事务的传播行为（参考的Spring框架）。
// *
// * @author Created by https://github.com/CLovinr on 2018/7/2.
// */
//public enum Propagation
//{
//
//    /**
//     * 如果当前没有事务，就新建一个事务，如果已存在一个事务中，加入到这个事务中。
//     */
//    REQUIRED,
//
//    /**
//     * 支持当前事务，如果没有当前事务，就以非事务方法执行
//     */
//    SUPPORTS,
//
//    /**
//     * 使用当前事务，如果没有当前事务，就抛出异常。
//     */
//    MANDATORY,
//
//    /**
//     * 新建事务，如果当前存在事务，把当前事务挂起。
//     */
//    REQUIRES_NEW,
//
//
//    /**
//     * 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
//     */
//    NOT_SUPPORTED,
//
//    /**
//     * 以非事务方式执行操作，如果当前事务存在则抛出异常。
//     */
//    NEVER,
//
//    /**
//     * 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与propagation_required类似的操作。
//     */
//    NESTED
//}
