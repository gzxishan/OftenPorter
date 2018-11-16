package cn.xishan.oftenporter.demo.testmem;

import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */
public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        while (true){
            test();
            Thread.sleep(20);
        }
    }

    static void test()
    {
        LocalMain localMain = new LocalMain(true, new BridgeName("P"), "utf-8");

        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setContextName("OP");

        porterConf.getSeekPackages().addPackages(PackageUtil.getPackageWithRelative(Main.class, "./porter", '.'));

        localMain.startOne(porterConf);
        localMain.destroyOne(porterConf.getContextName());
    }
}
