package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IAutoSetListener;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.SetOk;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal._AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.base.OftenContextInfo;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.AutoSetException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetHandle
{

    public static class _SetOkObject implements Comparable<_SetOkObject>
    {
        public final Object obj;
        public final Method method;
        public final int priority;
        Logger logger;

        public _SetOkObject(Object obj, Method method, int priority, Logger logger)
        {
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

        private void doAutoSetSeek(List<String> packages, List<String> classStrs, List<Class<?>> classes,
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
                    for (int k = 0; k < packages.size(); k++)
                    {
                        String packageStr = packages.get(k);
                        LOGGER.debug("扫描包：{}", packageStr);
                        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
                        for (int i = 0; i < classeses.size(); i++)
                        {
                            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                            doAutoSetForCurrent(false, null, null, clazz, null, RangeType.STATIC);
                        }
                    }
                }
                if (classes != null)
                {
                    for (int k = 0; k < classes.size(); k++)
                    {
                        Class<?> clazz = classes.get(k);
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
            this.doAutoSetSeek((List) args[0], (List) args[1], (List) args[2], (ClassLoader) args[3]);
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
    private Set<Object> autoSetDealtSet = new HashSet<>();
    private AutoSetHandleWorkedInstance workedInstance;
    private IConfigData iConfigData;
    private OftenContextInfo oftenContextInfo;
    private IOtherStartDestroy iOtherStartDestroy;
    private IAutoVarGetter autoVarGetter;
    private IAutoSetter autoSetter;

    private AutoSetHandle(IConfigData iConfigData, IArgumentsFactory argumentsFactory,
            InnerContextBridge innerContextBridge,
            Delivery thisDelivery, PorterData porterData, AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal,
            String contextName)
    {
        this.iConfigData = iConfigData;
        this.argumentsFactory = argumentsFactory;
        this.innerContextBridge = innerContextBridge;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        this.workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
        this.oftenContextInfo = new OftenContextInfo(thisDelivery.currentName(), contextName);
        LOGGER = LogUtil.logger(AutoSetHandle.class);
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

    /**
     * 调用所有的{@linkplain SetOk SetOk}函数。
     */
    public synchronized void invokeSetOk(OftenObject oftenObject)
    {
        _SetOkObject[] setOkObjects = this.setOkObjects.toArray(new _SetOkObject[0]);
        Arrays.sort(setOkObjects);
        try
        {
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
                setOkObject.invoke(oftenObject, iConfigData);
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static AutoSetHandle newInstance(IConfigData iConfigData, IArgumentsFactory argumentsFactory,
            InnerContextBridge innerContextBridge,
            Delivery thisDelivery,
            PorterData porterData, AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal, String currentContextName)
    {
        return new AutoSetHandle(iConfigData, argumentsFactory, innerContextBridge, thisDelivery, porterData,
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

    public synchronized void addStaticAutoSet(List<String> packages, List<String> classStrs, List<Class> classes,
            ClassLoader classLoader)
    {
        if (OftenTool.isEmptyCollection(packages) && OftenTool.isEmptyCollection(classStrs) && OftenTool
                .isEmptyCollection(classes))
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


    public synchronized void doAutoSetNormal() throws AutoSetException
    {
        try
        {
//            workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
            for (int i = 0; i < iHandles_notporter.size(); i++)
            {
                iHandles_notporter.get(i).handle();
            }
            for (int i = 0; i < iHandles_porter.size(); i++)
            {
                iHandles_porter.get(i).handle();
            }
            workedInstance.clear();
//            workedInstance = null;
        } catch (AutoSetException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new AutoSetException(e);
        } finally
        {
            iHandles_notporter.clear();
            iHandles_porter.clear();
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
            workedInstance.clear();
//            workedInstance = null;
        } catch (AutoSetException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new AutoSetException(e);
        } finally
        {
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
                currentObject,
                RangeType.ALL);
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

    private Object doAutoSetForCurrent(boolean doProxyCurrent, @MayNull Porter porter, @MayNull Object finalObject,
            Class<?> currentObjectClass,
            @MayNull Object currentObject, RangeType rangeType) throws Exception
    {
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

        AutoSetHandleWorkedInstance.Result result = workedInstance.workInstance(currentObject, this, doProxyCurrent);
        currentObject = result.object;
        if (result.isWorked)
        {
            return currentObject;//已经递归扫描过该实例
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
            Class fieldRealType = null;
            f.setAccessible(true);
            Property property = AnnoUtil.getAnnotation(f, Property.class);
            if (property != null)
            {
                fieldRealType = AnnoUtil.Advance.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
                Object value = configData.getValue(currentObject, f, fieldRealType, property);
                if (value != null)
                {
                    f.set(currentObject, value);
                }
                doAutoSetPut(f, value, fieldRealType);
                continue;
            }

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
            Method[] methods = OftenTool.getAllPublicMethods(currentObjectClass);
            for (Method method : methods)
            {
//                dealMethodAutoSetInvoke(currentObject, currentObjectClass, method, configData);
                addSetOkMethod(currentObject, method);
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
            //Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
            //Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
            switch (autoSet.range())
            {
                case Global:
                {
                    value = innerContextBridge.innerBridge.getGlobalSet(keyName);
                    if (value == null && !OftenTool.isInterfaceOrAbstract(mayNew))
                    {

//                    value = OftenTool.newObjectMayNull(mayNew);
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
//                    value = OftenTool.newObjectMayNull(mayNew);
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
//                value = OftenTool.newObjectMayNull(mayNew);
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

//    private void dealMethodAutoSetInvoke(Object currentObject, Class currentClass, Method method,
//            IConfigData iConfigData)
//    {
//        AutoSet.Invoke invokeFun = AnnoUtil.getAnnotation(method, AutoSet.Invoke.class);
//        if (invokeFun == null)
//        {
//            return;
//        }
//        Parameter[] parameters = method.getParameters();
//        Object[] args = new Object[parameters.length];
//        try
//        {
//            for (int i = 0; i < parameters.length; i++)
//            {
//                Parameter parameter = parameters[i];
//                Property property = AnnoUtil.getAnnotation(parameter, Property.class);
//                if (property != null)
//                {
//                    Class realType = AnnoUtil.Advance.getRealTypeOfMethodParameter(currentClass, method, i);
//                    Object value = iConfigData.getValue(currentObject, method, realType, property);
//                    args[i] = value;
//                } else
//                {
//                    throw new InitException(
//                            "expected '@Property' for arg of index " + i + " for method '" + method + "'");
//                }
//            }
//            method.setAccessible(true);
//            method.invoke(currentObject, args);
//        } catch (InitException e)
//        {
//            throw e;
//        } catch (Exception e)
//        {
//            throw new InitException(e);
//        }
//    }


    private void addSetOkMethod(Object currentObject, Method method)
    {
        SetOk setOk = AnnoUtil.getAnnotation(method, SetOk.class);
        if (setOk != null)
        {
            method.setAccessible(true);
            if (currentObject == null && !Modifier.isStatic(method.getModifiers()))
            {
                LOGGER.warn("ignore SetOk method for no instance:method={}", method);
            } else
            {
                setOkObjects.add(new _SetOkObject(currentObject, method, setOk.priority(), LOGGER));
            }
        }
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
                iOtherStartDestroy
                        .addOtherStarts(object, innerContextBridge.annotationDealt.getPortStart(object, objectClass));
            }
            iOtherStartDestroy
                    .addOtherDestroys(object, innerContextBridge.annotationDealt.getPortDestroy(object, objectClass));
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
