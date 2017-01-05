package cn.xishan.oftenporter.demo.core.test1;

import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.demo.core.test1.check.GlobalCheckPassable;
import cn.xishan.oftenporter.demo.core.test1.porter.Hello5Porter;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main2 {

    public static void main(String[] args) {
        /**
         * 1.CheckPassable:用于认证检测
         */

        final Logger logger = LoggerFactory.getLogger(Main1.class);

        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");

        // 进行配置
        PorterConf conf = localMain.newPorterConf();
        // 设置名称
        conf.setContextName("Test1-2Main");
        // 添加接口类
        conf.getSeekPackages().addClassPorter(Hello5Porter.class);
        // 设置全局检测
        conf.addContextCheck(new GlobalCheckPassable());

        localMain.startOne(conf);
        logger.debug("****************************************************");

        localMain.getPLinker().currentBridge().request(
                new PRequest("/Test1-2Main/Hello5/say"), new PCallback() {

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
