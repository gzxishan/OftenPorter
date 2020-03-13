package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.h2.jdbcx.JdbcDataSource;

import java.io.IOException;

/**
 * 配合Jdk/bin/jvisualvm.exe进行内存泄漏检测
 * @author Created by https://github.com/CLovinr on 2018/9/21.
 */
public class TestForMem
{
    public static void main(String[] args) throws InterruptedException
    {
        while (true)
        {
            test();
            Thread.sleep(500);
        }
    }

    static void test()
    {
        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setOftenContextName("T1");
        porterConf.getSeekPackages()
                .addPackages(Main1.class.getPackage().getName() + ".porter");


        MyBatisOption myBatisOption = new MyBatisOption(null,"/oftendb/test1mapper/", true);
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:~/PorterDemo/oftendb2;MODE=MySQL");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        myBatisOption.dataSourceObject = jdbcDataSource;
        //myBatisOption.resourcesDir = new File("Demo/src/main/resources").getAbsolutePath().replace("\\", "/");
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
        localMain.startOne(porterConf);
        localMain.destroyOne(porterConf.getOftenContextName());
    }
}
