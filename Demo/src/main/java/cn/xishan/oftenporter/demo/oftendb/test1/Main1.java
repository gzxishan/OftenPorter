package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.IBridge;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.local.LocalMain;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Main1
{

    static LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");

    public static void main(String[] args)
    {
        Thread thread = new Thread(Main1::init);
        thread.setDaemon(true);
        thread.start();
        try
        {
            Thread.sleep(300 * 1000);
            localMain.destroyAll();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    static void init()
    {
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setContextName("T1");
        porterConf.getSeekPackages()
                .addPackages(Main1.class.getPackage().getName() + ".porter");

        {
            MyBatisOption myBatisOption = new MyBatisOption(null,"/oftendb/test1mapper/", true);
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:~/PorterDemo/oftendb2;MODE=MySQL");
            jdbcDataSource.setUser("sa");
            jdbcDataSource.setPassword("");
            myBatisOption.dataSourceObject = jdbcDataSource;
            myBatisOption.resourcesDir = new File("Demo/src/main/resources").getAbsolutePath().replace("\\", "/");
            myBatisOption.mybatisStateListener = new MyBatisOption.IMybatisStateListener()
            {
                @Override
                public void onStart()
                {

                }

                @Override
                public void onDestroy()
                {

                }

                @Override
                public void beforeReload()
                {

                }

                @Override
                public void afterReload()
                {
                    LogUtil.printPosLn("***************************************************************");
                    test();
                }

                @Override
                public void onReloadFailed(Throwable throwable)
                {

                }
            };
            try
            {
                MyBatisBridge.init(porterConf, myBatisOption, "mybatis.xml");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        localMain.startOne(porterConf);
        test();
    }

    public static void test()
    {
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        IBridge bridge = localMain.getBridgeLinker().currentBridge();


        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/testSavePoint"),
                lResponse -> logger.debug(lResponse.toString()));

        for (int i = 0; i < 10; i++)
        {
            bridge.request(new BridgeRequest(PortMethod.POST, "/T1/Hello/add")
                    .addParam("name", "小明-" + (new Random().nextInt(3))), lResponse ->
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            });
        }
        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/count")
                .addParam("name", "小明-1"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new BridgeRequest(PortMethod.POST, "/T1/Hello/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.PUT, "/T1/Hello/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.POST, "/T1/Hello/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.PUT, "/T1/Hello/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/del")
                .addParam("name", "小明-10"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/testJBatis"), null);


        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));

        ///////////////
        bridge.request(
                new BridgeRequest(PortMethod.GET, "/T1/Hello/transactionOk")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));

        /////////////
        bridge.request(
                new BridgeRequest(PortMethod.GET, "/T1/Hello/transactionFailed")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Hello/clear"),
                lResponse -> logger.debug(lResponse.toString()));

        Object as = new String[]{"1", "2", "3"};

        logger.debug("array={}", as);

    }

}
