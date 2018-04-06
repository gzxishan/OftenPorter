package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MixinOnly;
import cn.xishan.oftenporter.porter.core.annotation.MixinTo;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.porter.HelloPorter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/20.
 */
@PortIn
@MixinTo(porter = HelloPorter.class)
public class HelloMixinToPorter
{

    @PortStart
    public void start()
    {
        LogUtil.printErrPos();
    }

    @PortIn("helloMixinTo")
    public String helloMixinTo(WObject wObject)
    {
        return "MixinTo!";
    }
}
