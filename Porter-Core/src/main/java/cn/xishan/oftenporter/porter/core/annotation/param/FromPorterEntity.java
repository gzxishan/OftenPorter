package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * 从@{@linkplain PortIn}类的@{@linkplain OnPorterEntity}上获取对象绑定，且可以获取子类，但优先选择更亲的类。
 * <pre>
 *     如:
 *     class A{}
 *     class B extends A{};
 *     class FatherPorter{
 *          &#64;PortIn
 *          &#64;PortInObj({SomeClass.class})
 *          &#64;PortInObj.FromPorter({A.class})
 *          public   void fun(WObject wObject){
 *              SomeClass mSomeClass = wObject.finObject(0);
 *              A a = wObject.finObject(1);//对于ChildPorter,具体实例是B;对于Child2Porter,具体实例是A;
 *          }
 *     }
 *
 *     &#64;PortInObj({B.class})
 *     class ChildPorter extends FatherPorter{
 *
 *
 *     }
 *
 *      &#64;PortInObj({A.class,B.class})
 *     class Child2Porter extends FatherPorter{
 *
 *
 *     }
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public @interface FromPorterEntity
{
    Class<?>[] value() default {};
}
