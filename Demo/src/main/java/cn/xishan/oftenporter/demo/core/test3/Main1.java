package cn.xishan.oftenporter.demo.core.test3;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {
        /*
         * <pre>
         * 1.PortStart:用于在框架Context启动时调用
         * 2.PortDestroy:用于在框架Context关闭时调用
         * </pre>
         */

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("Test3Main");
        conf.getSeekPackages()
            .addPorters(Main1.class.getPackage().getName() + ".porter");

        localMain.startOne(conf);

        localMain.destroyAll();
    }

}
