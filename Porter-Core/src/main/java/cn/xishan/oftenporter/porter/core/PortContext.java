package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;

import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PortContext
{

    private static final Logger LOGGER = LogUtil.logger(PortContext.class);
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

    private PorterConf porterConf;
    private ListenerAdder<OnPorterAddListener> listenerAdder;

    public Map<Class<?>, CheckPassable> initSeek(ListenerAdder<OnPorterAddListener> listenerAdder,
            PorterConf porterConf,
            AutoSetUtil autoSetUtil) throws FatalInitException
    {
        this.porterConf = porterConf;
        this.listenerAdder = listenerAdder;

        SthDeal sthDeal = new SthDeal();
        seek(porterConf.getSeekPackages().getPackages(), autoSetUtil, sthDeal);

        Set<Class<?>> forSeek = porterConf.getSeekPackages().getClassesForSeek();
        for (Class<?> clazz : forSeek)
        {
            LOGGER.debug("may add porter:{}", clazz);
            try
            {
                mayAddPorter(clazz, autoSetUtil, sthDeal);
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        Set<Object> objectSet = porterConf.getSeekPackages().getObjectsForSeek();
        for (Object object : objectSet)
        {
            LOGGER.debug("may add porter:{}:{}", object.getClass(), object);
            try
            {
                if (PortUtil.isPortClass(object.getClass())&&willSeek(object.getClass()))
                {
                    addPorter(object.getClass(), object, autoSetUtil, sthDeal);
                }
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        this.checkPassableForCF = autoSetUtil.getInnerContextBridge().checkPassableForCFTemps;
        autoSetUtil.getInnerContextBridge().checkPassableForCFTemps = null;

        this.porterConf = null;
        this.listenerAdder = null;

        return checkPassableForCF;
    }

    private void seek(@NotNull JSONArray packages,
            AutoSetUtil autoSetUtil, SthDeal sthDeal) throws FatalInitException
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


    private void seekPackage(String packageStr,
            AutoSetUtil autoSetUtil, SthDeal sthDeal) throws FatalInitException
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
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    private boolean willSeek(Class<?> clazz)
    {
        boolean willSeek = true;
        Enumeration<OnPorterAddListener> enumeration = listenerAdder.listeners();
        while (enumeration.hasMoreElements())
        {
            OnPorterAddListener onPorterAddListener = enumeration.nextElement();
            if (onPorterAddListener.onSeeking(porterConf.getContextName(), clazz))
            {
                willSeek = false;
                break;
            }
        }
        if(!willSeek){
            LOGGER.debug("seek canceled!![{}]",clazz);
        }
        return willSeek;
    }

    private void mayAddPorter(Class<?> clazz, AutoSetUtil autoSetUtil,
            SthDeal sthDeal) throws FatalInitException, Exception
    {
        if (PortUtil.isPortClass(clazz)&&willSeek(clazz))
        {
            addPorter(clazz, null, autoSetUtil, sthDeal);
        }
    }


    /**
     * 添加接口！！！！！
     *
     * @param clazz
     * @param objectPorter
     * @param autoSetUtil
     * @param sthDeal
     * @throws FatalInitException
     * @throws Exception
     */
    private void addPorter(Class<?> clazz, Object objectPorter,
            AutoSetUtil autoSetUtil,
            SthDeal sthDeal) throws FatalInitException, Exception
    {
        LOGGER.debug("添加接口：");
        LOGGER.debug("\n\tat " + clazz.getName() + ".<init>(" + clazz.getSimpleName() + ".java:1)");

        Porter porter = sthDeal.porter(clazz, objectPorter, autoSetUtil);
        if (porter != null)
        {
            boolean willAdd = true;
            Enumeration<OnPorterAddListener> enumeration = listenerAdder.listeners();
            while (enumeration.hasMoreElements())
            {
                OnPorterAddListener onPorterAddListener = enumeration.nextElement();
                if (onPorterAddListener.onAdding(porterConf.getContextName(), porter))
                {
                    willAdd = false;
                    break;
                }
            }
            _PortIn port = porter.getPortIn();
            if (willAdd)
            {
                if (portMap.containsKey(port.getTiedName()))
                {
                    LOGGER.warn("class tiedName '{}' added before.(current:{},last:{})", port.getTiedName(),
                            clazz, portMap.get(port.getTiedName()).getClazz());
                }
                portMap.put(port.getTiedName(), porter);
            } else
            {
                LOGGER.warn("porter add canceled!!(class tied={},{})", port.getTiedName(), clazz);
            }
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
            iterator.next().start();
        }
    }

    public void destroy()
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().destroy();
        }
    }


    CheckPassable getCheckPassable(Class<? extends CheckPassable> clazz)
    {
        return checkPassableForCF.get(clazz);
    }

}
