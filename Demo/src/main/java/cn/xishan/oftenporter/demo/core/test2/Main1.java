package cn.xishan.oftenporter.demo.core.test2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {

        /*
         * 1.PortOut:用于标记输出类型
         * 2.所有时期的{@linkplain cn.xishan.oftenporter.porter.core.base.CheckPassable}
         */
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();

        conf.addForAllCheckPassable((wObject, type, checkHandle) -> {
            logger.debug("1:"+type.name());
            checkHandle.next();
        });

        conf.addForAllCheckPassable((wObject, type, checkHandle) -> {
            logger.debug("2:"+type.name());
            checkHandle.next();
        });

        conf.setContextName("Test2Main");
        conf.getSeekPackages()
            .addPorters(Main1.class.getPackage().getName() + ".porter");

        PBridge bridge = localMain.getPLinker().currentBridge();
        localMain.startOne(conf);
        logger.debug("****************************************************");
        bridge.request(
                new PRequest("/Test2Main/Hello1/say").addParam("name", "火星人"),
                new PCallback() {

                    @Override
                    public void onResponse(PResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        logger.debug("{}",obj);
                    }
                });

        bridge.request(
                new PRequest("/Test2Main/Hello1/say2").addParam("name", "火星人"),
                new PCallback() {

                    @Override
                    public void onResponse(PResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        logger.debug("{}",obj);
                    }
                });
        logger.debug("****************************************************");
        localMain.destroyAll();
    }

}
