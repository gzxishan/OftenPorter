package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.advanced.IAutoSetListener;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.SetOk;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal._AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.OftenContextInfo;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.exception.AutoSetException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.sysset.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetHandle
{

    public static class _SetOkObject implements Comparable<_SetOkObject>
    {
        private final Object obj;
        public final Method method;
        public final int priority;
        Logger logger;

        public _SetOkObject(Object obj, _SetOkObject ref)
        {
            this.obj = obj;
            this.method = ref.method;
            this.priority = ref.priority;
            this.logger = ref.logger;
        }

        public _SetOkObject(Object obj, Method method, int priority, Logger logger)
        {
            if (method != null)
            {
                method.setAccessible(true);
            }
            this.obj = obj;
            this.method = method;
            this.priority = priority;
            this.logger = logger;
        }

        @Override
        public int compareTo(_SetOkObject o)
        {
            int n = o.priority - priority;
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

        public void invoke(OftenObject oftenObject, IConfigData configData) throws Exception
        {
            logger.debug("invoke @SetOk:{}", method);
            method.setAccessible(true);
            DefaultArgumentsFactory.invokeWithArgs(configData, obj, method, oftenObject, configData);
        }
    }

    public static class _PortInited implements Comparable<_PortInited>
    {
        private final Object obj;
        public final Method method;
        public final int order;
        Logger logger;

        public _PortInited(Object obj, _PortInited ref)
        {
            this.obj = obj;
            this.method = ref.method;
            this.order = ref.order;
            this.logger = ref.logger;
        }

        public _PortInited(Object obj, Method method, int order, Logger logger)
        {
            if (method != null)
            {
                method.setAccessible(true);
            }
            this.obj = obj;
            this.method = method;
            this.order = order;
            this.logger = logger;
        }

        @Override
        public int compareTo(_PortInited o)
        {
            int n = this.order - o.order;
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

        public void invoke(OftenObject oftenObject, IConfigData configData) throws Exception
        {
            logger.debug("invoke @PortInited:{}", method);
            method.setAccessible(true);
            DefaultArgumentsFactory.invokeWithArgs(configData, obj, method, oftenObject, configData);
        }
    }

    enum RangeType
    {
        /**
         * 只针对static类型的成员。
         */
        STATIC,
        /**
         * 只针对实例类型。
         */
        INSTANCE,
        /**
         * 针对所有类型。
         */
        ALL
    }

    private interface IHandle
    {
        void handle() throws Exception;
    }

    /**
     * 处理{@linkplain AutoSetToThatForMixin}和{@linkplain AutoSetThatForMixin}
     */
    private class Handle_doAutoSetThatOfMixin implements IHandle
    {
        Object[] args;

        public Handle_doAutoSetThatOfMixin(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetThatOfMixin(Object obj1, Object obj2) throws AutoSetException
        {
            try
            {
                _doAutoSetThatOfMixin(obj1, obj2);
                _doAutoSetThatOfMixin(obj2, obj1);
            } catch (Exception e)
            {
                throw new AutoSetException(e);
            }
        }

        @Override
        public void handle() throws AutoSetException
        {
            this.doAutoSetThatOfMixin(args[0], args[1]);
        }
    }

    private class Handle_doAutoSetsForNotPorter implements IHandle
    {

        private Object[] objects;

        public Handle_doAutoSetsForNotPorter(Object[] objects)
        {
            this.objects = objects;
        }

        private void doAutoSetsForNotPorter(Object[] objects) throws Exception
        {
            for (Object obj : objects)
            {
                if (obj == null)
                {
                    continue;
                }
                doAutoSetForCurrent(false, obj, obj);
            }
        }

        @Override
        public void handle() throws Exception
        {
            this.doAutoSetsForNotPorter(objects);
        }
    }

    private class Handle_doAutoSetSeek implements IHandle
    {

        Object[] args;

        public Handle_doAutoSetSeek(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetSeek(List<String> packages, ClassLoader classLoader)
        {
            if (packages == null)
            {
                return;
            }
            try
            {
                for (int i = 0; i < packages.size(); i++)
                {
                    AutoSetHandle.this.doAutoSetSeek(packages.get(i), classLoader);
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle()
        {
            this.doAutoSetSeek((List) args[0], (ClassLoader) args[1]);
        }
    }

    private class Handle_doStaticAutoSet implements IHandle
    {

        Object[] args;

        public Handle_doStaticAutoSet(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetSeek(Collection<String> packages, Collection<String> classStrs,
                Collection<Class<?>> classes,
                ClassLoader classLoader)
        {
            if ((packages == null || packages.size() == 0) && (classStrs == null || classStrs.size() == 0) &&
                    (classes == null || classes.size() == 0))
            {
                return;
            }
            try
            {
                LOGGER.debug("*****StaticAutoSet******");
                if (packages != null)
                {
                    for (String packageStr : packages)
                    {
                        LOGGER.debug("扫描包：{}", packageStr);
                        List<String> pkgClasses = PackageUtil.getClassName(packageStr, classLoader);
                        for (int i = 0; i < pkgClasses.size(); i++)
                        {
                            Class<?> clazz = PackageUtil.newClass(pkgClasses.get(i), classLoader);
                            doAutoSetForCurrent(false, null, null, clazz, null, RangeType.STATIC);
                        }
                    }
                }
                if (classes != null)
                {
                    for (Class<?> clazz : classes)
                    {
                        doAutoSetForCurrent(false, null, null, clazz, null, RangeType.STATIC);
                    }
                }

                if (classStrs != null)
                {
                    for (String clazzStr : classStrs)
                    {
                        Class<?> clazz = null;
                        try
                        {
                            clazz = PackageUtil.newClass(clazzStr, classLoader);
                        } catch (Exception e)
                        {
                            LOGGER.error(e.getMessage(), e);
                        }
                        if (clazz != null)
                        {
                            doAutoSetForCurrent(false, null, null, clazz, null, RangeType.STATIC);
                        }
                    }
                }

            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle()
        {
            this.doAutoSetSeek((Collection<String>) args[0], (Collection) args[1], (Collection) args[2],
                    (ClassLoader) args[3]);
        }
    }

    private class Handle_doAutoSetForPorter implements IHandle
    {
        Object[] args;

        public Handle_doAutoSetForPorter(Object... args)
        {
            this.args = args;
        }

        private void doAutoSetForPorter(Porter porter) throws Exception
        {
            Object obj = doAutoSetForCurrent(true, porter, porter.getFinalPorterObject(), porter.getClazz(),
                    porter.getObj(),
                    RangeType.ALL);
            porter.setObj(obj);
        }

        @Override
        public void handle() throws Exception
        {
            this.doAutoSetForPorter((Porter) args[0]);
        }
    }


    private final Logger LOGGER;

    private InnerContextBridge innerContextBridge;
    private IArgumentsFactory argumentsFactory;
    private Delivery thisDelivery;
    private PorterData porterData;
    private List<IHandle> iHandles_porter = new ArrayList<>();
    private List<IHandle> iHandles_notporter = new ArrayList<>();
    private List<IHandle> iHandlesForAutoSetThat = new ArrayList<>();
    private Map<Class, Porter> porterMap = new HashMap<>();
    private Map<Object, Object> proxyObjectMap = new HashMap<>();

    private List<_SetOkObject> setOkObjects = new ArrayList<>();
    private List<_PortInited> portIniteds = new ArrayList<>();

    private Set<Object> autoSetDealtSet = new HashSet<>();
    private AutoSetHandleWorkedInstance workedInstance;
    private PorterConf porterConf;
    private IConfigData iConfigData;
    private OftenContextInfo oftenContextInfo;
    private IOtherStartDestroy iOtherStartDestroy;
    private IAutoVarGetter autoVarGetter;
    private CheckerBuilder checkerBuilder;
    private IAutoSetter autoSetter;

    /////////////////////////////////
    private boolean useCache;
    private Map<Class, List<Object>> setOkOrPortInitCacheMap;
    private Map<Class, Method[]> startsCacheMap, destroysCacheMap;
    //已经添加了的
    private Set<Class> addedAutoSetStaticClasses = new HashSet<>();

    /////////////////////////////////

    private AutoSetHandle(PorterConf porterConf, IArgumentsFactory argumentsFactory,
            InnerContextBridge innerContextBridge, CheckerBuilder checkerBuilder,
            Delivery thisDelivery, PorterData porterData, AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal,
            String contextName)
    {
        this.porterConf = porterConf;
        this.iConfigData = porterConf.getConfigData();
        this.argumentsFactory = argumentsFactory;
        this.innerContextBridge = innerContextBridge;
        this.checkerBuilder = checkerBuilder;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        this.workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
        this.oftenContextInfo = new OftenContextInfo(thisDelivery.currentName(), contextName);
        LOGGER = LogUtil.logger(AutoSetHandle.class);
    }

    public boolean isUseCache()
    {
        return useCache;
    }

    public void setUseCache(boolean useCache)
    {
        this.useCache = useCache;
        if (useCache)
        {
            if (setOkOrPortInitCacheMap == null)
            {
                setOkOrPortInitCacheMap = new HashMap<>();
            }
            if (startsCacheMap == null)
            {
                startsCacheMap = new HashMap<>();
            }
            if (destroysCacheMap == null)
            {
                destroysCacheMap = new HashMap<>();
            }
        }
    }

    public void clearCache()
    {
        if (setOkOrPortInitCacheMap != null)
        {
            setOkOrPortInitCacheMap.clear();
            setOkOrPortInitCacheMap = null;
        }
        if (startsCacheMap != null)
        {
            startsCacheMap.clear();
            startsCacheMap = null;
        }
        if (destroysCacheMap != null)
        {
            destroysCacheMap.clear();
            destroysCacheMap = null;
        }
    }

    public IAutoVarGetter getAutoVarGetter()
    {
        return autoVarGetter;
    }

    public IConfigData getConfigData()
    {
        return iConfigData;
    }

    public void setAutoVarGetter(IAutoVarGetter autoVarGetter)
    {
        this.autoVarGetter = autoVarGetter;
    }

    public void setAutoSetter(IAutoSetter autoSetter)
    {
        this.autoSetter = autoSetter;
    }

    public void invokePortInited(OftenObject oftenObject)
    {
        try
        {
            if (portIniteds != null)
            {
                _PortInited[] portIniteds = this.portIniteds.toArray(new _PortInited[0]);
                Arrays.sort(portIniteds);
                for (_PortInited portInited : portIniteds)
                {
                    portInited.invoke(oftenObject, iConfigData);
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.portIniteds = null;
        }
    }

    /**
     * 调用所有的{@linkplain SetOk SetOk}函数。
     */
    public synchronized void invokeSetOk(OftenObject oftenObject)
    {

        _SetOkObject[] setOkObjects = this.setOkObjects.toArray(new _SetOkObject[0]);
        Arrays.sort(setOkObjects);
        //先清理、再调用setOk函数
        this.setOkObjects.clear();
//            this.porterMap = null;
//            this.proxyObjectMap = null;
        this.iHandles_notporter.clear();
        this.iHandles_porter.clear();
        this.iHandlesForAutoSetThat = null;
        this.innerContextBridge.annotationDealt.clearCache();
        autoSetDealtSet.clear();
//            autoSetDealtSet = null;

        for (_SetOkObject setOkObject : setOkObjects)
        {
            try
            {
                setOkObject.invoke(oftenObject, iConfigData);
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static AutoSetHandle newInstance(PorterConf porterConf, IArgumentsFactory argumentsFactory,
            InnerContextBridge innerContextBridge, CheckerBuilder checkerBuilder,
            Delivery thisDelivery,
            PorterData porterData, AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal, String currentContextName)
    {
        return new AutoSetHandle(porterConf, argumentsFactory, innerContextBridge, checkerBuilder, thisDelivery,
                porterData,
                autoSetObjForAspectOfNormal, currentContextName);
    }


    public <T> T getContextObject(Class<?> key)
    {
        return getContextObject(key.getName());
    }

    public <T> T getContextObject(String key)
    {
        return (T) innerContextBridge.getContextSet(key);
    }

    public void setIOtherStartDestroy(IOtherStartDestroy iOtherStartDestroy)
    {
        this.iOtherStartDestroy = iOtherStartDestroy;
    }

    public IOtherStartDestroy getOtherStartDestroy()
    {
        return iOtherStartDestroy;
    }

    public InnerContextBridge getInnerContextBridge()
    {
        return innerContextBridge;
    }

    public IArgumentsFactory getArgumentsFactory()
    {
        return argumentsFactory;
    }

    public OftenContextInfo getOftenContextInfo()
    {
        return oftenContextInfo;
    }

    public synchronized void addAutoSetSeekPackages(List<String> packages, ClassLoader classLoader)
    {
        iHandles_notporter.add(new Handle_doAutoSetSeek(packages, classLoader));
    }

    public synchronized void addStaticAutoSet(Collection<String> packages, Collection<String> classStrs,
            Collection<Class> classes,
            ClassLoader classLoader)
    {
        if (OftenTool.isEmptyOf(packages) && OftenTool.isEmptyOf(classStrs) && OftenTool
                .isEmptyOf(classes))
        {
            return;
        }
        iHandles_notporter.add(new Handle_doStaticAutoSet(packages, classStrs, classes, classLoader));
    }

    private void doAutoSetSeek(String packageStr, ClassLoader classLoader) throws Exception
    {
        LOGGER.debug("*****autoSetSeek******");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++)
        {
            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
            doAutoSetForCurrent(true, null, null, clazz, null, RangeType.STATIC);
            if (clazz.isAnnotationPresent(AutoSetSeek.class))
            {
                Object object = clazz.newInstance();
                doAutoSetForCurrent(true, null, object, clazz, object, RangeType.INSTANCE);
            }
        }
    }


    public synchronized void addAutoSetsForNotPorter(Collection objects)
    {
        addAutoSetsForNotPorter(objects.toArray(OftenTool.EMPTY_OBJECT_ARRAY));
    }

    public synchronized void addAutoSetsForNotPorter(Object[] objects)
    {
        iHandles_notporter.add(new Handle_doAutoSetsForNotPorter(objects));
    }

    synchronized Object addAutoSetForPorter(Porter porter) throws Exception
    {
        Object porterObject = porter.getObj();
        if (porterObject == null)
        {
            porterObject = workedInstance.newAndProxy(porter.getClazz(), this);
        }
        iHandles_porter.add(new Handle_doAutoSetForPorter(porter));
        porterMap.put(porter.getPortIn().getToPorterKey(), porter);
        porterMap.putAll(porter.getMixinToThatCouldSet());

        List<PortIn.ContextSet> contextSets = AnnoUtil
                .getAnnotationsWithSuper(porter.getClazz(), PortIn.ContextSet.class);
        for (PortIn.ContextSet contextSet : contextSets)
        {
            if (OftenTool.isEmpty(contextSet.value()))
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("@{} value of {} is empty!", PortIn.ContextSet.class.getSimpleName(),
                            porter.getClazz());
                }
                continue;
            }
            Object last = innerContextBridge.putContextSet(contextSet.value(), porterObject);
            if (last != null && LOGGER.isWarnEnabled())
            {
                LOGGER.warn("override by @{}:key={},newValue={},oldValue={}", PortIn.ContextSet.class.getSimpleName(),
                        contextSet.value(),
                        porter.getObj(), last);
            }
        }

        return porterObject;
    }

    public synchronized void addAutoSetThatOfMixin(Object porter1, Object porter2)
    {
        iHandlesForAutoSetThat.add(new Handle_doAutoSetThatOfMixin(porter1, porter2));
    }


    public Object doAutoSetMayProxy(Object object)
    {
        try
        {
            return doAutoSetForCurrent(true, object, object);
        } catch (AutoSetException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new AutoSetException(e);
        } finally
        {
            workedInstance.clear();
            addedAutoSetStaticClasses.clear();
        }
    }


    public synchronized void doAutoSetNormal() throws AutoSetException
    {
        try
        {
            for (int i = 0; i < iHandles_notporter.size(); i++)
            {
                iHandles_notporter.get(i).handle();
            }

            iHandles_notporter.clear();

            for (int i = 0; i < iHandles_porter.size(); i++)
            {
                iHandles_porter.get(i).handle();
            }

            //再执行一篇，可能添加了AutoSetStatic
            for (int i = 0; i < iHandles_notporter.size(); i++)
            {
                iHandles_notporter.get(i).handle();
            }

        } catch (AutoSetException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new AutoSetException(e);
        } finally
        {
            workedInstance.clear();
            iHandles_notporter.clear();
            iHandles_porter.clear();
            addedAutoSetStaticClasses.clear();
        }
    }

    public synchronized void doAutoSetThat() throws AutoSetException
    {
        try
        {
//            workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
            for (int i = 0; i < iHandlesForAutoSetThat.size(); i++)
            {
                iHandlesForAutoSetThat.get(i).handle();
            }
        } catch (AutoSetException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new AutoSetException(e);
        } finally
        {
            workedInstance.clear();
            iHandlesForAutoSetThat.clear();
        }
    }

    private void _doAutoSetThatOfMixin(Object objectForGet, Object objectForSet) throws Exception
    {
        objectForGet = mayGetProxyObject(objectForGet);
        objectForSet = mayGetProxyObject(objectForSet);

        Map<String, Field> fromGet = new HashMap<>();

        Field[] fieldsGet = OftenTool.getAllFields(PortUtil.getRealClass(objectForGet));
        for (Field field : fieldsGet)
        {
            AutoSetToThatForMixin autoSetToThatForMixin = AnnoUtil.getAnnotation(field, AutoSetToThatForMixin.class);
            if (autoSetToThatForMixin == null)
            {
                continue;
            }
            String key;
            if (autoSetToThatForMixin.key().equals("") && autoSetToThatForMixin.value().equals(AutoSet.class))
            {
//                throw new InitException("the key of annotation " + AutoSetToThatForMixin.class
//                        .getSimpleName() + " is empty  for field '" + field + "'");
                key = AnnoUtil.Advance.getRealTypeOfField(objectForGet.getClass(), field).getName();
            } else if (OftenTool.notEmpty(autoSetToThatForMixin.key()))
            {
                key = autoSetToThatForMixin.key();
            } else
            {
                key = autoSetToThatForMixin.value().getName();
            }

            if (fromGet.containsKey(key))
            {
                LOGGER.warn("already exists key '{}', current field is [{}],last field is [{}]", field,
                        fromGet.get(key));
            }
            fromGet.put(key, field);
        }

        Field[] fields = OftenTool.getAllFields(PortUtil.getRealClass(objectForSet));
        for (Field field : fields)
        {
            AutoSetThatForMixin autoSetThatForMixin = AnnoUtil.getAnnotation(field, AutoSetThatForMixin.class);
            if (autoSetThatForMixin == null)
            {
                continue;
            }
            field.setAccessible(true);
            if (field.get(objectForSet) != null)
            {//忽略不为null的
                continue;
            }

            Object value;
            if (autoSetThatForMixin.value().equals(AutoSet.class) && autoSetThatForMixin.key().equals(""))
            {
                value = objectForGet;
            } else
            {
                String key = autoSetThatForMixin.key().equals("") ? autoSetThatForMixin.value()
                        .getName() : autoSetThatForMixin.key();
                Field getField = fromGet.get(key);
                if (getField == null && autoSetThatForMixin.searchChild())
                {
                    //尝试查找子类型
                    Class type = AnnoUtil.Advance.getRealTypeOfField(objectForSet.getClass(), field);
                    for (Field f : fromGet.values())
                    {
                        if (OftenTool
                                .isAssignable(AnnoUtil.Advance.getRealTypeOfField(objectForGet.getClass(), f), type))
                        {
                            getField = f;
                            break;
                        }
                    }
                }

                if (getField == null)
                {
                    value = null;
                } else
                {
                    getField.setAccessible(true);
                    value = getField.get(objectForGet);
                }
                if (value == null && autoSetThatForMixin.required())
                {
                    throw new InitException("need transfer field from '" + objectForGet
                            .getClass() + "' with key '" + key + "' to set field '" + field + "'");
                }
            }
            if (value == null || !OftenTool.isAssignable(value.getClass(), field.getType()))
            {//忽略非继承关系的。
                continue;
            }
            field.set(objectForSet, value);
            LOGGER.debug("AutoSet.AutoSetThatForMixin:[{}] with [{}]", field, value);
        }
    }

    Object doAutoSetForCurrent(boolean doProxyCurrent, @MayNull Object finalObject,
            Object currentObject) throws Exception
    {
        return doAutoSetForCurrent(doProxyCurrent, null, finalObject, PortUtil.getRealClass(currentObject),
                currentObject,RangeType.ALL);
    }

    private Object mayGetProxyObject(Object object)
    {
        if (object != null)
        {
            Object obj = proxyObjectMap.get(object);
            if (obj != null)
            {
                object = obj;
            }
        }
        return object;
    }

    void putProxyObject(Object origin, Object proxy)
    {
        if (origin != null)
        {
            proxyObjectMap.put(origin, proxy);
        }
    }

    private void doAutoSetPut(Field field, Object obj, Class realType)
    {
        if (obj != null)
        {
            AutoSet.Put put = AnnoUtil.getAnnotation(field, AutoSet.Put.class);
            if (put != null)
            {
                //Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
                String name = put.name().equals("") ? realType.getName() : put.name();
                if (put.range() == AutoSet.Range.Global)
                {
                    innerContextBridge.innerBridge.putGlobalSet(name, obj);
                } else
                {
                    innerContextBridge.putContextSet(name, obj);
                }
            }
        }
    }

    //每个对象或被autoset的都会调用此函数
    private Object doAutoSetForCurrent(boolean doProxyCurrent, @MayNull Porter porter, @MayNull Object finalObject,
            Class<?> currentObjectClass, @MayNull Object currentObject, RangeType rangeType) throws Exception
    {

        //处理AutoSetStatic
        List<AutoSetStatic> autoSetStatics = AnnoUtil.getAnnotationsWithSuper(currentObjectClass, AutoSetStatic.class);
        for (AutoSetStatic autoSetStatic : autoSetStatics)
        {
            Set<Class> set = new HashSet<>();
            for (Class clazz : autoSetStatic.value())
            {
                if (!addedAutoSetStaticClasses.contains(clazz))
                {
                    addedAutoSetStaticClasses.add(clazz);
                    set.add(clazz);
                }
            }
            if (set.size() > 0)
            {
                addStaticAutoSet(null, null, set, null);
            }
        }

        finalObject = mayGetProxyObject(finalObject);
        currentObject = mayGetProxyObject(currentObject);
        if (currentObject != null && autoSetDealtSet
                .contains(currentObject) || currentObject == null && autoSetDealtSet.contains(currentObjectClass))
        {
            LOGGER.debug("already do autoset of:class={},object={}", currentObjectClass, currentObject);
            return currentObject;
        }
        autoSetDealtSet.add(currentObjectClass);
        if (currentObject != null)
        {
            autoSetDealtSet.add(currentObject);
        }

        AutoSetHandleWorkedInstance.Result result;
        if (currentObject == null)
        {
            result = workedInstance.workInstance(currentObjectClass, this, false);
        } else
        {
            result = workedInstance.workInstance(currentObject, this, doProxyCurrent);
            currentObject = result.object;
        }

        if (result.isWorked)
        {
            return currentObject;//已经递归扫描过该实例
        }

        try
        {
            porterConf.seekImporter(new Class[]{currentObjectClass});
        } catch (AutoSetException | InitException e)
        {
            throw e;
        } catch (Throwable e)
        {
            LOGGER.warn("seek importer failed:class={},ex={}", currentObjectClass, e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }

        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;

        IConfigData configData = getContextObject(IConfigData.class);

        Field[] fields = OftenTool.getAllFields(currentObjectClass);
        LOGGER.debug("autoSetSeek:class={},instance={},{}", currentObjectClass.getName(), currentObject,
                currentObject == null ? "" : currentObject.hashCode());
        RuntimeException thr = null;
        for (int i = 0; i < fields.length; i++)
        {
            Field f = fields[i];

            if (Modifier.isStatic(f.getModifiers()))
            {
                if (rangeType == RangeType.INSTANCE)
                {
                    continue;
                }
            } else
            {
                if (rangeType == RangeType.STATIC)
                {
                    continue;
                }
            }

            Class fieldRealType = null;
            f.setAccessible(true);
            Property property = AnnoUtil.getAnnotation(f, Property.class);
            if (property != null)
            {
                fieldRealType = AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
                Object value = configData.getValue(currentObject, currentObjectClass, f, null, fieldRealType, property);
                if (value != null)
                {
                    f.set(currentObject, value);
                }
                doAutoSetPut(f, value, fieldRealType);
                continue;
            }

            _AutoSet autoSet = annotationDealt.autoSet(currentObjectClass, f);
            if (autoSet == null)
            {
                Object value = f.get(currentObject);
                if (value != null && f.isAnnotationPresent(AutoSetSeek.class))
                {
                    Object newValue = workedInstance.doProxy(value, this);
                    if (newValue != value)
                    {
                        f.set(currentObject, newValue);
                        value = newValue;
                    }
                    workedInstance.doAutoSet(value, this);
                }

                if (fieldRealType == null)
                {
                    fieldRealType = AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
                }
                doAutoSetPut(f, value, fieldRealType);
                continue;
            }


            try
            {
                if (isDefaultAutoSetObject(f, porter, finalObject, currentObjectClass, currentObject, autoSet))
                {
                    continue;
                }
                Object value = f.get(currentObject);
                if (fieldRealType == null)
                {
                    fieldRealType = AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
                }

                String keyName;
                Class<?> mayNew = null;
                Class<?> classClass = autoSet.classValue();
                if (classClass.equals(AutoSet.class))
                {
                    keyName = autoSet.value();
                } else
                {
                    keyName = classClass.getName();
                    mayNew = classClass;
                }
                if ("".equals(keyName))
                {
                    keyName = fieldRealType.getName();
                }
                if (mayNew == null)
                {
                    mayNew = fieldRealType;
                }

                if (value != null)
                {
                    LOGGER.debug("ignore field [{}] for it's not null:{}", f, value);
                    //！！！忽略了非null的成员
                } else
                {
                    value = getFieldObject(currentObject, currentObjectClass, keyName, f, mayNew, autoSet);
                }

                value = doAutoSetDefaultDealt(autoSet, finalObject, currentObjectClass, currentObject, f, value);

                if (value == null && !autoSet.nullAble())
                {
                    thr = new RuntimeException(String.format("AutoSet:could not set [%s] with null!", f));
                    break;
                }


                boolean willSet = true;
                IAutoSetListener.Will lastWill = new IAutoSetListener.Will(willSet);
                lastWill.optionValue = value;

                for (IAutoSetListener listener : innerContextBridge.autoSetListeners)
                {
                    lastWill.willSet = willSet;
                    lastWill.optionValue = value;
                    IAutoSetListener.Will will = listener.willSet(autoSet, currentObjectClass, currentObject, f,
                            fieldRealType, lastWill);
                    boolean b = will == null || will.willSet;
                    willSet = willSet && b;
                    if (will != null && will.optionValue != null)
                    {
                        value = will.optionValue;
                    }
                }
                value = workedInstance.doProxy(value, this);
                saveFieldObject(keyName, value, autoSet);
                doAutoSetPut(f, value, fieldRealType);
                if (willSet)
                {
                    f.set(currentObject, value);//设置变量
                    if (autoSet.willRecursive())
                    {
                        workedInstance.doAutoSet(value, this);//递归设置
                    }
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("AutoSet:[{}] with [{}],realType=[{}]", f, value, fieldRealType);
                    }
                } else
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("ignore AutoSet:[{}] ignored value [{}],realType=[{}]", f, value, fieldRealType);
                    }
                }

            } catch (AutoSetException | InitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.warn("AutoSet failed for [{}]({}),ex={}", f, autoSet.range(), e.getMessage());
                LOGGER.error(e.getMessage(), e);
            }

        }
        if (thr != null)
        {
            throw thr;
        } else
        {
            if (useCache)
            {
                List<Object> list = setOkOrPortInitCacheMap.get(currentObjectClass);
                if (list != null)
                {
                    for (Object setOkOrInit : list)
                    {
                        if (setOkOrInit instanceof _SetOkObject)
                        {
                            _SetOkObject setOkObject = (_SetOkObject) setOkOrInit;
                            this.setOkObjects.add(new _SetOkObject(currentObject, setOkObject));
                        } else
                        {
                            _PortInited portInited = (_PortInited) setOkOrInit;
                            this.portIniteds.add(new _PortInited(currentObject, portInited));
                        }
                    }
                } else
                {
                    list = new ArrayList<>(2);
                    Method[] methods = OftenTool.getAllPublicMethods(currentObjectClass);
                    for (Method method : methods)
                    {
                        Object setOkOrInit = addSetOkAndPorterInitedMethod(currentObject, method);
                        if (setOkOrInit != null)
                        {
                            list.add(setOkOrInit);
                        }
                    }
                    setOkOrPortInitCacheMap.put(currentObjectClass, list);
                }
            } else
            {
                Method[] methods = OftenTool.getAllPublicMethods(currentObjectClass);
                for (Method method : methods)
                {
                    addSetOkAndPorterInitedMethod(currentObject, method);
                }
            }


            if (porter == null && !PortUtil.isPorter(currentObjectClass))
            {
                addOtherStartDestroy(currentObject, currentObjectClass);
            }
        }
        return currentObject;
    }

    private Object getFieldObject(Object currentObject, Class currentObjectClass, String keyName, Field field,
            Class<?> mayNew, _AutoSet autoSet) throws Exception
    {
        Object value = null;

        {//先获取
            switch (autoSet.range())
            {
                case Global:
                {
                    value = innerContextBridge.innerBridge.getGlobalSet(keyName);
                    if (value == null && !OftenTool.isInterfaceOrAbstract(mayNew))
                    {
                        value = workedInstance.newAndProxy(mayNew, this);
                        if (value == null)
                        {
                            LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                        }
                    }
                }
                break;
                case Context:
                {
                    value = innerContextBridge.getContextSet(keyName);
                    if (value == null)
                    {
                        Porter thePorter = porterMap.get(mayNew);
                        if (thePorter != null)
                        {
                            value = thePorter.getObj();
                        }
                    }
                    if (value == null && !OftenTool.isInterfaceOrAbstract(mayNew))
                    {
                        value = workedInstance.newAndProxy(mayNew, this);
                        if (value == null)
                        {
                            LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                        }
                    }
                }
                break;
                case New:
                {
                    value = workedInstance.newAndProxy(mayNew, this);
                    if (value == null)
                    {
                        LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                    }
                }
                break;
            }
        }

        if (value == null)
        {//再生成
            value = doAutoSetGen(autoSet, currentObjectClass, currentObject, field);
        }


        if (value == null)
        {
            if (!autoSet.nullAble())
            {
                throw new RuntimeException(String.format("AutoSet:could not set [%s] with null!", field));
            }
        }

        return value;
    }

    private void saveFieldObject(String keyName, Object value, _AutoSet autoSet)
    {
        if (autoSet.isWillSave() && value != null && autoSet.notNullPut())
        {
            //Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
            //Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
            switch (autoSet.range())
            {
                case Global:
                    innerContextBridge.innerBridge.putGlobalSet(keyName, value);
                    break;
                case Context:
                    innerContextBridge.putContextSet(keyName, value);
                    break;
                case New:
                    break;
            }
        }
    }


    private Object addSetOkAndPorterInitedMethod(Object currentObject, Method method)
    {
        SetOk setOk = AnnoUtil.getAnnotation(method, SetOk.class);
        PortInited portInited;
        Object setOkOrInit = null;
        if (setOk != null)
        {
            if (currentObject == null && !Modifier.isStatic(method.getModifiers()))
            {
                LOGGER.warn("ignore SetOk method for no instance:method={}", method);
            } else
            {
                setOkOrInit = new _SetOkObject(currentObject, method, setOk.priority(), LOGGER);
                setOkObjects.add((_SetOkObject) setOkOrInit);
            }
        } else if ((portInited = AnnoUtil.getAnnotation(method, PortInited.class)) != null)
        {
            if (currentObject == null && !Modifier.isStatic(method.getModifiers()))
            {
                LOGGER.warn("ignore PortInited method for no instance:method={}", method);
            } else
            {
                setOkOrInit = new _PortInited(currentObject, method, portInited.order(), LOGGER);
                portIniteds.add((_PortInited) setOkOrInit);
            }
        }
        return setOkOrInit;
    }

    /**
     * 用于生成注入对象。
     *
     * @param autoSet
     * @param currentObject
     * @param field
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object doAutoSetGen(_AutoSet autoSet, Class<?> currentObjectClass, Object currentObject,
            Field field) throws Exception
    {
        Class<? extends AutoSetGen> genClass = autoSet.gen();
        String option = autoSet.option();
        if (genClass.equals(AutoSetGen.class))
        {
            AutoSetDefaultDealt autoSetDefaultDealt = AnnoUtil.
                    getAutoSetDefaultDealt(field, currentObjectClass);
            if (autoSetDefaultDealt != null)
            {
                genClass = autoSetDefaultDealt.gen();
                if ("".equals(option))
                {
                    option = autoSetDefaultDealt.option();
                }
            }
        }
        if (genClass.equals(AutoSetGen.class))
        {
            return null;
        }

        AutoSetGen autoSetGen = OftenTool.newObject(genClass);
        addOtherStartDestroy(autoSetGen, genClass);
        autoSetGen = (AutoSetGen) doAutoSetForCurrent(true, autoSetGen, autoSetGen);
        Object value = autoSetGen.genObject(iConfigData, currentObjectClass, currentObject, field,
                AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, field), autoSet, option);
        return value;
    }

    private void addOtherStartDestroy(@MayNull Object object, Class objectClass)
    {
        if (iOtherStartDestroy != null)
        {
            if (iOtherStartDestroy.hasOtherStart())
            {
                Method[] starts = null;
                if (useCache)
                {
                    starts = startsCacheMap.get(objectClass);
                }

                if (starts == null)
                {
                    starts = innerContextBridge.annotationDealt.getPortStart(object, objectClass);
                    if (useCache)
                    {
                        startsCacheMap.put(objectClass, starts);
                    }
                }
                iOtherStartDestroy.addOtherStarts(object, starts);
            }
            Method[] destroys = null;


            if (useCache)
            {
                destroys = destroysCacheMap.get(objectClass);
            }

            if (destroys == null)
            {
                destroys = innerContextBridge.annotationDealt.getPortDestroy(object, objectClass);
                if (useCache)
                {
                    destroysCacheMap.put(objectClass, destroys);
                }
            }

            iOtherStartDestroy.addOtherDestroys(object, destroys);
        }
    }

    private Object doAutoSetDefaultDealt(_AutoSet autoSet, @MayNull Object finalObject, Class<?> currentObjectClass,
            Object currentObject,
            Field field,
            Object value) throws Exception
    {
        Class<? extends AutoSetDealt> autoSetDealtClass = autoSet.dealt();
        String option = autoSet.option();
        if (autoSetDealtClass.equals(AutoSetDealt.class))
        {
            AutoSetDefaultDealt autoSetDefaultDealt = AnnoUtil.
                    getAutoSetDefaultDealt(field, currentObjectClass);
            if (autoSetDefaultDealt != null)
            {
                autoSetDealtClass = autoSetDefaultDealt.dealt();
                if ("".equals(option))
                {
                    option = autoSetDefaultDealt.option();
                }
            }
        }
        if (autoSetDealtClass.equals(AutoSetDealt.class))
        {
            return value;
        }
        AutoSetDealt autoSetDealt = OftenTool.newObject(autoSetDealtClass);
        addOtherStartDestroy(autoSetDealt, autoSetDealtClass);
        autoSetDealt = (AutoSetDealt) doAutoSetForCurrent(true, autoSetDealt, autoSetDealt);
        Object finalValue = autoSetDealt.deal(iConfigData, finalObject, currentObjectClass, currentObject, field,
                AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, field), value, autoSet, option);
        return finalValue;
    }

    /**
     * 是否是默认工具类。
     */
    private boolean isDefaultAutoSetObject(Field f, Porter porter, Object finalObject, Class<?> currentObjectClass,
            @MayNull Object currentObject, _AutoSet autoSet) throws Exception

    {
        Object sysset = null;
        String typeName = f.getType().getName();
        if (typeName.equals(IAutoVarGetter.class.getName()))
        {
            sysset = autoVarGetter;
        } else if (typeName.equals(IAutoSetter.class.getName()))
        {
            sysset = autoSetter;
        } else if (typeName.equals(CheckerBuilder.class.getName()))
        {
            sysset = checkerBuilder;
        } else if (typeName.equals(IConfigData.class.getName()))
        {
            sysset = iConfigData;
        } else if (typeName.equals(TypeTo.class.getName()))
        {
            sysset = new TypeTo(innerContextBridge);
        } else if (typeName.equals(PorterData.class.getName()))
        {
            sysset = porterData;
        } else if (typeName.equals(Logger.class.getName()))
        {
            sysset = LogUtil.logger(currentObjectClass);
        } else if (typeName.equals(PorterSync.class.getName())
                || typeName.equals(PorterNotInnerSync.class.getName())
                || typeName.equals(PorterThrowsSync.class.getName()))
        {
            boolean isInner = !typeName.equals(PorterNotInnerSync.class.getName());

            PorterParamGetterImpl porterParamGetter = new PorterParamGetterImpl();
            porterParamGetter.setContext(getOftenContextInfo().getContextName());

            if (porter == null)
            {
                LOGGER.debug("auto set {} in not porter[{}]",
                        (isInner ? PorterSync.class : PorterNotInnerSync.class).getSimpleName(), currentObjectClass);
                //throw new FatalInitException(PorterSync.class.getSimpleName() + "just allowed in porter!");
                if (!f.isAnnotationPresent(PorterSyncOption.class) && finalObject != null)
                {
                    porterParamGetter.setClassTied(PortUtil.tied(PortUtil.getRealClass(finalObject)));
                }
            } else
            {
                porterParamGetter.setClassTied(PortUtil.tied(PortUtil.getRealClass(porter.getFinalPorterObject())));
            }

            _SyncPorterOption syncPorterOption = innerContextBridge.annotationDealt
                    .syncPorterOption(f, porterParamGetter);

            porterParamGetter.check();
            sysset = new SyncPorterImpl(syncPorterOption, isInner);
            if (typeName.equals(PorterThrowsSync.class.getName()))
            {
                sysset = new SyncPorterThrowsImpl((SyncPorterImpl) sysset);
            }
        } else if (typeName.equals(Delivery.class.getName()))
        {
            String pName = autoSet.value();
            Delivery delivery;
            if (OftenTool.isEmpty(pName))
            {
                delivery = thisDelivery;
            } else
            {
                CommonMain commonMain = PorterMain.getMain(pName);
                if (commonMain == null)
                {
                    throw new Error(
                            String.format("%s object is null for %s[%s]!", AutoSet.class.getSimpleName(), f, pName));
                }
                delivery = commonMain.getBridgeLinker();
            }

            sysset = delivery;
        }
        //sysset = dealtAutoSet(autoSet, object, f, sysset);
        if (sysset != null)
        {
            f.set(currentObject, sysset);
            LOGGER.debug("AutoSet [{}] with default object [{}]", f, sysset);
            sysset = doAutoSetForCurrent(true, sysset, sysset);//递归：设置被设置的变量。
        }
        return sysset != null;
    }
}
