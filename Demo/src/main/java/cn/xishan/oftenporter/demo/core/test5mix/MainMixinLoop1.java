package cn.xishan.oftenporter.demo.core.test5mix;

import cn.xishan.oftenporter.demo.core.test5mix.mixinloop.RootPorter;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
public class MainMixinLoop1
{
    public static void main(String[] args) {
        /*
         * 1.测试{@linkplain cn.xishan.oftenporter.porter.core.annotation.Mixin},自己混入自己
         */
        final Logger logger = LoggerFactory.getLogger(MainMixinLoop1.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("MainMixinLoop1");
        conf.getSeekPackages().addClassPorter(RootPorter.class);
        localMain.startOne(conf);
        logger.debug("****************************************************");


        logger.debug("****************************************************");
        localMain.destroyAll();

    }
}
