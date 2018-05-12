package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetSeek;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.annotation.PortOut;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.*;

/**
 * 框架配置对象。非线程安全。
 */
public class PorterConf
{
    private SeekPackages seekPackages;
    private InitParamSource userInitParam;
    private List<StateListener> stateListenerList;

    private List<CheckPassable> contextChecks;
    private List<CheckPassable> porterCheckPassableList;

    private Map<String, Object> contextAutoSetMap;
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
    private IArgumentsFactory iArgumentsFactory;


    PorterConf()
    {
        seekPackages = new SeekPackages();
        stateListenerList = new ArrayList<>();
        contextChecks = new ArrayList<>();
        porterCheckPassableList = new ArrayList<>();
        userInitParam = new InitParamSourceImpl();
        contextAutoSetMap = new HashMap<>();
        paramSourceHandleManager = new ParamSourceHandleManager();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 设置没有加{@linkplain PortOut#}
     *
     * @param defaultPortOutType
     */
    public void setDefaultPortOutType(OutType defaultPortOutType)
    {
        this.defaultPortOutType = defaultPortOutType;
    }

    public OutType getDefaultPortOutType()
    {
        return defaultPortOutType;
    }

    public void setDefaultReturnFactory(DefaultReturnFactory defaultReturnFactory)
    {
        this.defaultReturnFactory = defaultReturnFactory;
    }

    public DefaultReturnFactory getDefaultReturnFactory()
    {
        return defaultReturnFactory;
    }

    /**
     * 用于扫描包中含有{@linkplain AutoSetSeek}注解的类，进而注入里面的变量。
     *
     * @param autoSetSeekPackages
     */
    public void addAutoSetSeekPackages(String... autoSetSeekPackages)
    {
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
    public void addStaticAutoSetPackages(String... staticAutoSetPackages)
    {
        WPTool.addAll(this.staticAutoSetPackages, staticAutoSetPackages);
    }

    public List<String> getStaticAutoSetPackages()
    {
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
    public void addStaticAutoSetClasses(Class<?>... staticAutoSetClasses)
    {
        WPTool.addAll(this.staticAutoSetClasses, staticAutoSetClasses);
    }

    public List<Class<?>> getStaticAutoSetClasses()
    {
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
    public void addStaticAutoSetClasses(String... staticAutoSetClasses)
    {
        WPTool.addAll(this.staticAutoSetClassStrs, staticAutoSetClasses);
    }

    public List<String> getStaticAutoSetClassStrs()
    {
        return staticAutoSetClassStrs;
    }

    public List<String> getAutoSetSeekPackages()
    {
        return autoSetSeekPackages;
    }

    public List<CheckPassable> getPorterCheckPassableList()
    {
        return porterCheckPassableList;
    }

    /**
     * 请使用{@linkplain #addPorterCheck(CheckPassable)}
     */
    @Deprecated
    public void addForAllCheckPassable(CheckPassable forAllCheckPassable)
    {
        porterCheckPassableList.add(forAllCheckPassable);
    }

    /**
     * <p>
     * 走的流程为:{@linkplain DuringType#BEFORE_CLASS}--&gt;{@linkplain DuringType#ON_CLASS}--&gt;{@linkplain
     * DuringType#BEFORE_METHOD}--&gt;{@linkplain DuringType#ON_METHOD}--&gt;
     * {@linkplain DuringType#AFTER_METHOD}或{@linkplain DuringType#ON_METHOD_EXCEPTION}
     * <br>
     * 另见：{@linkplain PortIn#checks()}和{@linkplain PortIn#checksForWholeClass()}
     * </p>
     * <p>
     * <strong>注意：调用顺序见{@linkplain DuringType}</strong>
     * </p>
     *
     * @param checkPassable
     */
    public void addPorterCheck(CheckPassable checkPassable)
    {
        porterCheckPassableList.add(checkPassable);
    }

    public String getContentEncoding()
    {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding)
    {
        checkInited();
        this.contentEncoding = contentEncoding;
    }

    public void setContextName(String contextName)
    {
        checkInited();
        PortUtil.checkName(contextName);
        this.name = contextName;
    }

    public String getContextName()
    {
        return name;
    }


    private void checkInited()
    {
        if (isInited)
        {
            throw new RuntimeException("already init!");
        }
    }

    /**
     * 见{@linkplain #isEnableTiedNameDefault()}
     *
     * @param enablePortInTiedNameDefault
     */
    public void setEnableTiedNameDefault(boolean enablePortInTiedNameDefault)
    {
        checkInited();
        this.enablePortInTiedNameDefault = enablePortInTiedNameDefault;
    }

    /**
     * 是否允许{@linkplain PortIn#value()}、{@linkplain PortInObj.Nece#value()}和{@linkplain PortInObj.UnNece#value()}取默认值。默认为true。
     *
     * @return
     */
    public boolean isEnableTiedNameDefault()
    {
        checkInited();
        return enablePortInTiedNameDefault;
    }

    /**
     * 用于对象自动设置。另见{@linkplain AutoSet.Range#Context}
     *
     * @param name
     * @param object
     */
    public void addContextAutoSet(String name, Object object)
    {
        contextAutoSetMap.put(name, object);
    }

    /**
     * 效果同{@linkplain #addContextAutoSet(String, Object) addContextAutoSet(object.getClass().getName(), Object)}
     *
     * @param object
     */
    public void addContextAutoSet(Object object)
    {
        addContextAutoSet(object.getClass().getName(), object);
    }

    /**
     * 效果同{@linkplain #addContextAutoSet(String, Object) addContextAutoSet(clazz.getName(), Object)}
     *
     * @param object
     */
    public void addContextAutoSet(Class<?> clazz, Object object)
    {
        addContextAutoSet(clazz.getName(), object);
    }


    public boolean isResponseWhenException()
    {
        return responseWhenException;
    }

    /**
     * 发生异常时，是否响应错误结果，默认值为true。
     *
     * @param responseWhenException 是否响应错误结果
     */
    public void setResponseWhenException(boolean responseWhenException)
    {
        this.responseWhenException = responseWhenException;
    }

    public SeekPackages getSeekPackages()
    {
        return seekPackages;
    }

    /**
     * <strong>重要:</strong>{@linkplain StateListener}
     */
    public void addStateListener(StateListener stateListener)
    {
        checkInited();
        stateListenerList.add(stateListener);
    }

    /**
     * 添加针对{@linkplain DuringType#ON_CONTEXT}有效的全局检测对象,按顺序调用。
     *
     * @param checkPassable
     */
    public void addContextCheck(CheckPassable checkPassable)
    {
        contextChecks.add(checkPassable);
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        checkInited();
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public List<CheckPassable> getContextChecks()
    {
        return contextChecks;
    }

    public Map<String, Object> getContextAutoSetMap()
    {
        return contextAutoSetMap;
    }

    public List<StateListener> getStateListenerList()
    {
        checkInited();
        return stateListenerList;
    }

    public InitParamSource getUserInitParam()
    {
        checkInited();
        return userInitParam;
    }


    public IArgumentsFactory getArgumentsFactory()
    {
        return iArgumentsFactory;
    }

    public void setArgumentsFactory(IArgumentsFactory iArgumentsFactory)
    {
        checkInited();
        this.iArgumentsFactory = iArgumentsFactory;
    }

    public ParamSourceHandleManager getParamSourceHandleManager()
    {
        return paramSourceHandleManager;
    }

    void initOk()
    {
        isInited = true;
        seekPackages = null;
        userInitParam = null;
        classLoader = null;
        contextChecks = null;
        contextAutoSetMap = null;
        stateListenerList = null;
        porterCheckPassableList = null;
        autoSetSeekPackages = null;
        paramSourceHandleManager = null;
        defaultReturnFactory = null;
        iArgumentsFactory = null;
    }
}
