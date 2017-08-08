package cn.xishan.oftenporter.porter.local.porter3;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortOut;
import cn.xishan.oftenporter.porter.core.base.OutType;

/**
 * Created by https://github.com/CLovinr on 2017/8/8.
 */
@PortIn
@PortOut(OutType.VoidReturn)
public class TestDefaultReturnPorter {

    @PortIn
    @PortOut(OutType.VoidReturn)
    public void testVoid() {

    }

    @PortIn
    @PortOut(OutType.NullReturn)
    public Object testNull() {
        return null;
    }

    @PortIn
    public void testCVoid(){

    }
}
