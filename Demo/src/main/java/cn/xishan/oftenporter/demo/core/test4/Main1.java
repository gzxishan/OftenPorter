package cn.xishan.oftenporter.demo.core.test4;

import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.demo.core.test4.porter.Hello1Porter;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {
        /*
         * 1.Parser:转换数据类型,简单使用。
         */
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        conf.setContextName("Test4Main");
        conf.getSeekPackages().addObjectPorter(new Hello1Porter());

        IBridge bridge = localMain.getBridgeLinker().currentBridge();
        localMain.startOne(conf);
        logger.debug("****************************************************");
        bridge.request(new BridgeRequest("/Test4Main/Hello1/say")
                               .setMethod(PortMethod.POST).addParam("age", "20"),
                       new BridgeCallback() {

                           @Override
                           public void onResponse(BridgeResponse lResponse) {
                               Object obj = lResponse.getResponse();
                               LogUtil.printPos(obj);
                           }
                       });

        bridge.request(
                new BridgeRequest("/Test4Main/Hello1/say2")
                        .setMethod(PortMethod.POST).addParam("age", "500"),
                new BridgeCallback() {

                    @Override
                    public void onResponse(BridgeResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        LogUtil.printPos(obj);
                    }
                });
        bridge.request(
                new BridgeRequest("/Test4Main/Hello1/say3")
                        .setMethod(PortMethod.POST).addParam("age", "300"),
                new BridgeCallback() {

                    @Override
                    public void onResponse(BridgeResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        LogUtil.printPos(obj);
                    }
                });

        logger.debug("****************************************************");
        localMain.destroyAll();

    }

}
