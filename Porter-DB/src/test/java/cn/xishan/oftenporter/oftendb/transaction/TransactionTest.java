package cn.xishan.oftenporter.oftendb.transaction;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisBridge;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.oftendb.transaction.entity.TestEntity;
import cn.xishan.oftenporter.oftendb.transaction.porter.TestPorter;
import cn.xishan.oftenporter.oftendb.transaction.unit.TestUnit;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.local.LocalMain;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Created by https://github.com/CLovinr on 2019/1/2.
 */
public class TransactionTest
{

    @Test
    public void testTS()
    {
        LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setContextName("T1");
        porterConf.getSeekPackages()
                .addPackages(TransactionTest.class.getPackage().getName() + ".porter");

        {
            MyBatisOption myBatisOption = new MyBatisOption(null, "/tsmapper/", true);
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:~/OftenPorterDemo/oftendb-test;MODE=MySQL");
            jdbcDataSource.setUser("sa");
            jdbcDataSource.setPassword("");
            myBatisOption.dataSourceObject = jdbcDataSource;
            myBatisOption.resourcesDir = new File("Demo/src/main/resources").getAbsolutePath().replace("\\", "/");
            try
            {
                MyBatisBridge.init(porterConf, myBatisOption, "mybatis.xml");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        localMain.startOne(porterConf);

        TestEntity entity1 = new TestEntity();
        entity1.setCreatetime(new Date());
        entity1.setId("1");
        entity1.setName("entity1");

        TestEntity entity2 = new TestEntity();
        entity2.setCreatetime(new Date());
        entity2.setId("2");
        entity2.setName("entity2");

        TestEntity entity3 = new TestEntity();
        entity3.setCreatetime(new Date());
        entity3.setId("3");
        entity3.setName("entity3");


        IBridge bridge = localMain.getBridgeLinker().currentBridge();

        TestPorter testPorter = localMain.getAutoVarGetter("T1").getContextSet(TestPorter.class);
        TestUnit testUnit = localMain.getAutoVarGetter("T1").getContextSet(TestUnit.class);

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testOk")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2), lResponse -> {
            assertTrue(testUnit.contains("1"));
            assertTrue(testUnit.contains("2"));
        });
        assertNull(OftenObject.current());
        testPorter.testOk(entity1, entity2);
        assertTrue(testUnit.contains("1"));
        assertTrue(testUnit.contains("2"));

        ////////////////////

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testFail")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2), lResponse -> {
            assertFalse(testUnit.contains("1"));
            assertFalse(testUnit.contains("2"));
        });
        assertTrue(OftenObject.current() == null);
        try
        {
            testPorter.testFail(entity1, entity2);
        } catch (Exception e)
        {

        }
        assertFalse(testUnit.contains("1"));
        assertFalse(testUnit.contains("2"));

        ///////////////////

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testOk2")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2), lResponse -> {
            assertTrue(testUnit.contains("1"));
            assertTrue(testUnit.contains("2"));
        });
        assertTrue(OftenObject.current() == null);
        testPorter.testOk2(entity1, entity2);
        assertTrue(testUnit.contains("1"));
        assertTrue(testUnit.contains("2"));

        /////////////////

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testFail2")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2), lResponse -> {
            assertFalse(testUnit.contains("1"));
            assertFalse(testUnit.contains("2"));
        });
        assertTrue(OftenObject.current() == null);
        try
        {
            testPorter.testFail2(entity1, entity2);
        } catch (Exception e)
        {

        }
        assertFalse(testUnit.contains("1"));
        assertFalse(testUnit.contains("2"));

        //////////////////////

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testOkPoint")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2), lResponse -> {
            assertTrue(testUnit.contains("1"));
            assertTrue(testUnit.contains("2"));
        });
        assertTrue(OftenObject.current() == null);
        testPorter.testOkPoint(entity1, entity2);
        assertTrue(testUnit.contains("1"));
        assertTrue(testUnit.contains("2"));

        ////////////////////

        bridge.request(new BridgeRequest(PortMethod.GET, "/T1/Test/testFailPoint")
                .addParam("entity1", entity1)
                .addParam("entity2", entity2)
                .addParam("entity3", entity3), lResponse -> {
            assertTrue(testUnit.contains("1"));
            assertFalse(testUnit.contains("2"));
            assertTrue(testUnit.contains("3"));
        });
        assertTrue(OftenObject.current() == null);
        try
        {
            testPorter.testFailPoint(entity1, entity2, entity3);
        } catch (Exception e)
        {

        }
        assertTrue(testUnit.contains("1"));
        assertFalse(testUnit.contains("2"));
        assertTrue(testUnit.contains("3"));


        localMain.destroyAll();
    }
}
