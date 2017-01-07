package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortStart;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PortContext
{

    private static final Logger LOGGER = LoggerFactory.getLogger(PortContext.class);
    private ClassLoader classLoader;
    //接口
    private Map<String, Porter> portMap;
    private Map<Class<?>, CheckPassable> checkPassableForCF;

    //private SthDeal sthDeal;

    public PortContext()
    {
        init();
    }

    private void init()
    {
        portMap = new HashMap<>();
    }


    /**
     * 设置当前的类加载器。
     *
     * @param classLoader
     * @return
     */
    public PortContext setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        return this;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public void initSeek(PorterConf porterConf, AutoSetUtil autoSetUtil)
    {
        SthDeal sthDeal = new SthDeal();
        seek(porterConf.getSeekPackages().getPackages(), autoSetUtil, sthDeal);

        Set<Class<?>> forSeek = porterConf.getSeekPackages().getClassesForSeek();
        for (Class<?> clazz : forSeek)
        {
            LOGGER.debug("may add porter:{}", clazz);
            try
            {
                mayAddPorter(clazz, autoSetUtil, sthDeal);
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        Set<Object> objectSet = porterConf.getSeekPackages().getObjectsForSeek();
        for (Object object : objectSet)
        {
            LOGGER.debug("may add porter:{}:{}", object.getClass(), object);
            try
            {
                if (PortUtil.isPortClass(object.getClass()))
                {
                    addPorter(object.getClass(), object, autoSetUtil, sthDeal);
                }
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        this.checkPassableForCF = autoSetUtil.getInnerContextBridge().checkPassableForCFTemp;
        autoSetUtil.getInnerContextBridge().checkPassableForCFTemp = null;
    }

    private void seek(@NotNull JSONArray packages, AutoSetUtil autoSetUtil, SthDeal sthDeal)
    {
        if (classLoader != null)
        {
            //Thread.currentThread().setContextClassLoader(classLoader);
            if (classLoader instanceof PackageUtil.IClassLoader)
            {
                PackageUtil.IClassLoader iClassLoader = (PackageUtil.IClassLoader) classLoader;
                iClassLoader.setPackages(packages);
            }
        }

        for (int i = 0; i < packages.size(); i++)
        {
            seekPackage(packages.getString(i), autoSetUtil, sthDeal);
        }
    }


    private void seekPackage(String packageStr, AutoSetUtil autoSetUtil, SthDeal sthDeal)
    {
        LOGGER.debug("***********");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            try
            {
                Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                mayAddPorter(clazz, autoSetUtil, sthDeal);
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void mayAddPorter(Class<?> clazz, AutoSetUtil autoSetUtil, SthDeal sthDeal) throws Exception
    {
        if (PortUtil.isPortClass(clazz))
        {
            addPorter(clazz, null, autoSetUtil, sthDeal);
        }
    }


    private void addPorter(Class<?> clazz, Object objectPorter, AutoSetUtil autoSetUtil,
            SthDeal sthDeal) throws Exception
    {
        LOGGER.debug("添加接口：");
        LOGGER.debug("at " + clazz.getName() + ".<init>(" + clazz.getSimpleName() + ".java:1)");


        Porter porter = sthDeal.porter(clazz, objectPorter, autoSetUtil);
        if (porter != null)
        {
            _PortIn port = porter.getPortIn();
            if (portMap.containsKey(port.getTiedName()))
            {
                LOGGER.warn("the class tiedName '{}' added before.(current:{},last:{})", port.getTiedName(),
                        clazz, portMap.get(port.getTiedName()).getClazz());
            }
            portMap.put(port.getTiedName(), porter);
        }


    }

    Porter getClassPort(String classTied)
    {
        return portMap.get(classTied);
    }


    public void start()
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            Porter porter = iterator.next();
            _PortStart[] starts = porter.getStarts();
            for (int i = 0; i < starts.length; i++)
            {
                try
                {
                    starts[i].getMethod().invoke(porter.getObject());
                } catch (Exception e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public void destroy()
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            Porter porter = iterator.next();
            _PortDestroy[] ds = porter.getDestroys();
            for (int i = 0; i < ds.length; i++)
            {
                try
                {
                    ds[i].getMethod().invoke(porter.getObject());
                } catch (Exception e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }


    CheckPassable getCheckPassable(Class<? extends CheckPassable> clazz)
    {
        return checkPassableForCF.get(clazz);
    }

}
