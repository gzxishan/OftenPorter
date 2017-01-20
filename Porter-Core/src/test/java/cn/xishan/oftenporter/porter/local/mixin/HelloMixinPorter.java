package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/20.
 */
@PortIn
public class HelloMixinPorter
{
    @PortStart
    public void start(){
        LogUtil.printErrPos();
    }


    @PortIn("helloMixin")
    public String helloMixin(WObject wObject){
        AtomicInteger atomicInteger = new AtomicInteger();
        wObject.currentRequest("testCurrent", null, new PCallback()
        {
            @Override
            public void onResponse(PResponse lResponse)
            {
                if(atomicInteger.incrementAndGet()%1000==0){
                    LogUtil.printErrPosLn(lResponse,":",atomicInteger.get());
                }
            }
        });
        return "Mixin!";
    }

    @PortIn("testCurrent")
    public String testCurrent(){
        return "testCurrent!";
    }
}
