package cn.xishan.oftenporter.demo.core.test6filter.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/25.
 */
@PortIn
public class A
{
    @PortIn
    @PortIn.Filter(
            before = {
                    @PortIn.Before(funTied = "b"),
                    @PortIn.Before(funTied = "c"),
                    @PortIn.Before(funTied = "a")
            })
    public void a()
    {
        LogUtil.printErrPos();

    }

    @PortIn
    public void b()
    {
        LogUtil.printErrPos();
    }

    @PortIn
    public void c()
    {
        LogUtil.printErrPos();
    }
}
