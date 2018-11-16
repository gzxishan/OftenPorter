package cn.xishan.oftenporter.demo.core.test1;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.advanced.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.bridge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {

        /*
         * 1.PortIn：用于标记web接口 2.设置生成REST
         *
         */

        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");
        LogUtil.printPos(PortMethod.valueOf("GET"));

        localMain.getOnPorterAddListenerAdder().addListener("name1", new OnPorterAddListener()
        {
            @Override
            public boolean onAdding(String contextName, Porter porter)
            {
                logger.warn(":{}  {}",contextName,porter.getClazz());
                return false;
            }

            @Override
            public boolean onSeeking(String contextName, Class<?> clazz)
            {
                return false;
            }
        });

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        // 设置名称
        conf.setContextName("Test1Main");
        // 添加扫描的包（包含子包）
        conf.getSeekPackages()
            .addPackages(Main1.class.getPackage().getName() + ".porter");

        /**
         * 使用当前配置启动一个context
         */
        localMain.startOne(conf);
        logger.debug("****************************************************");

        IBridge bridge = localMain.getBridgeLinker().currentBridge();

        bridge.request(new BridgeRequest("/Test1Main/Hello1/say")
                               .addParam("name", "The Earth").addParam("sth", "class-param"),
                       new BridgeCallback() {

                           @Override
                           public void onResponse(BridgeResponse lResponse) {
                               Object obj = lResponse.getResponse();
                               LogUtil.printPos(obj);
                           }
                       });

        bridge.request(
                new BridgeRequest("/Test1Main/Hello2/say").setMethod(PortMethod.POST)
                                                     .addParam("name", "The Moon").addParam("msg", "beauty"),
                new BridgeCallback() {

                    @Override
                    public void onResponse(BridgeResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        LogUtil.printPos(obj);
                    }
                });

        bridge.request(new BridgeRequest("/Test1Main/Hello3REST/123456")
                               .setMethod(PortMethod.POST).addParam("name", "The Sun")
                               .addParam("msg", "beauty"), new BridgeCallback() {

            @Override
            public void onResponse(BridgeResponse lResponse) {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            }
        });

        bridge.request(new BridgeRequest("/Test1Main/Hello4REST/abcdef")
                               .setMethod(PortMethod.POST).addParam("name", "The Mars"),
                       new BridgeCallback() {

                           @Override
                           public void onResponse(BridgeResponse lResponse) {
                               Object obj = lResponse.getResponse();
                               LogUtil.printPos(obj);
                           }
                       });
        bridge.request(
                new BridgeRequest("/Test1Main/Hello4REST/add")
                        .addParam("content", "!!!!").setMethod(PortMethod.POST),
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
