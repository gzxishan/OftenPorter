package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/2.
 */
@PortIn("Start")
public class StartPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StartPorter.class);
    @PortStart
    public void onStart(){
        LOGGER.debug("[{}] on start!",getClass());
    }
}
