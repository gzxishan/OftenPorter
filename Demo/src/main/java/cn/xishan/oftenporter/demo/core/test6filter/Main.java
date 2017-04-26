package cn.xishan.oftenporter.demo.core.test6filter;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/25.
 */
public class Main
{
    public static void main(String[] args)
    {
        /**
         * 1.测试{@linkplain cn.xishan.oftenporter.porter.core.annotation.Mixin},自己混入自己
         */
        final Logger logger = LoggerFactory.getLogger(Main.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("MainFilter");
        conf.getSeekPackages().addPorters(PackageUtil.getPackageWithRelative(Main.class, "porter", "."));
        localMain.startOne(conf);
        logger.debug("****************************************************");


        logger.debug("****************************************************");
        localMain.destroyAll();

    }
}
