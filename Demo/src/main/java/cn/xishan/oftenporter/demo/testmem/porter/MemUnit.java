package cn.xishan.oftenporter.demo.testmem.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.util.LogMethodInvoke;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */
public class MemUnit
{
    @AutoSet
    Obj1 obj1;

    @LogMethodInvoke
    public void test(){
        testInner();
    }

    @LogMethodInvoke
     void testInner(){
        System.out.println("..................."+obj1);
    }
}
