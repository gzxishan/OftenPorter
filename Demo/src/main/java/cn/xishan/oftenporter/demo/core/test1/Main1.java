package cn.xishan.oftenporter.demo.core.test1;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.base.OnPorterAddListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {

        /**
         * 1.PortIn：用于标记web接口 2.设置生成REST
         *
         */

        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");
        LogUtil.printPos(PortMethod.valueOf("GET"));

        localMain.getOnPorterAddListenerAdder().add("name1", new OnPorterAddListener()
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
            .addPorters(Main1.class.getPackage().getName() + ".porter");

        /**
         * 使用当前配置启动一个context
         */
        localMain.startOne(conf);
        logger.debug("****************************************************");

        PBridge bridge = localMain.getPLinker().currentBridge();

        bridge.request(new PRequest("/Test1Main/Hello1/say")
                               .addParam("name", "The Earth").addParam("sth", "class-param"),
                       new PCallback() {

                           @Override
                           public void onResponse(PResponse lResponse) {
                               Object obj = lResponse.getResponse();
                               LogUtil.printPos(obj);
                           }
                       });

        bridge.request(
                new PRequest("/Test1Main/Hello2/say").setMethod(PortMethod.POST)
                                                     .addParam("name", "The Moon").addParam("msg", "beauty"),
                new PCallback() {

                    @Override
                    public void onResponse(PResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        LogUtil.printPos(obj);
                    }
                });

        bridge.request(new PRequest("/Test1Main/Hello3REST/123456")
                               .setMethod(PortMethod.POST).addParam("name", "The Sun")
                               .addParam("msg", "beauty"), new PCallback() {

            @Override
            public void onResponse(PResponse lResponse) {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            }
        });

        bridge.request(new PRequest("/Test1Main/Hello4REST/abcdef")
                               .setMethod(PortMethod.POST).addParam("name", "The Mars"),
                       new PCallback() {

                           @Override
                           public void onResponse(PResponse lResponse) {
                               Object obj = lResponse.getResponse();
                               LogUtil.printPos(obj);
                           }
                       });
        bridge.request(
                new PRequest("/Test1Main/Hello4REST/add")
                        .addParam("content", "!!!!").setMethod(PortMethod.POST),
                new PCallback() {

                    @Override
                    public void onResponse(PResponse lResponse) {
                        Object obj = lResponse.getResponse();
                        LogUtil.printPos(obj);
                    }
                });
        logger.debug("****************************************************");
        localMain.destroyAll();
    }

}
