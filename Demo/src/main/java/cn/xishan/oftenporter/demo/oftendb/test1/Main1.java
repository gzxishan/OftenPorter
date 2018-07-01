package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;
import cn.xishan.oftenporter.porter.local.LocalMain;

import java.io.IOException;
import java.util.Random;

public class Main1
{

    public static void main(String[] args)
    {
        LocalMain localMain = new LocalMain(true, new PName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setContextName("T1");
        porterConf.getSeekPackages()
                .addPorters(Main1.class.getPackage().getName() + ".porter");

        {
            MyBatisOption myBatisOption = new MyBatisOption("/oftendb/test1mapper/");
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:~/PorterDemo/oftendb2;MODE=MySQL");
            jdbcDataSource.setUser("sa");
            jdbcDataSource.setPassword("");
            myBatisOption.dataSourceObject = jdbcDataSource;
            try
            {
                MyBatisBridge.init(porterConf,myBatisOption,"mybatis.xml");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        localMain.startOne(porterConf);
        final Logger logger = LoggerFactory.getLogger(Main1.class);

        PBridge bridge = localMain.getPLinker().currentBridge();

        for (int i = 0; i < 1; i++)
        {
            bridge.request(new PRequest(PortMethod.POST, "/T1/Hello/add")
                    .addParam("name", "小明-" + (new Random().nextInt(3))).addParam("age", "21")
                    .addParam("sex", "男"), lResponse ->
            {
                Object obj = lResponse.getResponse();
                LogUtil.printPos(obj);
            });
        }
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/count")
                .addParam("name", "小明-1"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.POST, "/T1/Hello/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.PUT, "/T1/Hello/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.POST, "/T1/Hello/update")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.PUT, "/T1/Hello/update2")
                .addParam("name", "小明-5"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/del")
                .addParam("name", "小明-10"), lResponse -> logger.debug(lResponse.toString()));

        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/testJBatis"), null);


        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));

        ///////////////
        bridge.request(
                new PRequest(PortMethod.GET, "/T1/Hello/transactionOk")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));

        /////////////
        bridge.request(
                new PRequest(PortMethod.GET, "/T1/Hello/transactionFailed")
                        .addParam("names", "['小明-1','小明-2','小明-3','小明']"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/list"),
                lResponse -> logger.debug(lResponse.toString()));
        bridge.request(new PRequest(PortMethod.GET, "/T1/Hello/clear"),
                lResponse -> logger.debug(lResponse.toString()));

        localMain.destroyAll();

        Object as = new String[]{"1", "2", "3"};

        logger.debug("array={}", as);

    }

}
