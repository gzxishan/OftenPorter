package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
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
public class ContextPorter
{

    private static final Logger LOGGER = LogUtil.logger(ContextPorter.class);
    private ClassLoader classLoader;
    //接口
    private Map<String, Porter> portMap;
    private Map<Class<?>, CheckPassable> checkPassableForCF;

    //private SthDeal sthDeal;

    public ContextPorter()
    {
        init();
    }

    private void init()
    {
        portMap = new HashMap<>();
    }


    public Map<String, Porter> getPortMap()
    {
        return portMap;
    }

    /**
     * 设置当前的类加载器。
     *
     * @param classLoader
     * @return
     */
    public ContextPorter setClassLoader(ClassLoader classLoader)
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

    public Map<Class<?>, CheckPassable> initSeek(SthDeal sthDeal,ListenerAdder<OnPorterAddListener> listenerAdder,
            PorterConf porterConf,
            AutoSetHandle autoSetHandle) throws FatalInitException
    {
        this.porterConf = porterConf;
        this.listenerAdder = listenerAdder;

        seek(porterConf.getSeekPackages().getPackages(), autoSetHandle, sthDeal);

        Set<Class<?>> forSeek = porterConf.getSeekPackages().getClassesForSeek();
        for (Class<?> clazz : forSeek)
        {
            LOGGER.debug("may add porter:{}", clazz);
            try
            {
                mayAddPorter(clazz, autoSetHandle, sthDeal);
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
                if (PortUtil.isPortClass(object.getClass()) && willSeek(object.getClass()))
                {
                    addPorter(object.getClass(), object, autoSetHandle, sthDeal);
                }
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        this.checkPassableForCF = autoSetHandle.getInnerContextBridge().checkPassableForCFTemps;
        autoSetHandle.getInnerContextBridge().checkPassableForCFTemps = null;

        this.porterConf = null;
        this.listenerAdder = null;

        return checkPassableForCF;
    }

    private void seek(@NotNull JSONArray packages,
            AutoSetHandle autoSetHandle, SthDeal sthDeal) throws FatalInitException
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
            seekPackage(packages.getString(i), autoSetHandle, sthDeal);
        }
    }


    private void seekPackage(String packageStr,
            AutoSetHandle autoSetHandle, SthDeal sthDeal) throws FatalInitException
    {
        LOGGER.debug("***********");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            try
            {
                Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                mayAddPorter(clazz, autoSetHandle, sthDeal);
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
        if (!willSeek)
        {
            LOGGER.debug("seek canceled!![{}]", clazz);
        }
        return willSeek;
    }

    private void mayAddPorter(Class<?> clazz, AutoSetHandle autoSetHandle,
            SthDeal sthDeal) throws FatalInitException, Exception
    {
        if (PortUtil.isPortClass(clazz) && willSeek(clazz))
        {
            addPorter(clazz, null, autoSetHandle, sthDeal);
        }
    }


    /**
     * 添加接口！！！！！
     *
     * @param clazz
     * @param objectPorter
     * @param autoSetHandle
     * @param sthDeal
     * @throws FatalInitException
     * @throws Exception
     */
    private void addPorter(Class<?> clazz, Object objectPorter,
            AutoSetHandle autoSetHandle,
            SthDeal sthDeal) throws FatalInitException, Exception
    {
        LOGGER.debug("添加接口：");
        LOGGER.debug("\n\tat " + clazz.getName() + ".<init>(" + clazz.getSimpleName() + ".java:1)");

        Porter porter = sthDeal.porter(clazz, objectPorter, porterConf.getContextName(), autoSetHandle);
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
                autoSetHandle.getInnerContextBridge().contextAutoSet.put(porter.getClazz().getName(), porter.getObj());
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
