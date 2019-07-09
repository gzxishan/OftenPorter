package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.SyncOption;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.sysset.PorterSync;
import cn.xishan.oftenporter.porter.core.util.IdGen;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/2.
 */
@PortIn("Start")
public class StartPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StartPorter.class);
    @AutoSet
    IdGen idGen;
    String bridgeName;



    @PortIn
    public Object startInvoke()
    {
        return "From startInvoke!";
    }

    @PortStart(order = 1)
    public void onStart(OftenObject oftenObject,@Property("bridgeName") String bridgeName)
    {
        this.bridgeName=bridgeName;
        PorterSync porterSync = oftenObject.newSyncNotInnerPorter(new SyncOption(PortMethod.GET, "startInvoke"));
        LOGGER.debug("[{},{},{}] on start!", getClass(), oftenObject, porterSync.invokeWithNameValues(null));
    }

    @PortStart(order = 2)
    public void onStart()
    {
        LOGGER.debug("[{}] on start!", getClass());
        LOGGER.info("gen id:{}", idGen.nextId());
        Assert.assertEquals("P1",bridgeName);
    }
}
