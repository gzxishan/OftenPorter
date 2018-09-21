package cn.xishan.oftenporter.porter.local.proxy;

import cn.xishan.oftenporter.porter.core.util.LogMethodInvoke;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.Random;

/**
 * @author Created by https://github.com/CLovinr on 2018-09-21.
 */
public class ProxyUnit
{
    final Random random;

    public ProxyUnit()
    {
        this(null);
    }

    public ProxyUnit(Random random)
    {
        this.random = random;
    }

    @LogMethodInvoke
    public void test()
    {
        LogUtil.printPosLn("..........................................:", random.nextInt());
    }
}
