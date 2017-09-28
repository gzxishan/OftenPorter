package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetMixin;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.SetOk;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterMain;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.sysset.SyncNotInnerPorter;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.sysset.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.AutoSetSeek;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class AutoSetHandle {
    private final Logger LOGGER;

    private InnerContextBridge innerContextBridge;
    private Delivery thisDelivery;
    private PorterData porterData;
    private List<IHandle> iHandles = new ArrayList<>();
    private String currentContextName;
    private List<_SetOkObject> setOkObjects = new ArrayList<>();

    public static class _SetOkObject implements Comparable<_SetOkObject> {
        public final Object obj;
        public final Method method;
        public final int priority;

        public _SetOkObject(Object obj, Method method, int priority) {
            this.obj = obj;
            this.method = method;
            this.priority = priority;
        }

        @Override
        public int compareTo(_SetOkObject o) {
            return o.priority - priority;
        }

        public void invoke(WObject wObject) throws InvocationTargetException, IllegalAccessException {
            if (method.getParameterTypes().length == 1) {
                method.invoke(obj, wObject);
            } else {
                method.invoke(obj);
            }
        }
    }

    enum RangeType {
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

    private interface IHandle {
        void handle() throws FatalInitException;
    }

    private class Handle_doAutoSetThatOfMixin implements IHandle {
        Object[] args;

        public Handle_doAutoSetThatOfMixin(Object... args) {
            this.args = args;
        }

        private void doAutoSetThatOfMixin(Object obj1, Object obj2) throws FatalInitException {
            try {
                _doAutoSetThatOfMixin(obj1, obj2);
                _doAutoSetThatOfMixin(obj2, obj1);
            } catch (Exception e) {
                throw new FatalInitException(e);
            }
        }

        @Override
        public void handle() throws FatalInitException {
            this.doAutoSetThatOfMixin(args[0], args[1]);
        }
    }

    private class Handle_doAutoSetsForNotPorter implements IHandle {

        private Object[] objects;

        public Handle_doAutoSetsForNotPorter(Object[] objects) {
            this.objects = objects;
        }

        private void doAutoSetsForNotPorter(Object[] objects) throws FatalInitException {
            for (Object obj : objects) {
                doAutoSet(obj, obj, null);
            }
        }

        @Override
        public void handle() throws FatalInitException {
            this.doAutoSetsForNotPorter(objects);
        }
    }

    private class Handle_doAutoSetSeek implements IHandle {

        Object[] args;

        public Handle_doAutoSetSeek(Object... args) {
            this.args = args;
        }

        private void doAutoSetSeek(List<String> packages, ClassLoader classLoader) {
            if (packages == null) {
                return;
            }
            try {
                for (int i = 0; i < packages.size(); i++) {
                    AutoSetHandle.this.doAutoSetSeek(packages.get(i), classLoader);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle() {
            this.doAutoSetSeek((List) args[0], (ClassLoader) args[1]);
        }
    }

    private class Handle_doStaticAutoSet implements IHandle {

        Object[] args;

        public Handle_doStaticAutoSet(Object... args) {
            this.args = args;
        }

        private void doAutoSetSeek(List<String> packages, List<String> classStrs, List<Class<?>> classes, ClassLoader classLoader) {
            if ((packages == null || packages.size() == 0) && (classStrs == null || classStrs.size() == 0) &&
                    (classes == null || classes.size() == 0)) {
                return;
            }
            try {
                LOGGER.debug("*****StaticAutoSet******");
                if (packages != null) {
                    for (int k = 0; k < packages.size(); k++) {
                        String packageStr = packages.get(k);
                        LOGGER.debug("扫描包：{}", packageStr);
                        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
                        for (int i = 0; i < classeses.size(); i++) {
                            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
                            doAutoSet(null, null, clazz, null, null, RangeType.STATIC);
                        }
                    }
                }
                if (classes != null) {
                    for (int k = 0; k < classes.size(); k++) {
                        Class<?> clazz = classes.get(k);
                        doAutoSet(null, null, clazz, null, null, RangeType.STATIC);
                    }
                }

                if (classStrs != null) {
                    for (String clazzStr : classStrs) {
                        Class<?> clazz = null;
                        try {
                            clazz = PackageUtil.newClass(clazzStr, classLoader);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        if (clazz != null) {
                            doAutoSet(null, null, clazz, null, null, RangeType.STATIC);
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle() {
            this.doAutoSetSeek((List) args[0], (List) args[1], (List) args[2], (ClassLoader) args[3]);
        }
    }

    private class Handle_doAutoSetForPorter implements IHandle {
        Object[] args;

        public Handle_doAutoSetForPorter(Object... args) {
            this.args = args;
        }

        private void doAutoSetForPorter(Porter porter, Map<String, Object> autoSetMixinMap) throws FatalInitException {
            doAutoSet(porter, porter.getFinalPorterObject(), porter.getClazz(), porter.getObj(), autoSetMixinMap,
                    RangeType.ALL);
        }

        @Override
        public void handle() throws FatalInitException {
            this.doAutoSetForPorter((Porter) args[0], (Map<String, Object>) args[1]);
        }
    }

    public static AutoSetHandle newInstance(InnerContextBridge innerContextBridge, Delivery thisDelivery,
                                            PorterData porterData, String currentContextName) {
        return new AutoSetHandle(innerContextBridge, thisDelivery, porterData, currentContextName);
    }


    private AutoSetHandle(InnerContextBridge innerContextBridge, Delivery thisDelivery, PorterData porterData,
                          String currentContextName) {
        this.innerContextBridge = innerContextBridge;
        this.thisDelivery = thisDelivery;
        this.porterData = porterData;
        this.currentContextName = currentContextName;
        LOGGER = LogUtil.logger(AutoSetHandle.class);
    }

    public InnerContextBridge getInnerContextBridge() {
        return innerContextBridge;
    }

    public synchronized void addAutoSetSeek(List<String> packages, ClassLoader classLoader) {
        iHandles.add(new Handle_doAutoSetSeek(packages, classLoader));
    }

    public synchronized void addStaticAutoSet(List<String> packages, List<String> classStrs, List<Class<?>> classes,
                                              ClassLoader classLoader) {
        iHandles.add(new Handle_doStaticAutoSet(packages, classStrs, classes, classLoader));
    }

    private void doAutoSetSeek(String packageStr, ClassLoader classLoader) throws Exception {
        LOGGER.debug("*****autoSetSeek******");
        LOGGER.debug("扫描包：{}", packageStr);
        List<String> classeses = PackageUtil.getClassName(packageStr, classLoader);
        for (int i = 0; i < classeses.size(); i++) {
            Class<?> clazz = PackageUtil.newClass(classeses.get(i), classLoader);
            doAutoSet(null, null, clazz, null, null, RangeType.STATIC);
            if (clazz.isAnnotationPresent(AutoSetSeek.class)) {
                Object object = clazz.newInstance();
                doAutoSet(null, object, clazz, object, null, RangeType.INSTANCE);
            }
        }
    }


    public synchronized void addAutoSetsForNotPorter(Object[] objects) {
        iHandles.add(new Handle_doAutoSetsForNotPorter(objects));
    }

    public synchronized void addAutoSetForPorter(Porter porter, Map<String, Object> autoSetMixinMap) {
        iHandles.add(new Handle_doAutoSetForPorter(porter, autoSetMixinMap));
        //doAutoSet(object, autoSetMixinMap);
    }

    public synchronized void addAutoSetThatOfMixin(Object obj1, Object obj2) {
        iHandles.add(new Handle_doAutoSetThatOfMixin(obj1, obj2));
    }


    /**
     * 调用所有的{@linkplain SetOk SetOk}函数。
     */
    public synchronized void invokeSetOk(WObject wObject) {
        _SetOkObject[] setOkObjects = this.setOkObjects.toArray(new _SetOkObject[0]);
        Arrays.sort(setOkObjects);
        try {
            for (_SetOkObject setOkObject : setOkObjects) {
                setOkObject.invoke(wObject);
            }
            this.setOkObjects.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private AutoSetHandleWorkedInstance workedInstance;

    public synchronized void doAutoSet() throws FatalInitException {
        try {
            workedInstance = new AutoSetHandleWorkedInstance();
            for (int i = 0; i < iHandles.size(); i++) {
                iHandles.get(i).handle();
            }
            workedInstance.clear();
            workedInstance = null;
        } catch (FatalInitException e) {
            throw e;
        } catch (Exception e) {
            throw new FatalInitException(e);
        } finally {
            iHandles.clear();
        }
    }

    private void _doAutoSetThatOfMixin(Object obj1, Object objectForSet) throws Exception {
        Field[] fields = obj1.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoSet.AutoSetThatOfMixin.class) && WPTool
                    .isAssignable(objectForSet, field.getType())) {
                field.setAccessible(true);
                field.set(obj1, objectForSet);
                LOGGER.debug("AutoSet.AutoSetThatOfMixin:[{}] with [{}]", field, objectForSet);
            }
        }
    }

    private void doAutoSet(@MayNull Object finalObject, Object currentObject,
                           Map<String, Object> autoSetMixinMap) throws FatalInitException {
        doAutoSet(null, finalObject, currentObject.getClass(), currentObject, autoSetMixinMap, RangeType.ALL);
    }

    private void doAutoSet(Porter porter, @MayNull Object finalObject, Class<?> currentObjectClass,
                           @MayNull Object currentObject,
                           Map<String, Object> autoSetMixinMap, RangeType rangeType) throws FatalInitException {
        if(workedInstance.workInstance(currentObject)){
            return;//已经递归扫描过该实例
        }
        Map<String, Object> contextAutoSet = innerContextBridge.contextAutoSet;
        Map<String, Object> globalAutoSet = innerContextBridge.innerBridge.globalAutoSet;
        Field[] fields = WPTool.getAllFields(currentObjectClass);
        LOGGER.debug("autoSetSeek:class={},instance={},{}",currentObjectClass.getName(),currentObject,currentObject==null?"":currentObject.hashCode());
        RuntimeException thr = null;
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            AutoSet autoSet = f.getAnnotation(AutoSet.class);
            if(autoSet==null){
                continue;
            }

            if (Modifier.isStatic(f.getModifiers())) {
                if (rangeType == RangeType.INSTANCE) {
                    continue;
                }
            } else {
                if (rangeType == RangeType.STATIC) {
                    continue;
                }
            }

            try {
                Class fieldType = porter == null ? f.getType() : porter.getFieldRealClass(f);
                f.setAccessible(true);
//                if (Modifier.isStatic(f.getModifiers()) && f.get(currentObject) != null) {
//                    continue;//静态成员且不为null的，忽略。
//                }
                Object value = f.get(currentObject);
//                if (rangeType == RangeType.STATIC && f.get(currentObject) != null) {
//                    continue;//静态成员且不为null的，忽略。
//                }

                String autoSetMixinName = null;
                boolean needPut = false;
                AutoSetMixin autoSetMixin;
                if (autoSetMixinMap != null && f.isAnnotationPresent(AutoSetMixin.class)) {
                    autoSetMixin = f.getAnnotation(AutoSetMixin.class);
                    autoSetMixinName = "".equals(autoSetMixin.value()) ? (autoSetMixin.classValue()
                            .equals(AutoSetMixin.class) ? fieldType.getName() : autoSetMixin.classValue()
                            .getName()) : autoSetMixin.value();

                    if (autoSetMixin.waitingForSet()) {
                        if (value == null)
                            value = autoSetMixinMap.get(autoSetMixinName);
                    } else {
                        needPut = true;
                    }
                }
                if (autoSet == null) {
                    if (value == null || autoSetMixinMap == null) {
                        continue;
                    } else if (needPut) {
                        autoSetMixinMap.put(autoSetMixinName, f.get(currentObject));
                        continue;
                    }
                } else if (isDefaultAutoSetObject(f, porter, currentObjectClass, currentObject, autoSet,
                        autoSetMixinMap)) {
                    continue;
                }
                if (value == null) {
                    String keyName;
                    Class<?> mayNew = null;
                    Class<?> classClass = autoSet.classValue();
                    if (classClass.equals(AutoSet.class)) {
                        keyName = autoSet.value();
                    } else {
                        keyName = classClass.getName();
                        mayNew = classClass;
                    }
                    if ("".equals(keyName)) {
                        keyName = fieldType.getName();
                    }
                    if (mayNew == null) {
                        mayNew = fieldType;
                    }


                    switch (autoSet.range()) {
                        case Global: {
                            value = globalAutoSet.get(keyName);
                            if (value == null && !WPTool.isInterfaceOrAbstract(mayNew)) {
                                value = WPTool.newObject(mayNew);
                                globalAutoSet.put(keyName, value);
                            }
                        }
                        break;
                        case Context: {
                            value = contextAutoSet.get(keyName);
                            if (value == null && !WPTool.isInterfaceOrAbstract(mayNew)) {
                                value = WPTool.newObject(mayNew);
                                contextAutoSet.put(keyName, value);
                            }
                        }
                        break;
                        case New: {
                            value = WPTool.newObject(mayNew);
                        }
                        break;
                    }

                    if (value == null) {
                        value = genObjectOfAutoSet(autoSet, currentObjectClass, currentObject, f);
                    }
                    value = dealtAutoSet(autoSet, finalObject, currentObjectClass, currentObject, f, value);
                    if (value == null) {
                        if (!autoSet.nullAble()) {
                            thr = new RuntimeException(String.format("AutoSet:could not set [%s] with null!", f));
                            break;
                        }
                    } else if (needPut) {
                        autoSetMixinMap.put(autoSetMixinName, value);
                    }
                }
                if (value != null) {
                    doAutoSet(value, value, autoSetMixinMap);//递归：设置被设置的变量。
                }
                f.set(currentObject, value);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("AutoSet:({})[{}] with [{}]", autoSetMixinName == null ? "" : AutoSetMixin.class
                            .getSimpleName() + " " + (needPut ? "get" : "set") + " " + autoSetMixinName, f, value);
                }
            } catch (FatalInitException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.warn("AutoSet failed for [{}]({}),ex={}", f, autoSet.range(), e.getMessage());
            }

        }
        if (thr != null) {
            throw thr;
        } else {
            Method[] methods = WPTool.getAllPublicMethods(currentObjectClass);

            for (Method method : methods) {
                SetOk setOk = method.getAnnotation(SetOk.class);
                if (setOk != null) {
                    method.setAccessible(true);
                    setOkObjects.add(new _SetOkObject(currentObject, method, setOk.priority()));
                }
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
    private Object genObjectOfAutoSet(AutoSet autoSet, Class<?> currentObjectClass, Object currentObject,
                                      Field field) throws InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, FatalInitException {
        Class<? extends AutoSetGen> genClass = autoSet.gen();
        String option = autoSet.option();
        if (genClass.equals(AutoSetGen.class)) {
            Class<?> objClazz = field.getType();
            if (objClazz.isAnnotationPresent(AutoSet.AutoSetDefaultDealt.class)) {
                AutoSet.AutoSetDefaultDealt autoSetDefaultDealt = objClazz
                        .getAnnotation(AutoSet.AutoSetDefaultDealt.class);
                genClass = autoSetDefaultDealt.gen();
                if ("".equals(option)) {
                    option = autoSetDefaultDealt.option();
                }
            }
        }
        if (genClass.equals(AutoSetGen.class)) {
            return null;
        }

        AutoSetGen autoSetGen = WPTool.newObject(genClass);
        doAutoSet(autoSetGen, autoSetGen, null);
        Object value = autoSetGen.genObject(currentObjectClass, currentObject, field, option);
        return value;
    }

    private Object dealtAutoSet(AutoSet autoSet, @MayNull Object finalObject, Class<?> currentObjectClass,
                                Object currentObject,
                                Field field,
                                Object value) throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, FatalInitException {
        Class<? extends AutoSetDealt> autoSetDealtClass = autoSet.dealt();
        String option = autoSet.option();
        if (autoSetDealtClass.equals(AutoSetDealt.class)) {
            Class<?> objClazz = field.getType();
            if (objClazz.isAnnotationPresent(AutoSet.AutoSetDefaultDealt.class)) {
                AutoSet.AutoSetDefaultDealt autoSetDefaultDealt = objClazz
                        .getAnnotation(AutoSet.AutoSetDefaultDealt.class);
                autoSetDealtClass = autoSetDefaultDealt.dealt();
                if ("".equals(option)) {
                    option = autoSetDefaultDealt.option();
                }
            }
        }
        if (autoSetDealtClass.equals(AutoSetDealt.class)) {
            return value;
        }
        AutoSetDealt autoSetDealt = WPTool.newObject(autoSetDealtClass);
        doAutoSet(autoSetDealt, autoSetDealt, null);
        Object finalValue = autoSetDealt.deal(finalObject, currentObjectClass, currentObject, field, value, option);
        return finalValue;
    }

    /**
     * 是否是默认工具类。
     */
    private boolean isDefaultAutoSetObject(Field f, Porter porter, Class<?> currentObjectClass,
                                           @MayNull Object currentObject,
                                           AutoSet autoSet, Map<String, Object> autoSetMixinMap) throws IllegalAccessException, FatalInitException,
            NoSuchMethodException, InstantiationException, InvocationTargetException

    {
        Object sysset = null;
        String typeName = f.getType().getName();
        if (typeName.equals(TypeTo.class.getName())) {
            sysset = new TypeTo(innerContextBridge);
        } else if (typeName.equals(PorterData.class.getName())) {
            sysset = porterData;
        } else if (typeName.equals(Logger.class.getName())) {
            sysset = LogUtil.logger(currentObjectClass);
        } else if (typeName.equals(SyncPorter.class.getName()) || typeName.equals(SyncNotInnerPorter.class.getName())) {

            boolean isInner = typeName.equals(SyncPorter.class.getName());

            PorterParamGetterImpl porterParamGetter = new PorterParamGetterImpl();
            porterParamGetter.setContext(currentContextName);

            if (porter == null) {
                LOGGER.debug("auto set {} in not porter[{}]", (isInner ? SyncPorter.class : SyncNotInnerPorter.class).getSimpleName(), currentObjectClass);
                //throw new FatalInitException(SyncPorter.class.getSimpleName() + "just allowed in porter!");
            } else {
                porterParamGetter.setClassTied(PortUtil.tied(porter.getFinalPorterObject().getClass()));
            }
            _SyncPorterOption syncPorterOption = innerContextBridge.annotationDealt
                    .syncPorterOption(f, porterParamGetter);
            porterParamGetter.check();
            sysset = new SyncPorterImpl(syncPorterOption, isInner);
        } else if (typeName.equals(Delivery.class.getName())) {
            String pName = autoSet.value();
            Delivery delivery;
            if (WPTool.isEmpty(pName)) {
                delivery = thisDelivery;
            } else {
                CommonMain commonMain = PorterMain.getMain(pName);
                if (commonMain == null) {
                    throw new Error(
                            String.format("%s object is null for %s[%s]!", AutoSet.class.getSimpleName(), f, pName));
                }
                delivery = commonMain.getPLinker();
            }

            sysset = delivery;
        }
        //sysset = dealtAutoSet(autoSet, object, f, sysset);
        if (sysset != null) {
            f.set(currentObject, sysset);
            LOGGER.debug("AutoSet [{}] with default object [{}]", f, sysset);
            doAutoSet(sysset, sysset, autoSetMixinMap);//递归：设置被设置的变量。
        }
        return sysset != null;
    }
}
