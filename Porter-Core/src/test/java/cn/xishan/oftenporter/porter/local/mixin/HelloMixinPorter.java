package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/20.
 */
@PortIn
public class HelloMixinPorter
{
    @AutoSet("P1")
    Delivery delivery;

    @PortStart
    public void start()
    {
        LogUtil.printErrPos();
    }

    AtomicInteger atomicInteger = new AtomicInteger();
    @PortIn("helloMixin")
    public String helloMixin(WObject wObject)
    {

        wObject.currentRequest("testCurrent", null, lResponse ->
        {
            if (atomicInteger.incrementAndGet() % 50000 == 1)
            {
                LogUtil.printErrPosLn(lResponse, ":", atomicInteger.get());
                delivery.currentBridge().request(new PRequest(
                                "/" + wObject.url().contextName() + "/" + wObject.url().classTied() + "/testDelivery"),
                        lResponse1 -> LogUtil.printErrPosLn(lResponse1));
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
