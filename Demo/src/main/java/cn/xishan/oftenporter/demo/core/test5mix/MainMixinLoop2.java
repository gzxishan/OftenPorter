package cn.xishan.oftenporter.demo.core.test5mix;

import cn.xishan.oftenporter.demo.core.test5mix.mixinloop.Root2Porter;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class MainMixinLoop2
{
    public static void main(String[] args) {
        /**
         * 1.测试{@linkplain cn.xishan.oftenporter.porter.core.annotation.Mixin},自己混入自己
         */
        final Logger logger = LoggerFactory.getLogger(MainMixinLoop2.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("MainMixinLoop2");
        conf.getSeekPackages().addClassPorter(Root2Porter.class);
        localMain.startOne(conf);
        logger.debug("****************************************************");


        logger.debug("****************************************************");
        localMain.destroyAll();

    }
}
