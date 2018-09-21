package cn.xishan.oftenporter.demo.testmem.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */

@PortIn
public class MemPorter
{
    @AutoSet
    MemUnit memUnit;

    @PortIn.PortStart
    public void onStart(){
        memUnit.test();
    }
}
