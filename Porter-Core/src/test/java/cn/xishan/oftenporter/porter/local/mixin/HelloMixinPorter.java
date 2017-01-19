package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

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
    public String helloMixin(){
        return "Mixin!";
    }
}
