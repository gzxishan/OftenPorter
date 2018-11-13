package cn.xishan.oftenporter.demo.core.test5mix;

import cn.xishan.oftenporter.demo.core.test5mix.mixinloop.Root4Porter;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class MainMixinLoop4
{
    public static void main(String[] args) {
        /*
         * 1.测试{@linkplain cn.xishan.oftenporter.porter.core.annotation.Mixin},自己混入自己
         */
        final Logger logger = LoggerFactory.getLogger(MainMixinLoop4.class);

        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("MainMixinLoop4");
        conf.getSeekPackages().addClassPorter(Root4Porter.class);
        localMain.startOne(conf);
        logger.debug("****************************************************");


        logger.debug("****************************************************");
        localMain.destroyAll();

    }
}
