package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.SyncOption;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.util.IdGen;
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

    @PortStart
    public void onStart(){
        LOGGER.debug("[{}] on start!",getClass());
        LOGGER.info("gen id:{}",idGen.nextId());
    }

    @PortIn
    public Object startInvoke(){
        return "From startInvoke!";
    }

    @PortStart
    public void onStart(WObject wObject){
      SyncPorter syncPorter = wObject.newSyncNotInnerPorter(new SyncOption(PortMethod.GET,"startInvoke"));
        LOGGER.debug("[{},{},{}] on start!",getClass(),wObject,syncPorter.requestWNull());
    }
}
