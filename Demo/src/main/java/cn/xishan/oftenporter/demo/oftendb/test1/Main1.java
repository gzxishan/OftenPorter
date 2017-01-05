package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PResponse;
import cn.xishan.oftenporter.porter.local.LocalMain;

public class Main1 {

    public static void main(String[] args) {
        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setContextName("T1");
        porterConf.getSeekPackages()
                  .addPorters(Main1.class.getPackage().getName() + ".porter");

        localMain.startOne(porterConf);
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        PBridge bridge = localMain.getPLinker().currentBridge();

        bridge.request(new PRequest(PortMethod.POST, "/T1/Hello1/add")
                               .addParam("name", "小明").addParam("age", "21")
                               .addParam("sex", "男"), new PCallback() {

            @Override
            public void onResponse(PResponse lResponse) {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            }
        });
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/count")
                               .addParam("name", "小明"), new PCallback() {

            @Override
            public void onResponse(PResponse lResponse) {
                logger.debug(lResponse.toString());
            }
        });
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/update")
                               .addParam("name", "小明"), new PCallback() {

            @Override
            public void onResponse(PResponse lResponse) {
                logger.debug(lResponse.toString());
            }
        });
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/del")
                               .addParam("name", "小明1"), new PCallback() {

            @Override
            public void onResponse(PResponse lResponse) {
                logger.debug(lResponse.toString());
            }
        });
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/list"),
                       new PCallback() {

                           @Override
                           public void onResponse(PResponse lResponse) {
                               logger.debug(lResponse.toString());
                           }
                       });
        localMain.destroyAll();

    }

}
