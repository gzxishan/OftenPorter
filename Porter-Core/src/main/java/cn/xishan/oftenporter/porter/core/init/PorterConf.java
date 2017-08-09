package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.*;

/**
 * 框架配置对象。非线程安全。
 */
public class PorterConf {
    private SeekPackages seekPackages;
    private InitParamSource userInitParam;
    private Set<StateListener> stateListenerSet;

    private List<CheckPassable> contextChecks;
    private List<CheckPassable> forAllCheckPassableList;

    private Map<String, Object> contextAutoSetMap;
    private Map<String, Class<?>> contextAutoGenImplMap;
    private ClassLoader classLoader;
    private boolean responseWhenException = true;
    private boolean enablePortInTiedNameDefault = true;
    private boolean isInited;
    private String name;
    private String contentEncoding = "utf-8";
    private List<String> autoSetSeekPackages = new ArrayList<>(),
            staticAutoSetPackages = new ArrayList<>(), staticAutoSetClassStrs = new ArrayList<>();
    private List<Class<?>> staticAutoSetClasses = new ArrayList<>();
    private ParamSourceHandleManager paramSourceHandleManager;

    private DefaultReturnFactory defaultReturnFactory;
    private OutType defaultPortOutType;


    PorterConf() {
        seekPackages = new SeekPackages();
        stateListenerSet = new HashSet<>();
        contextChecks = new ArrayList<>();
        forAllCheckPassableList = new ArrayList<>();
        userInitParam = new InitParamSourceImpl();
        contextAutoSetMap = new HashMap<>();
        contextAutoGenImplMap = new HashMap<>();
        paramSourceHandleManager = new ParamSourceHandleManager();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public void setDefaultPortOutType(OutType defaultPortOutType) {
        this.defaultPortOutType = defaultPortOutType;
    }

    public OutType getDefaultPortOutType() {
        return defaultPortOutType;
    }

    public void setDefaultReturnFactory(DefaultReturnFactory defaultReturnFactory) {
        this.defaultReturnFactory = defaultReturnFactory;
    }

    public DefaultReturnFactory getDefaultReturnFactory() {
        return defaultReturnFactory;
    }

    /**
     * 用于扫描包中含有{@linkplain AutoSet.AutoSetSeek}注解的类，进而注入里面的变量。
     *
     * @param autoSetSeekPackages
     */
    public void addAutoSetSeekPackages(String... autoSetSeekPackages) {
        WPTool.addAll(this.autoSetSeekPackages, autoSetSeekPackages);
    }

    /**
     * 用于扫描指定包中的每一个类，进而注入含有{@linkplain AutoSet AutoSet}的静态变量。
     * <pre>
     *     会忽略非null的对象。
     * </pre>
     *
     * @param staticAutoSetPackages
     */
    public void addStaticAutoSetPackages(String... staticAutoSetPackages) {
        WPTool.addAll(this.staticAutoSetPackages, staticAutoSetPackages);
    }

    public List<String> getStaticAutoSetPackages() {
        return staticAutoSetPackages;
    }

    /**
     * 注入含有{@linkplain AutoSet AutoSet}的静态变量。
     * <pre>
     *     会忽略非null的对象。
     * </pre>
     *
     * @param staticAutoSetClasses
     */
    public void addStaticAutoSetClasses(Class<?>... staticAutoSetClasses) {
        WPTool.addAll(this.staticAutoSetClasses, staticAutoSetClasses);
    }

    public List<Class<?>> getStaticAutoSetClasses() {
        return staticAutoSetClasses;
    }

    /**
     * 注入含有{@linkplain AutoSet AutoSet}的静态变量。
     * <pre>
     *     会忽略非null的对象。
     * </pre>
     *
     * @param staticAutoSetClasses
     */
    public void addStaticAutoSetClasses(String... staticAutoSetClasses) {
        WPTool.addAll(this.staticAutoSetClassStrs, staticAutoSetClasses);
    }

    public List<String> getStaticAutoSetClassStrs() {
        return staticAutoSetClassStrs;
    }

    public List<String> getAutoSetSeekPackages() {
        return autoSetSeekPackages;
    }

    public List<CheckPassable> getForAllCheckPassableList() {
        return forAllCheckPassableList;
    }

    /**
     * 除了{@linkplain DuringType#ON_GLOBAL DuringType.ON_GLOBAL
     * }和{@linkplain DuringType#ON_CONTEXT DuringType.ON_CONTEXT}的所有时期都会调用它,并且该检测最先被调用。
     *
     * @param forAllCheckPassable
     */
    public void addForAllCheckPassable(CheckPassable forAllCheckPassable) {
        forAllCheckPassableList.add(forAllCheckPassable);
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        checkInited();
        this.contentEncoding = contentEncoding;
    }

    public void setContextName(String contextName) {
        checkInited();
        PortUtil.checkName(contextName);
        this.name = contextName;
    }

    public String getContextName() {
        return name;
    }


    private void checkInited() {
        if (isInited) {
            throw new RuntimeException("already init!");
        }
    }

    /**
     * 见{@linkplain #isEnableTiedNameDefault()}
     *
     * @param enablePortInTiedNameDefault
     */
    public void setEnableTiedNameDefault(boolean enablePortInTiedNameDefault) {
        checkInited();
        this.enablePortInTiedNameDefault = enablePortInTiedNameDefault;
    }

    /**
     * 是否允许{@linkplain PortIn#value()}、{@linkplain PortInObj.Nece#value()}和{@linkplain PortInObj.UnNece#value()}取默认值。默认为true。
     *
     * @return
     */
    public boolean isEnableTiedNameDefault() {
        checkInited();
        return enablePortInTiedNameDefault;
    }

    /**
     * 用于对象自动设置。另见{@linkplain AutoSet.Range#Context}
     *
     * @param name
     * @param object
     */
    public void addContextAutoSet(String name, Object object) {
        contextAutoSetMap.put(name, object);
    }

    /**
     * 效果同{@linkplain #addContextAutoSet(String, Object) addContextAutoSet(object.getClass().getName(), Object)}
     *
     * @param object
     */
    public void addContextAutoSet(Object object) {
        addContextAutoSet(object.getClass().getName(), object);
    }

    /**
     * 效果同{@linkplain #addContextAutoSet(String, Object) addContextAutoSet(clazz.getName(), Object)}
     *
     * @param object
     */
    public void addContextAutoSet(Class<?> clazz, Object object) {
        addContextAutoSet(clazz.getName(), object);
    }

    /**
     * 用于添加接口实现.
     *
     * @param name      名称
     * @param implClass 实现类。
     */
    public void addContextAutoGenImpl(String name, Class<?> implClass) {
        contextAutoGenImplMap.put(name, implClass);
    }

    /**
     * 效果同{@linkplain #addContextAutoGenImpl(String, Class) addContextAutoGenImpl(clazz.getName(), Class)}
     *
     * @param clazz
     * @param implClass
     */
    public void addContextAutoGenImpl(Class<?> clazz, Class<?> implClass) {
        contextAutoGenImplMap.put(clazz.getName(), implClass);
    }

    public Map<String, Class<?>> getContextAutoGenImplMap() {
        return contextAutoGenImplMap;
    }

    public boolean isResponseWhenException() {
        return responseWhenException;
    }


    public SeekPackages getSeekPackages() {
        return seekPackages;
    }

    public void addStateListener(StateListener stateListener) {
        checkInited();
        stateListenerSet.add(stateListener);
    }

    /**
     * 添加针对{@linkplain DuringType#ON_CONTEXT}有效的全局检测对象。
     *
     * @param checkPassable
     */
    public void addContextCheck(CheckPassable checkPassable) {
        contextChecks.add(checkPassable);
    }

    public void setClassLoader(ClassLoader classLoader) {
        checkInited();
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<CheckPassable> getContextChecks() {
        return contextChecks;
    }

    public Map<String, Object> getContextAutoSetMap() {
        return contextAutoSetMap;
    }

    public Set<StateListener> getStateListenerSet() {
        checkInited();
        return stateListenerSet;
    }

    public InitParamSource getUserInitParam() {
        checkInited();
        return userInitParam;
    }

    public ParamSourceHandleManager getParamSourceHandleManager() {
        return paramSourceHandleManager;
    }

    void initOk() {
        isInited = true;
        seekPackages = null;
        userInitParam = null;
        classLoader = null;
        contextChecks = null;
        contextAutoSetMap = null;
        contextAutoGenImplMap = null;
        stateListenerSet = null;
        forAllCheckPassableList = null;
        autoSetSeekPackages = null;
        paramSourceHandleManager = null;
        defaultReturnFactory = null;
    }
}
