package cn.xishan.oftenporter.demo.core.test2;

import cn.xishan.oftenporter.porter.core.bridge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {

        /*
         * 1.PortOut:用于标记输出类型
         * 2.所有时期的{@linkplain cn.xishan.oftenporter.porter.core.base.CheckPassable}
         */
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();

        conf.addPorterCheck((wObject, type, checkHandle) -> {
            logger.debug("1:"+type.name());
            checkHandle.next();
        });

        conf.addPorterCheck((wObject, type, checkHandle) -> {
            logger.debug("2:"+type.name());
            checkHandle.next();
        });

        conf.setContextName("Test2Main");
        conf.getSeekPackages()
            .addPorters(Main1.class.getPackage().getName() + ".porter");

        IBridge bridge = localMain.getBridgeLinker().currentBridge();
        localMain.startOne(conf);
        logger.debug("****************************************************");
        bridge.request(
                new BridgeRequest("/Test2Main/Hello1/say").addParam("name", "火星人"),
                new BridgeCallback() {

                    @Override
                    public void onResponse(BridgeResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        logger.debug("{}",obj);
                    }
                });

        bridge.request(
                new BridgeRequest("/Test2Main/Hello1/say2").addParam("name", "火星人"),
                new BridgeCallback() {

                    @Override
                    public void onResponse(BridgeResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        logger.debug("{}",obj);
                    }
                });
        logger.debug("****************************************************");
        localMain.destroyAll();
    }

}
