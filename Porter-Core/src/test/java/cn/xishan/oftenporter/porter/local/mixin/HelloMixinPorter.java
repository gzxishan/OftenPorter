package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MixinOnly;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/20.
 */
@PortIn
@MixinOnly
public class HelloMixinPorter
{
    @AutoSet("P1")
    Delivery delivery;

    @PortStart
    public void start()
    {
        LogUtil.printPos();
    }

    AtomicInteger atomicInteger = new AtomicInteger();
    @PortIn("helloMixin")
    public String helloMixin(OftenObject oftenObject)
    {

        oftenObject.innerRequest("testCurrent", null, lResponse ->
        {
            if (atomicInteger.incrementAndGet() % 50000 == 1)
            {
                LogUtil.printPosLn(lResponse, ":", atomicInteger.get());
                delivery.currentBridge().request(new BridgeRequest(PortMethod.GET,
                                "/" + oftenObject.url().contextName() + "/" + oftenObject.url().classTied() + "/testDelivery"),
                        lResponse1 -> LogUtil.printPosLn(lResponse1));
            }
        });
        return "Mixin!";
    }

    @PortIn("testDelivery")
    public String testDelivery()
    {
        return "testDelivery!";
    }

    @PortIn("testCurrent")
    public String testCurrent()
    {
        return "testCurrent!";
    }
}
