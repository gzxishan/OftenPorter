package cn.xishan.oftenporter.demo.testmem.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */

@PortIn
public class MemPorter
{
    @AutoSet
    MemUnit memUnit;

    @PortStart
    public void onStart(){
        memUnit.test();
    }
}
