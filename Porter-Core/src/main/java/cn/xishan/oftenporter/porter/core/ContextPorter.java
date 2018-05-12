package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MixinTo;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal._MixinPorter;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.IOtherStartDestroy;
import cn.xishan.oftenporter.porter.core.init.PortIniter;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class ContextPorter implements IOtherStartDestroy
{


    @Override
    public void addOtherStart(Object object, Method[] starts)
    {
        for (Method method : starts)
        {
            PortIn.PortStart portStart = AnnoUtil.getAnnotation(method, PortIn.PortStart.class);
            otherStartList.add(new OtherStartDestroy(object, method, portStart.order()));
        }
    }

    @Override
    public void addOtherDestroys(Object object, Method[] destroys)
    {
        for (Method method : destroys)
        {
            PortIn.PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortIn.PortDestroy.class);
            otherDestroyList.add(new OtherStartDestroy(object, method, portDestroy.order()));
        }
    }

    class OtherStartDestroy implements Comparable<OtherStartDestroy>
    {
        Object object;
        Method method;
        int order;

        public OtherStartDestroy(Object object, Method method, int order)
        {
            this.object = object;
            this.method = method;
            this.order = order;
        }

        @Override
        public int compareTo(OtherStartDestroy other)
        {
            return order - other.order;
        }
    }

    public static final class SrcPorter
    {
        Class<?> clazz;
        Object object;

        public SrcPorter(Class<?> clazz, Object object)
        {
            this.clazz = clazz;
            this.object = object;
        }

        public Class<?> getClazz()
        {
            return clazz;
        }

        public Object getObject()
        {
            return object;
        }
    }


    private static final Logger LOGGER = LogUtil.logger(ContextPorter.class);
    private ClassLoader classLoader;
    //接口
    private Map<String, Porter> portMap;
    private Map<Class<?>, CheckPassable> checkPassableForCF;
    private List<OtherStartDestroy> otherStartList = new ArrayList<>();
    private List<OtherStartDestroy> otherDestroyList = new ArrayList<>();


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

    private Map<Class, SrcPorter> class_porterMap;
    private Map<Class, Set<_MixinPorter>> mixinToMap;//key为被混入的接口。


    public Map<Class<?>, CheckPassable> initSeek(SthDeal sthDeal, ListenerAdder<OnPorterAddListener> listenerAdder,
            PorterConf porterConf,
            AutoSetHandle autoSetHandle, List<PortIniter> portIniterList) throws FatalInitException
    {
        this.porterConf = porterConf;
        this.listenerAdder = listenerAdder;
        class_porterMap = new HashMap<>();
        mixinToMap = new HashMap<>();

        autoSetHandle.setIOtherStartDestroy(this);
        // 1/3:搜索包:最终是搜索Class类
        seek(porterConf.getSeekPackages().getPackages());

        // 2/3:搜索Class类
        Set<Class<?>> forSeek = porterConf.getSeekPackages().getClassesForSeek();
        for (Class<?> clazz : forSeek)
        {
            try
            {
                mayAddPorterOfClass(clazz, null);
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        // 3/3:搜索实例
        Set<Object> objectSet = porterConf.getSeekPackages().getObjectsForSeek();
        for (Object object : objectSet)
        {
            try
            {
                mayAddPorterOfClass(object.getClass(), object);
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }


        try
        {
            //添加接口
            LOGGER.debug("添加接口开始:{}**********************[", autoSetHandle.getContextName());
            for (Map.Entry<Class, SrcPorter> entry : class_porterMap.entrySet())
            {
                SrcPorter porter = entry.getValue();
                addPorter(porter, autoSetHandle, sthDeal, portIniterList);
            }
            LOGGER.debug("添加接口完毕:{}*****************************]", autoSetHandle.getContextName());
            class_porterMap = null;
            mixinToMap = null;
        } catch (FatalInitException e)
        {
            throw e;
        } catch (Exception e)
        {
            LOGGER.warn(e.getMessage(), e);
        }


        this.checkPassableForCF = autoSetHandle.getInnerContextBridge().checkPassableForCFTemps;
        autoSetHandle.getInnerContextBridge().checkPassableForCFTemps = null;

        this.porterConf = null;
        this.listenerAdder = null;

        for (Map.Entry<String, Porter> entry : portMap.entrySet())
        {
            entry.getValue().dealInNames(autoSetHandle.getInnerContextBridge().innerBridge.globalParserStore);
        }


        PortIniter[] portIniters = portIniterList.toArray(new PortIniter[0]);
        Arrays.sort(portIniters);//排序
        portIniterList.clear();
        WPTool.addAll(portIniterList, portIniters);

        return checkPassableForCF;
    }


    private void mayAddStaticAutoSet(Class<?> clazz)
    {
        Field[] fields = WPTool.getAllFields(clazz);
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(AutoSet.class))
            {
                porterConf.addStaticAutoSetClasses(clazz);
                break;
            }
        }
    }

    private void seek(@NotNull JSONArray packages) throws FatalInitException
    {
        if (classLoader != null)
        {
            //Thread.currentThread().setContextClassLoader(classLoader);
            if (classLoader instanceof PackageUtil.IClassLoader)
            {
                PackageUtil.IClassLoader iClassLoader = (PackageUtil.IClassLoader) classLoader;
                List list = packages;
                iClassLoader.setPackages(list);
            }
        }

        for (int i = 0; i < packages.size(); i++)
        {
            seekPackage(packages.getString(i));
        }
    }


    private void seekPackage(String packageStr) throws FatalInitException
    {
        LOGGER.debug("***********");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            try
            {
                Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                mayAddPorterOfClass(clazz, null);
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

    private void mayAddPorterOfClass(Class<?> clazz, Object objectPorter) throws FatalInitException, Exception
    {
        if (PortUtil.isJustPortInClass(clazz) && willSeek(clazz))
        {
            LOGGER.debug("will add porter:{}({})", clazz, objectPorter);
            class_porterMap.put(clazz, new SrcPorter(clazz, objectPorter));
            //addPorter(clazz, objectPorter, autoSetHandle, sthDeal, portIniterList);
        } else if (!isMixinTo(clazz, objectPorter))
        {
            LOGGER.debug("will do static @{}:{}", AutoSet.class.getSimpleName(), clazz);
            mayAddStaticAutoSet(clazz);
        }
    }

    private boolean isMixinTo(Class<?> clazz, Object objectPorter)
    {
        MixinTo mixinTo = AnnoUtil.getAnnotation(clazz, MixinTo.class);
        if (mixinTo != null)
        {
            _MixinPorter minxinPorter = new _MixinPorter(clazz, objectPorter, mixinTo.override());
            Set<_MixinPorter> set = mixinToMap.get(mixinTo.toPorter());
            if (set == null)
            {
                set = new HashSet<>();
                mixinToMap.put(mixinTo.toPorter(), set);
            }
            LOGGER.debug("add to mixinToMap:{}", clazz);
            set.add(minxinPorter);
            return true;
        } else
        {
            return false;
        }
    }


    /**
     * 添加接口！！！！！
     *
     * @param srcPorter
     * @param autoSetHandle
     * @param sthDeal
     * @throws FatalInitException
     * @throws Exception
     */
    private void addPorter(SrcPorter srcPorter, AutoSetHandle autoSetHandle,
            SthDeal sthDeal, List<PortIniter> portIniterList) throws FatalInitException, Exception
    {
        Class clazz = srcPorter.getClazz();
        LOGGER.debug("添加接口：{}", clazz);
        //LOGGER.debug("\n\t\t\t\t\t" + clazz.getName() + ".<init>(" + clazz.getSimpleName() + ".java:1)");

        Porter porter = sthDeal.porter(srcPorter, mixinToMap, porterConf.getContextName(), autoSetHandle);
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
                String[] tieds = port.getTiedNames();
                for (String tiedName : tieds)
                {
                    if (portMap.containsKey(tiedName))
                    {
                        LOGGER.warn("class tiedName '{}' added before.(current:{},last:{})", tiedName,
                                clazz, portMap.get(tiedName).getClazz());
                    }
                    portMap.put(tiedName, porter);
                }
                autoSetHandle.getInnerContextBridge().contextAutoSet.put(porter.getClazz().getName(), porter.getObj());
            } else
            {
                LOGGER.warn("porter add canceled!!(class tied={},{})", port.getTiedNames(), clazz);
            }

            Iterator<Porter> it = portMap.values().iterator();
            while (it.hasNext())
            {
                it.next().seekPortInit(porterConf.getContextName(), portIniterList);
            }
        }
    }

    Porter getClassPort(String classTied)
    {
        return portMap.get(classTied);
    }


    public void start(WObject wObject)
    {

        OtherStartDestroy[] otherStartDestroys = otherStartList.toArray(new OtherStartDestroy[0]);
        Arrays.sort(otherStartDestroys);

        for (OtherStartDestroy otherStartDestroy : otherStartDestroys)
        {
            try
            {
                Method method = otherStartDestroy.method;
                method.setAccessible(true);
                if (method.getParameterTypes().length > 0)
                {
                    method.invoke(otherStartDestroy.object, wObject);
                } else
                {
                    method.invoke(otherStartDestroy.object);
                }
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        Iterator<Porter> iterator = portMap.values().iterator();
        try
        {
            while (iterator.hasNext())
            {
                iterator.next().initArgumentsFactory();
            }
        }catch (Exception e){
            throw new InitException(e);
        }


        iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().initIInObjHandle();
        }

        iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().start(wObject);
        }
    }

    public void destroy()
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().destroy();
        }
        OtherStartDestroy[] otherStartDestroys = otherDestroyList.toArray(new OtherStartDestroy[0]);
        Arrays.sort(otherStartDestroys);
        for (OtherStartDestroy otherStartDestroy : otherStartDestroys)
        {
            try
            {
                Method method = otherStartDestroy.method;
                method.setAccessible(true);
                method.invoke(otherStartDestroy.object);
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    CheckPassable getCheckPassable(Class<? extends CheckPassable> clazz)
    {
        return checkPassableForCF.get(clazz);
    }

}
