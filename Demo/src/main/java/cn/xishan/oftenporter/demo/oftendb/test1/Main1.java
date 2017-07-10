package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.demo.oftendb.base.SqlDBSource;
import cn.xishan.oftenporter.oftendb.data.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.local.LocalMain;

import java.util.Random;

public class Main1
{

    public static void main(String[] args)
    {
        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.addContextAutoSet(DBSource.class,new SqlDBSource());
        porterConf.setContextName("T1");
        porterConf.getSeekPackages()
                .addPorters(Main1.class.getPackage().getName() + ".porter");
        porterConf.addForAllCheckPassable(DBCommon.autoTransaction(new TransactionConfirm()
        {
            @Override
            public boolean needTransaction(WObject wObject, DuringType type, CheckHandle checkHandle)
            {
                return true;
            }

            @Override
            public TConfig getTConfig(WObject wObject, DuringType type, CheckHandle checkHandle)
            {
                TConfig tConfig = new TConfig();
                tConfig.dbSource=new SqlDBSource();
                return tConfig;
            }
        }));

        localMain.startOne(porterConf);
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        PBridge bridge = localMain.getPLinker().currentBridge();

        for (int i = 0; i < 1; i++)
        {
            bridge.request(new PRequest(PortMethod.POST, "/T1/Hello1/add")
                    .addParam("name", "小明-" + (new Random().nextInt(3))).addParam("age", "21")
                    .addParam("sex", "男"), lResponse ->
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            });
        }
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/count")
                .addParam("name", "小明-1"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.POST, "/T1/Hello1/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.PUT, "/T1/Hello1/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.POST, "/T1/Hello1/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.PUT, "/T1/Hello1/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/del")
                .addParam("name", "小明-10"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/testJBatis"),null);


        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/list"),
                lResponse -> logger.debug(lResponse.toString()));

        ///////////////
        bridge.request(
                new PRequest(PortMethod.GET, "/T1/Hello1/transactionOk")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/list"),
                lResponse -> logger.debug(lResponse.toString()));

        /////////////
        bridge.request(
                new PRequest(PortMethod.GET, "/T1/Hello1/transactionFailed")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/list"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello1/clear"),
                lResponse -> logger.debug(lResponse.toString()));

        localMain.destroyAll();

        Object as = new String[]{"1","2","3"};

        logger.debug("array={}",as);

    }

}
