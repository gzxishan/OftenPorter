package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * 从@{@linkplain PortIn}类的@{@linkplain OnPorterEntities}上获取对象绑定，且可以获取子类，但优先选择更亲的类。
 * <pre>
 *     如:
 *     class A{}
 *     class B extends A{};
 *     class FatherPorter{
 *          &#64;PortIn
 *          &#64;BindEntities({SomeClass.class})
 *          &#64;FromPorterEntities({A.class})
 *          public   void fun(WObject wObject){
 *              SomeClass mSomeClass = wObject.fentity(0);
 *              A a = wObject.fentity(1);//对于ChildPorter,具体实例是B;对于Child2Porter,具体实例是A;
 *          }
 *     }
 *
 *     &#64;BindEntities({B.class})
 *     class ChildPorter extends FatherPorter{
 *
 *
 *     }
 *
 *      &#64;BindEntities({A.class,B.class})
 *     class Child2Porter extends FatherPorter{
 *
 *
 *     }
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public @interface FromPorterEntities
{
    Class<?>[] value() default {};
}
