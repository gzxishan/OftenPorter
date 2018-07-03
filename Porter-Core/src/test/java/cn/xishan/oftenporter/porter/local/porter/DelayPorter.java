package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/29.
 */
@PortIn("Delay")
public class DelayPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayPorter.class);
    @AutoSet
    Random random;

    public DelayPorter()
    {
        LOGGER.debug("***new " + getClass().getSimpleName() + "***");
    }

    @PortIn("${fun.test}")
    public Object test()
    {
        return random.nextInt();
    }
}
