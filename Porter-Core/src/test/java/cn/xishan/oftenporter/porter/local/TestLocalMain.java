package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.DefaultReturnFactory;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.sysset.IAutoSetter;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
import cn.xishan.oftenporter.porter.local.porter.Article;
import cn.xishan.oftenporter.porter.local.porter.User;
import cn.xishan.oftenporter.porter.local.porter2.My2Porter;
import cn.xishan.oftenporter.porter.local.porter2.MyPorter;
import cn.xishan.oftenporter.porter.local.proxy.ProxyUnit;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
public class TestLocalMain
{

    interface Listener
    {
        void onEnd(long totalDTime, int n);
    }

    public static void main(String[] args)
    {
        new TestLocalMain().main();
    }

    @Test
    public void main()
    {
        PropertyConfigurator.configure(getClass().getResourceAsStream("/log4j.properties"));
        final LocalMain localMain = new LocalMain(true, new BridgeName("P1"), "utf-8");
        PorterConf porterConf = localMain.newPorterConf();
        porterConf.setOftenContextName("Local-1");
        porterConf.getSeekPackages().addPackages(getClass().getPackage().getName() + ".porter");
        porterConf.getSeekPackages().addPackages(getClass().getPackage().getName() + ".porter3");
        porterConf.getSeekPackages().addPackages(getClass().getPackage().getName() + ".mixin");
        porterConf.getSeekPackages().addClassPorter(My2Porter.class)
                .addObjectPorter(new MyPorter("Hello MyPorter!"));


        porterConf.addNormalAspectAdvancedHandle(
                new AutoSetObjForAspectOfNormal.AdvancedHandle(false, new AspectHandle("*(*OftenObject)*")));

        Properties properties = new Properties();
        properties.setProperty("isTest", String.valueOf(true));
        properties.setProperty("bridgeName", "P1");
        properties.setProperty("ref-bridgeName", "ref-#{bridgeName}-#{isTest}");
        properties.setProperty("change", "a");
        porterConf.getConfigData().putAll(properties);

        //porterConf.setEnableTiedNameDefault(false);
        porterConf.addContextAutoSet("globalName", "全局对象");
        porterConf.addContextAutoSet(new ProxyUnit(new Random()));

        porterConf.setDefaultReturnFactory(new DefaultReturnFactory()
        {
            @Override
            public Object getVoidReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject,
                    Object handleMethod)
            {
                return Void.TYPE;
            }

            @Override
            public Object getNullReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject,
                    Object handleMethod)
            {
                return "null-return";
            }

            @Override
            public Object getExReturn(OftenObject oftenObject, Object finalPorterObject, Object handleObject,
                    Object handleMethod, Throwable throwable) throws Throwable
            {
                throw throwable;
            }
        });

        final Logger logger = LoggerFactory.getLogger(getClass());

        porterConf.addStateListener(new StateListener.Adapter()
        {
            @Override
            public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
                    ParamSourceHandleManager paramSourceHandleManager)
            {
                initParamSource.putInitParameter("debug", true);
                logger.debug("");
            }

            @Override
            public void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager)
            {
                logger.debug("");
            }

            @Override
            public void beforeDestroy()
            {
                logger.debug("");
            }

            @Override
            public void afterDestroy()
            {
                logger.debug("");
            }
        });

        porterConf.addContextCheck((wObject, type, handle) ->
        {
            handle.next();
        });

        IConfigData configData = porterConf.getConfigData();
        IAutoSetter autoSetter = localMain.startOne(porterConf);
        configData.set("change", "b");

        int n = 10 * 10000;
        final int threads = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(threads, r ->
        {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            return thread;
        });


        //多线程下测试
        // final long time = System.nanoTime();
        exe(executorService, n, localMain.getBridgeLinker().currentBridge(), (totalDtime, N) ->
        {

            logger.debug("******************世界你好，中国你好********************");
            logger.debug("threads={},n={}:total={}ms,average={}ms", threads, N, 1.0f * totalDtime,
                    1.0f * totalDtime / N);
            logger.debug("******************世界你好，中国你好********************");

            logger.debug("**************AutoSet delay test******************");
            localMain.getBridgeLinker().currentBridge()
                    .request(new BridgeRequest(PortMethod.GET, "/Local-1/Delay/test"),
                            lResponse -> logger.debug("{}", lResponse));

            try
            {
                hotTest(logger, localMain);
            } catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }

            List<Object> autoSetterList = new ArrayList<>();
            String[] destroyResult = new String[]{
                    "init"
            };
            try
            {
                //手动注入测试
                autoSetter.forInstance(new Object[]{
                        new Object()
                        {
                            @AutoSet
                            My2Porter my2Porter;
                            @AutoSet
                            ProxyUnit proxyUnit;

                            @AutoSet.SetOk
                            public void setOk()
                            {
                                autoSetterList.add(my2Porter);
                                autoSetterList.add(proxyUnit);
                            }

                            @PortDestroy
                            public void onDestroy()
                            {
                                destroyResult[0] = "destroy-ok";
                            }
                        }
                });
                localMain.destroyAll();
                executorService.shutdown();
            } finally
            {
                assertEquals(My2Porter.class, getClass(autoSetterList, 0));
                assertEquals(ProxyUnit.class, getClass(autoSetterList, 1));
                assertEquals("destroy-ok", destroyResult[0]);

            }


        });


    }

    private Class getClass(List<Object> list, int index)
    {
        Object obj = null;
        if (index < list.size())
        {
            obj = list.get(index);
        }
        return obj == null ? null : ProxyUtil.unwrapProxyForGeneric(obj);
    }

    private void hotTest(final Logger logger, CommonMain commonMain) throws Exception
    {
        logger.debug("热部署测试...");
        File dir = new File(System.getProperty(
                "user.home") + File.separator + "porter-core" + File.separator + "test" + File.separator);
        if (!dir.exists())
        {
            boolean rs = dir.mkdirs();
            if (!rs)
            {
                throw new IOException("mkdirs failed:" + dir.getAbsolutePath());
            }
        }
        File clazzFile = new File(dir.getPath() + File.separator + "HotPorter.jar");


        FileTool.write2File(getClass().getResourceAsStream("/hot/hot1.jar"), clazzFile, true);
        PorterConf conf = commonMain.newPorterConf();
        conf.setClassLoader(new URLClassLoader(new URL[]{clazzFile.toURI().toURL()}));
        conf.getSeekPackages().addPackages("cn.xishan.oftenporter.porter.local.hot");
        conf.setOftenContextName("hot-test");
        commonMain.startOne(conf);
        commonMain.getBridgeLinker().currentBridge().request(
                new BridgeRequest(PortMethod.GET, "/hot-test/Hot/show"),
                lResponse -> logger.debug(lResponse.toString()));
        commonMain.destroyOne("hot-test");
        //////////////////////////////////////
        FileTool.write2File(getClass().getResourceAsStream("/hot/hot2.jar"), clazzFile, true);
        conf = commonMain.newPorterConf();
        conf.setClassLoader(new URLClassLoader(new URL[]{clazzFile.toURI().toURL()}));
        conf.getSeekPackages().addPackages("cn.xishan.oftenporter.porter.local.hot");
        conf.setContextName("hot-test");
        commonMain.startOne(conf);
        commonMain.getBridgeLinker().currentBridge()
                .request(new BridgeRequest(PortMethod.GET, "/hot-test/Hot/show"),
                        lResponse -> logger.debug(lResponse.toString()));
    }

    private void exe(final ExecutorService executorService, final int n, final IBridge bridge, final Listener listener)
    {

        final AtomicInteger count = new AtomicInteger(0);
        final AtomicLong dtime = new AtomicLong(0);

        for (int i = 0; i < n; i++)
        {

            if (executorService == null)
            {
                bridge.request(
                        new BridgeRequest(PortMethod.GET, "/Local-1/Hello/say").addParam("name", "小明")
                                .addParam("age", "22")
                                .addParam("myAge", 22),
                        lResponse -> assertEquals("小明+22", lResponse.getResponse()));
            } else
            {
                executorService.execute(() ->
                {
                    long time = System.currentTimeMillis();

                    bridge.request(
                            new BridgeRequest(PortMethod.GET, "/Local-1/Hello/parseObject").addParam("title", "转换成对象")
                                    .addParam("comments", "['c1','c2']")
                                    .addParam("content", "this is content!")
                                    .addParam("time", String.valueOf(System.currentTimeMillis()))
                                    .addParam("name", "小傻").addParam("myAge", "18"),
                            lResponse ->
                                    assertTrue(
                                            lResponse.getResponse() instanceof User || lResponse
                                                    .getResponse() instanceof Article));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Hello/helloMixin"),
                            lResponse -> assertEquals("Mixin!", lResponse.getResponse()));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Hello/helloMixinTo"),
                            lResponse -> assertEquals("MixinTo!", lResponse.getResponse()));
                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Delay/helloMixinTo"),
                            lResponse -> assertEquals("MixinTo!", lResponse.getResponse()));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Hello/say").addParam("name", "小明")
                                    .addParam("age", "22")
                                    .addParam("myAge", 22),
                            lResponse -> assertEquals("小明+22", lResponse.getResponse()));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Hello/").addParam("sex", "男")
                            .addParam("name", "name2")
                            .addParam("myAge", 10), lResponse -> assertEquals("=男", lResponse.getResponse()));


                    bridge.request(
                            new BridgeRequest(PortMethod.GET, "/Local-1/Hello").setMethod(PortMethod.POST)
                                    .addParam("name", "name3")
                                    .addParam("myAge", 10).addParam("sex", "0"),
                            lResponse -> assertEquals(":0", lResponse.getResponse()));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/Hello/hihihi").setMethod(PortMethod.POST)
                                    .addParam("name", "name4")
                                    .addParam("myAge", 10).addParam("sex", "0"),
                            lResponse -> assertEquals("hihihi:0", lResponse.getResponse()));

                    bridge.request(new BridgeRequest(PortMethod.GET, "/Local-1/My2/hello"),
                            lResponse -> assertEquals("My2Porter", lResponse.getResponse()));

                    dtime.addAndGet(System.currentTimeMillis() - time);
                    if (count.incrementAndGet() == n)
                    {
                        listener.onEnd(dtime.get(), n);
                        executorService.shutdown();
                    }
                });
            }


        }

        if (executorService != null)
        {

            try
            {
                while (!executorService.isTerminated())
                {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();

            }
        }

    }
}
