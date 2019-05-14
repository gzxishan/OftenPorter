package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal._MixinPorter;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.sysset.IAutoSetter;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
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
    public void addOtherStarts(Object object, Method[] starts)
    {
        for (Method method : starts)
        {
            PortStart portStart = AnnoUtil.getAnnotation(method, PortStart.class);
            otherStartList.add(new OtherStartDestroy(object, method, portStart.order()));
        }
    }

    @Override
    public void addOtherDestroys(Object object, Method[] destroys)
    {
        for (Method method : destroys)
        {
            PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
            otherDestroyList.add(new OtherStartDestroy(object, method, portDestroy.order()));
        }
    }

    public static class OtherStartDestroy implements Comparable<OtherStartDestroy>
    {
        public Object object;
        public Method method;
        public int order;

        public OtherStartDestroy(Object object, Method method, int order)
        {
            this.object = object;
            this.method = method;
            this.order = order;
        }

        @Override
        public int compareTo(OtherStartDestroy other)
        {
            int n = order - other.order;
            if (n == 0)
            {
                return 0;
            } else if (n > 0)
            {
                return 1;
            } else
            {
                return -1;
            }
        }
    }

    public static final class SrcPorter
    {
        Class<?> clazz;
        Object object;
        SeekPackages.Tiedfix classTiedfix;
        String funTiedPrefix = "";
        String funTiedSuffix = "";

        public SrcPorter(Class<?> clazz, Object object)
        {
            this.clazz = clazz;
            this.object = object;
        }

        public SrcPorter(_MixinPorter mixinPorter)
        {
            this(mixinPorter.getClazz(), mixinPorter.getObject());
            this.funTiedPrefix = mixinPorter.getFunTiedPrefix();
            this.funTiedSuffix = mixinPorter.getFunTiedSuffix();
        }

        public SeekPackages.Tiedfix getClassTiedfix()
        {
            return classTiedfix;
        }


        public Class<?> getClazz()
        {
            return clazz;
        }

        public Object getObject()
        {
            return object;
        }

        public String getFunTiedPrefix()
        {
            return funTiedPrefix;
        }

        public String getFunTiedSuffix()
        {
            return funTiedSuffix;
        }
    }


    private static final Logger LOGGER = LogUtil.logger(ContextPorter.class);
    private ClassLoader classLoader;
    //接口
    private Map<String, Porter> portMap;
    private Map<Class<?>, CheckPassable> checkPassableForCF;
    private List<OtherStartDestroy> otherStartList = new ArrayList<>();
    private List<OtherStartDestroy> otherDestroyList = new ArrayList<>();
    private IConfigData configData;
    private IAutoSetter autoSetter;

    //private SthDeal sthDeal;

    public ContextPorter(IAutoSetter autoSetter, IConfigData configData)
    {
        this.autoSetter = autoSetter;
        this.configData = configData;
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
    private IListenerAdder<OnPorterAddListener> listenerAdder;

    private Map<Class, SrcPorter> class_porterMap;
    private Map<Class, Set<_MixinPorter>> mixinToMap;//key为被混入的接口。


    public Map<Class<?>, CheckPassable> initSeek(SthDeal sthDeal, IListenerAdder<OnPorterAddListener> listenerAdder,
            PorterConf porterConf, AutoSetHandle autoSetHandle, List<PortIniter> portIniterList) throws Throwable
    {
        this.porterConf = porterConf;
        this.listenerAdder = listenerAdder;
        class_porterMap = new HashMap<>();
        mixinToMap = new HashMap<>();

        autoSetHandle.setIOtherStartDestroy(this);
        // 1/3:搜索包:最终是搜索Class类
        seekPackages(porterConf.getSeekPackages().getPackages(), porterConf.getSeekPackages().getTiedfixPkgs());

        // 2/3:搜索Class类
        Set<Class<?>> forSeek = porterConf.getSeekPackages().getClassesForSeek();
        for (Class<?> clazz : forSeek)
        {
            mayAddPorterOfClass(clazz, null, null);
        }

        // 3/3:搜索实例
        Set<Object> objectSet = porterConf.getSeekPackages().getObjectsForSeek();
        for (Object object : objectSet)
        {
            mayAddPorterOfClass(PortUtil.getRealClass(object), object, null);
        }


        {
            OftenContextInfo contextInfo = autoSetHandle.getOftenContextInfo();
            //添加接口
            LOGGER.debug("添加接口开始:{}**********************[", contextInfo.getContextName());
            for (Map.Entry<Class, SrcPorter> entry : class_porterMap.entrySet())
            {
                SrcPorter porter = entry.getValue();
                addPorter(porter, autoSetHandle, sthDeal, portIniterList);
            }
            LOGGER.debug("添加接口完毕:{}*****************************]", contextInfo.getContextName());
            class_porterMap = null;
            mixinToMap = null;
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
        OftenTool.addAll(portIniterList, portIniters);

        return checkPassableForCF;
    }


    private void mayAddStaticAutoSet(Class<?> clazz)
    {
        Field[] fields = OftenTool.getAllFields(clazz);
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

    private void seekPackages(@NotNull JSONArray packages,
            List<SeekPackages.TiedfixPkg> tiedfixPkgList) throws FatalInitException
    {
        if (classLoader != null)
        {
            //Thread.currentThread().setContextClassLoader(classLoader);
            if (classLoader instanceof PackageUtil.IClassLoader)
            {
                PackageUtil.IClassLoader iClassLoader = (PackageUtil.IClassLoader) classLoader;
                List<String> list = new ArrayList<>();
                for (int i = 0; i < packages.size(); i++)
                {
                    list.add(packages.getString(i));
                }
                for (SeekPackages.TiedfixPkg tiedfixPkg : tiedfixPkgList)
                {
                    list.add(tiedfixPkg.getPackageName());
                }
                iClassLoader.setPackages(list);
            }
        }

        for (int i = 0; i < packages.size(); i++)
        {
            seekPackage(packages.getString(i), null);
        }
        for (SeekPackages.TiedfixPkg tiedfixPkg : tiedfixPkgList)
        {
            seekPackage(tiedfixPkg.getPackageName(), tiedfixPkg.getClassTiedfix());
        }
    }


    private void seekPackage(String packageStr, SeekPackages.Tiedfix classTiedfix) throws FatalInitException
    {
        LOGGER.debug("***********");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            try
            {
                Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                mayAddPorterOfClass(clazz, null, classTiedfix);
            } catch (FatalInitException e)
            {
                throw e;
            } catch (Exception e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    private boolean willSeek(Class<?> clazz)
    {
        boolean willSeek = true;
        Enumeration<OnPorterAddListener> enumeration = listenerAdder.listeners(1);
        while (enumeration.hasMoreElements())
        {
            OnPorterAddListener onPorterAddListener = enumeration.nextElement();
            if (onPorterAddListener.onSeeking(porterConf.getOftenContextName(), clazz))
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

    private void mayAddPorterOfClass(Class<?> clazz, Object objectPorter,
            SeekPackages.Tiedfix classTiedfix) throws FatalInitException, Exception
    {
        if (PortUtil.isJustPortInClass(clazz) && willSeek(clazz))
        {
            LOGGER.debug("will add porter:{}({})", clazz, objectPorter);
            SrcPorter srcPorter = new SrcPorter(clazz, objectPorter);
            srcPorter.classTiedfix = classTiedfix;
            class_porterMap.put(clazz, srcPorter);
            //addPorter(clazz, objectPorter, autoSetHandle, sthDeal, portIniterList);
        } else if (!isMixinTo(clazz, objectPorter))
        {
            LOGGER.debug("will do static @{}:{}", AutoSet.class.getSimpleName(), clazz);
            mayAddStaticAutoSet(clazz);
        }
    }

    private boolean isMixinTo(Class<?> clazz, Object objectPorter)
    {
        MixinTo[] mixinTos = PortUtil.getMixinTos(clazz);
        for (MixinTo mixinTo : mixinTos)
        {
            _MixinPorter mixinPorter = new _MixinPorter(clazz, objectPorter, mixinTo.override(),
                    mixinTo.funTiedPrefix(), mixinTo.funTiedSuffix());
            Set<_MixinPorter> set = mixinToMap.get(mixinTo.toPorter());
            if (set == null)
            {
                set = new HashSet<>();
                mixinToMap.put(mixinTo.toPorter(), set);
            }
            LOGGER.debug("add to mixinToMap:{}", clazz);
            set.add(mixinPorter);
        }
        return mixinTos.length > 0;
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

        Porter porter = sthDeal.porter(srcPorter, mixinToMap, autoSetHandle);
        if (porter != null)
        {
            boolean willAdd = true;
            Enumeration<OnPorterAddListener> enumeration = listenerAdder.listeners(1);
            while (enumeration.hasMoreElements())
            {
                OnPorterAddListener onPorterAddListener = enumeration.nextElement();
                if (onPorterAddListener.onAdding(porterConf.getOftenContextName(), porter))
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
                        throw new InitException(
                                String.format("class tiedName '%s' added before.(current:%s,last:%s)", tiedName,
                                        clazz, portMap.get(tiedName).getClazz()));
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
                it.next().seekPortInit(porterConf.getOftenContextName(), portIniterList);
            }
        }
    }

    Porter getClassPort(String classTied)
    {
        return portMap.get(classTied);
    }


    public void initPorter(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        try
        {
            while (iterator.hasNext())
            {
                iterator.next().initArgsHandle(autoSetHandle.getArgumentsFactory());
            }
        } catch (Exception e)
        {
            throw new InitException(e);
        }


        //重要：在IArgumentsFactory之后初始化,用于支持：IExtraEntitySupport
        iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            //内部会调用addAutoSetsForNotPorter
            iterator.next().initOftenEntitiesHandle(extraEntityMap, sthDeal, innerContextBridge);
        }
    }

    public void start(OftenObject oftenObject)
    {
        OtherStartDestroy[] otherStartDestroys = otherStartList.toArray(new OtherStartDestroy[0]);
        Arrays.sort(otherStartDestroys);

        for (OtherStartDestroy otherStartDestroy : otherStartDestroys)
        {
            try
            {
                DefaultArgumentsFactory.invokeWithArgs(configData, otherStartDestroy.object,
                        otherStartDestroy.method, oftenObject, configData);
            } catch (Exception e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }
        }

        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().start(oftenObject, configData);
        }
    }

    public void destroy()
    {
        Iterator<Porter> iterator = portMap.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next().destroy();
        }
        onOtherDestroy();
    }

    @Override
    public boolean hasOtherStart()
    {
        return true;
    }

    @Override
    public void onOtherDestroy()
    {
        onOtherDestroy(configData, otherDestroyList.toArray(new OtherStartDestroy[0]));
    }

    public static void onOtherDestroy(IConfigData configData, OtherStartDestroy[] otherDestroys)
    {
        Arrays.sort(otherDestroys);
        for (OtherStartDestroy otherStartDestroy : otherDestroys)
        {
            try
            {
                Method method = otherStartDestroy.method;
                DefaultArgumentsFactory.invokeWithArgs(configData, otherStartDestroy.object, method, configData);
            } catch (Exception e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    Throwable throwable = OftenTool.unwrapThrowable(e);
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    CheckPassable getCheckPassable(Class<? extends CheckPassable> clazz)
    {
        return checkPassableForCF.get(clazz);
    }

    public IConfigData getConfigData()
    {
        return configData;
    }

    public IAutoSetter getAutoSetter()
    {
        return autoSetter;
    }
}
