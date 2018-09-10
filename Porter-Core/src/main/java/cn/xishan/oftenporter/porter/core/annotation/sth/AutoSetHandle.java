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
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.IOtherStartDestroy;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.sysset.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetHandle
{
    private final Logger LOGGER;

    private InnerContextBridge innerContextBridge;
    private IArgumentsFactory argumentsFactory;
    private Delivery thisDelivery;
    private PorterData porterData;
    private List<IHandle> iHandles_porter = new ArrayList<>();
    private List<IHandle> iHandles_notporter = new ArrayList<>();
    private List<IHandle> iHandlesForAutoSetThat = new ArrayList<>();
    private Map<Class, Porter> porterMap = new HashMap<>();
    private String currentContextName;
    private List<_SetOkObject> setOkObjects = new ArrayList<>();
    private IOtherStartDestroy iOtherStartDestroy;
    private Map<Object, Object> proxyObjectMap = new HashMap<>();

    public <T> T getContextObject(Class<?> key)
    {
        return getContextObject(key.getName());
    }

    public <T> T getContextObject(String key)
    {
        return (T) innerContextBridge.contextAutoSet.get(key);
    }

    public static class _SetOkObject implements Comparable<_SetOkObject>
    {
        public final Object obj;
        public final Method method;
        public final int priority;

        public _SetOkObject(Object obj, Method method, int priority)
        {
            this.obj = obj;
            this.method = method;
            this.priority = priority;
        }

        @Override
        public int compareTo(_SetOkObject o)
        {
            return o.priority - priority;
        }

        public void invoke(WObject wObject) throws InvocationTargetException, IllegalAccessException
        {
            if (method.getParameterTypes().length == 1)
            {
                method.invoke(obj, wObject);
            } else
            {
                method.invoke(obj);
            }
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

        private void doAutoSetThatOfMixin(Object obj1, Object obj2) throws FatalInitException
        {
            try
            {
                _doAutoSetThatOfMixin(obj1, obj2);
                _doAutoSetThatOfMixin(obj2, obj1);
            } catch (Exception e)
            {
                throw new FatalInitException(e);
            }
        }

        @Override
        public void handle() throws FatalInitException
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

    public static AutoSetHandle newInstance(IArgumentsFactory argumentsFactory, InnerContextBridge innerContextBridge,
            Delivery thisDelivery,
            PorterData porterData, String currentContextName)
    {
        return new AutoSetHandle(argumentsFactory, innerContextBridge, thisDelivery, porterData, currentContextName);
    }


    public void setIOtherStartDestroy(IOtherStartDestroy iOtherStartDestroy)
    {
        this.iOtherStartDestroy = iOtherStartDestroy;
    }

    private AutoSetHandle(IArgumentsFactory argumentsFactory, InnerContextBridge innerContextBridge,
            Delivery thisDelivery, PorterData porterData,
            String currentContextName)
    {
        this.argumentsFactory = argumentsFactory;
        this.innerContextBridge = innerContextBridge;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        this.currentContextName = currentContextName;
        LOGGER = LogUtil.logger(AutoSetHandle.class);
    }

    public InnerContextBridge getInnerContextBridge()
    {
        return innerContextBridge;
    }

    public IArgumentsFactory getArgumentsFactory()
    {
        return argumentsFactory;
    }

    public String getContextName()
    {
        return currentContextName;
    }

    public PName getPName()
    {
        return thisDelivery.currentPName();
    }

    public synchronized void addAutoSetSeekPackages(List<String> packages, ClassLoader classLoader)
    {
        iHandles_notporter.add(new Handle_doAutoSetSeek(packages, classLoader));
    }

    public synchronized void addStaticAutoSet(List<String> packages, List<String> classStrs, List<Class<?>> classes,
            ClassLoader classLoader)
    {
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


    public synchronized void addAutoSetsForNotPorter(Object... objects)
    {
        iHandles_notporter.add(new Handle_doAutoSetsForNotPorter(objects));
    }

    public synchronized void addAutoSetForPorter(Porter porter)
    {
        iHandles_porter.add(new Handle_doAutoSetForPorter(porter));
        porterMap.put(porter.getPortIn().getToPorterKey(), porter);
        porterMap.putAll(porter.getMixinToThatCouldSet());

        List<PortIn.ContextSet> contextSets = AnnoUtil
                .getAnnotationsWithSuper(porter.getClazz(), PortIn.ContextSet.class);
        for (PortIn.ContextSet contextSet : contextSets)
        {
            if (WPTool.isEmpty(contextSet.value()))
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("@{} value of {} is empty!", PortIn.ContextSet.class.getSimpleName(),
                            porter.getClazz());
                }
                continue;
            }
            Object last = innerContextBridge.contextAutoSet.put(contextSet.value(), porter.getObj());
            if (last != null && LOGGER.isWarnEnabled())
            {
                LOGGER.warn("override by @{}:key={},newValue={},oldValue={}", PortIn.ContextSet.class.getSimpleName(),
                        contextSet.value(),
                        porter.getObj(), last);
            }
        }

        //doAutoSet(object, autoSetMixinMap);
    }

    public synchronized void addAutoSetThatOfMixin(Object porter1, Object porter2)
    {
        iHandlesForAutoSetThat.add(new Handle_doAutoSetThatOfMixin(porter1, porter2));
    }

    /**
     * 调用所有的{@linkplain SetOk SetOk}函数。
     */
    public synchronized void invokeSetOk(WObject wObject)
    {
        _SetOkObject[] setOkObjects = this.setOkObjects.toArray(new _SetOkObject[0]);
        Arrays.sort(setOkObjects);
        try
        {
            for (_SetOkObject setOkObject : setOkObjects)
            {
                setOkObject.invoke(wObject);
            }
            this.setOkObjects.clear();
            this.porterMap = null;
            this.proxyObjectMap.clear();
            this.proxyObjectMap = null;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private AutoSetHandleWorkedInstance workedInstance;

    public synchronized void doAutoSetNormal(
            AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal) throws FatalInitException
    {
        try
        {
            workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
            for (int i = 0; i < iHandles_notporter.size(); i++)
            {
                iHandles_notporter.get(i).handle();
            }
            for (int i = 0; i < iHandles_porter.size(); i++)
            {
                iHandles_porter.get(i).handle();
            }
            workedInstance.clear();
            workedInstance = null;
        } catch (FatalInitException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new FatalInitException(e);
        } finally
        {
            iHandles_notporter.clear();
            iHandles_porter.clear();
        }
    }

    public synchronized void doAutoSetThat(
            AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal) throws FatalInitException
    {
        try
        {
            workedInstance = new AutoSetHandleWorkedInstance(autoSetObjForAspectOfNormal);
            for (int i = 0; i < iHandlesForAutoSetThat.size(); i++)
            {
                iHandlesForAutoSetThat.get(i).handle();
            }
            workedInstance.clear();
            workedInstance = null;
        } catch (FatalInitException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new FatalInitException(e);
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

        Field[] fieldsGet = WPTool.getAllFields(PortUtil.getRealClass(objectForGet));
        for (Field field : fieldsGet)
        {
            AutoSetToThatForMixin autoSetToThatForMixin = AnnoUtil.Advanced
                    .getAnnotation(field, AutoSetToThatForMixin.class);
            if (autoSetToThatForMixin == null)
            {
                continue;
            }
            if (autoSetToThatForMixin.key().equals("") && autoSetToThatForMixin.value()
                    .equals(AutoSet.class))
            {
                throw new InitException("the key of annotation " + AutoSetToThatForMixin.class
                        .getSimpleName() + " is empty  for field '" + field + "'");
            }
            String key = autoSetToThatForMixin.key().equals("") ? autoSetToThatForMixin.value()
                    .getName() : autoSetToThatForMixin.key();
            if (fromGet.containsKey(key))
            {
                LOGGER.warn("already exists key '{}', current field is [{}],last field is [{}]", field,
                        fromGet.get(key));
            }
            fromGet.put(key, field);
        }

        Field[] fields = WPTool.getAllFields(PortUtil.getRealClass(objectForSet));
        for (Field field : fields)
        {
            AutoSetThatForMixin autoSetThatForMixin = AnnoUtil.Advanced.getAnnotation(field, AutoSetThatForMixin.class);
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
            if (!WPTool.isAssignable(value.getClass(), field.getType()))
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
        proxyObjectMap.put(origin, proxy);
    }

    private void doAutoSetPut(Field field, Object obj, Class realType)
    {
        if (obj != null)
        {
            AutoSet.Put put = AnnoUtil.getAnnotation(field, AutoSet.Put.class);
            if (put != null)
            {
                Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
                Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
                String name = put.name().equals("") ? realType.getName() : put.name();
                if (put.range() == AutoSet.Range.Global)
                {
                    globalAutoSet.put(name, obj);
                } else
                {
                    contextAutoSet.put(name, obj);
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

        AutoSetHandleWorkedInstance.Result result = workedInstance.workInstance(currentObject, this, doProxyCurrent);
        currentObject = result.object;
        if (result.isWorked)
        {
            return currentObject;//已经递归扫描过该实例
        }

        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
        Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
        IConfigData configData = getContextObject(IConfigData.class);

        Field[] fields = WPTool.getAllFields(currentObjectClass);
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
                fieldRealType = AnnoUtil.Advanced.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
                Object value = configData.getValue(currentObject, f, fieldRealType, property);
                if (value != null)
                {
                    f.set(currentObject, value);
                }
                doAutoSetPut(f, value, fieldRealType);
                continue;
            }
            _AutoSet autoSet = annotationDealt.autoSet(f);
            if (autoSet == null)
            {
                Object value = f.get(currentObject);
                if (value != null && f.isAnnotationPresent(AutoSetSeek.class))
                {
                    Object newValue = workedInstance.doProxyAndDoAutoSet(value, this, true);
                    if (newValue != value)
                    {
                        f.set(currentObject, newValue);
                        value = newValue;
                    }
                }
                if (fieldRealType == null)
                {
                    fieldRealType = AnnoUtil.Advanced.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
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

            try
            {
                if (isDefaultAutoSetObject(f, porter, finalObject, currentObjectClass, currentObject, autoSet))
                {
                    continue;
                }
                Object value = f.get(currentObject);
                if (fieldRealType == null)
                {
                    fieldRealType = AnnoUtil.Advanced.getRealTypeOfField(currentObjectClass, f);//支持泛型变量获取到正确的类型
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

                boolean isValueNotNull = value != null;
                if (isValueNotNull)
                {
                    LOGGER.debug("ignore field [{}] for it's not null:{}", f, value);
                    //！！！忽略了非null的成员
                    value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                } else
                {
                    switch (autoSet.range())
                    {
                        case Global:
                        {
                            value = globalAutoSet.get(keyName);
                            if (value == null && !WPTool.isInterfaceOrAbstract(mayNew))
                            {
                                value = WPTool.newObjectMayNull(mayNew);
                                if (value == null)
                                {
                                    LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                                }
                            }
                            if (value != null)
                            {
                                value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                                globalAutoSet.put(keyName, value);
                            }
                        }
                        break;
                        case Context:
                        {
                            value = contextAutoSet.get(keyName);
                            if (value == null)
                            {
                                Porter thePorter = porterMap.get(mayNew);
                                if (thePorter != null)
                                {
                                    value = thePorter.getObj();
                                }
                            }
                            if (value == null && !WPTool.isInterfaceOrAbstract(mayNew))
                            {
                                value = WPTool.newObjectMayNull(mayNew);
                                if (value == null)
                                {
                                    LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                                }
                            }
                            if (value != null)
                            {
                                value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                                contextAutoSet.put(keyName, value);
                            }
                        }
                        break;
                        case New:
                        {
                            value = WPTool.newObjectMayNull(mayNew);
                            value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                            if (value == null)
                            {
                                LOGGER.debug("there is no zero-args constructor:{}", mayNew);
                            }
                        }
                        break;
                    }

                    if (value == null)
                    {
                        value = genObjectOfAutoSet(autoSet, currentObjectClass, currentObject, f);
                        value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                    }

                    if (value == null)
                    {
                        if (!autoSet.nullAble())
                        {
                            thr = new RuntimeException(String.format("AutoSet:could not set [%s] with null!", f));
                            break;
                        }
                    }
                }

                Object dealValue = dealtAutoSet(autoSet, finalObject, currentObjectClass, currentObject, f, value);
                if (value != dealValue)
                {
                    value = workedInstance.doProxyAndDoAutoSet(dealValue, this, autoSet.willRecursive());
                }
                if (value == null && !autoSet.nullAble())
                {
                    thr = new RuntimeException(String.format("AutoSet:could not set [%s] with null!", f));
                    break;
                }
                if (isValueNotNull && autoSet.notNullPut())
                {
                    switch (autoSet.range())
                    {
                        case Global:
                            globalAutoSet.put(keyName, value);
                            break;
                        case Context:
                            contextAutoSet.put(keyName, value);
                            break;
                        case New:
                            break;
                    }
                }
                //value = workedInstance.mayProxy(value, this, doProxy);
                boolean willSet = true;
                IAutoSetListener.Will lastWill = new IAutoSetListener.Will(willSet);
                lastWill.optionValue = value;
                Object originValue = value;
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
                if (willSet)
                {
                    if (value != originValue)
                    {
                        value = workedInstance.doProxyAndDoAutoSet(value, this, autoSet.willRecursive());
                    }
                    f.set(currentObject, value);//设置变量
                    doAutoSetPut(f, value, fieldRealType);
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

            } catch (FatalInitException e)
            {
                throw e;
            } catch (InitException e)
            {
                throw e;
            } catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
                LOGGER.warn("AutoSet failed for [{}]({}),ex={}", f, autoSet.range(), e.getMessage());
            }

        }
        if (thr != null)
        {
            throw thr;
        } else
        {
            Method[] methods = WPTool.getAllMethods(currentObjectClass);
            for (Method method : methods)
            {
                dealMethodAutoSet(currentObject, currentObjectClass, method, configData);
                SetOk setOk = AnnoUtil.Advanced.getAnnotation(method, SetOk.class);
                if (setOk != null)
                {
                    method.setAccessible(true);
                    setOkObjects.add(new _SetOkObject(currentObject, method, setOk.priority()));
                }
            }

            if (porter == null)
            {
                addOtherStartDestroy(currentObject,currentObjectClass);
            }
        }
        return currentObject;
    }

    private void dealMethodAutoSet(Object currentObject, Class currentClass, Method method, IConfigData iConfigData)
    {
        AutoSet autoSet = AnnoUtil.Advanced.getAnnotation(method, AutoSet.class);
        if (autoSet == null)
        {
            return;
        }
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        try
        {
            for (int i = 0; i < parameters.length; i++)
            {
                Parameter parameter = parameters[i];
                Property property = AnnoUtil.getAnnotation(parameter, Property.class);
                if (property != null)
                {
                    Class realType = AnnoUtil.Advanced.getRealTypeOfMethodParameter(currentClass, method, i);
                    Object value = iConfigData.getValue(currentObject, method, realType, property);
                    args[i] = value;
                } else
                {
                    throw new InitException(
                            "expected '@Property' for arg of index " + i + " for method '" + method + "'");
                }
            }
            method.setAccessible(true);
            method.invoke(currentObject, args);
        } catch (InitException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new InitException(e);
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
    private Object genObjectOfAutoSet(_AutoSet autoSet, Class<?> currentObjectClass, Object currentObject,
            Field field) throws Exception
    {
        Class<? extends AutoSetGen> genClass = autoSet.gen();
        String option = autoSet.option();
        if (genClass.equals(AutoSetGen.class))
        {
            AutoSetDefaultDealt autoSetDefaultDealt = AnnoUtil.Advanced
                    .getAutoSetDefaultDealt(field, currentObjectClass);
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

        AutoSetGen autoSetGen = WPTool.newObject(genClass);
        addOtherStartDestroy(autoSetGen,genClass);
        autoSetGen = (AutoSetGen) doAutoSetForCurrent(true, autoSetGen, autoSetGen);
        Object value = autoSetGen.genObject(currentObjectClass, currentObject, field,
                AnnoUtil.Advanced.getRealTypeOfField(currentObjectClass, field), autoSet, option);
        return value;
    }

    private void addOtherStartDestroy(@MayNull Object object,Class objectClass)
    {
        if (iOtherStartDestroy != null)
        {
            iOtherStartDestroy.addOtherStarts(object, innerContextBridge.annotationDealt.getPortStart(object,objectClass));
            iOtherStartDestroy.addOtherDestroys(object, innerContextBridge.annotationDealt.getPortDestroy(object,objectClass));
        }
    }

    private Object dealtAutoSet(_AutoSet autoSet, @MayNull Object finalObject, Class<?> currentObjectClass,
            Object currentObject,
            Field field,
            Object value) throws Exception
    {
        Class<? extends AutoSetDealt> autoSetDealtClass = autoSet.dealt();
        String option = autoSet.option();
        if (autoSetDealtClass.equals(AutoSetDealt.class))
        {
            AutoSetDefaultDealt autoSetDefaultDealt = AnnoUtil.Advanced
                    .getAutoSetDefaultDealt(field, currentObjectClass);
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
        AutoSetDealt autoSetDealt = WPTool.newObject(autoSetDealtClass);
        addOtherStartDestroy(autoSetDealt,autoSetDealtClass);
        autoSetDealt = (AutoSetDealt) doAutoSetForCurrent(true, autoSetDealt, autoSetDealt);
        Object finalValue = autoSetDealt.deal(finalObject, currentObjectClass, currentObject, field,
                AnnoUtil.Advanced.getRealTypeOfField(currentObjectClass, field), value, autoSet, option);
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
        if (typeName.equals(TypeTo.class.getName()))
        {
            sysset = new TypeTo(innerContextBridge);
        } else if (typeName.equals(PorterData.class.getName()))
        {
            sysset = porterData;
        } else if (typeName.equals(Logger.class.getName()))
        {
            sysset = LogUtil.logger(currentObjectClass);
        } else if (typeName.equals(SyncPorter.class.getName()) || typeName.equals(SyncNotInnerPorter.class.getName()) ||
                typeName.equals(SyncPorterThrows.class.getName()))
        {

            boolean isInner = !typeName.equals(SyncNotInnerPorter.class.getName());

            PorterParamGetterImpl porterParamGetter = new PorterParamGetterImpl();
            porterParamGetter.setContext(currentContextName);

            if (porter == null)
            {
                LOGGER.debug("auto set {} in not porter[{}]",
                        (isInner ? SyncPorter.class : SyncNotInnerPorter.class).getSimpleName(), currentObjectClass);
                //throw new FatalInitException(SyncPorter.class.getSimpleName() + "just allowed in porter!");
                if (!f.isAnnotationPresent(SyncPorterOption.class) && finalObject != null)
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
            if (typeName.equals(SyncPorterThrows.class.getName()))
            {
                sysset = new SyncPorterThrowsImpl((SyncPorterImpl) sysset);
            }
        } else if (typeName.equals(Delivery.class.getName()))
        {
            String pName = autoSet.value();
            Delivery delivery;
            if (WPTool.isEmpty(pName))
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
                delivery = commonMain.getPLinker();
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
