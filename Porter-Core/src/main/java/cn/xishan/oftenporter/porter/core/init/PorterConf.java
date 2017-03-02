package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.base.*;

import java.util.*;

/**
 * 框架配置对象。非线程安全。
 */
public class PorterConf
{
    private SeekPackages seekPackages;
    private InitParamSource userInitParam;
    private Set<StateListener> stateListenerSet;
    private List<CheckPassable> contextChecks;
    private Map<String, Object> contextAutoSetMap;
    private Map<String, Class<?>> contextAutoGenImplMap;
    private ClassLoader classLoader;
    private boolean responseWhenException = true;
    private boolean enablePortInTiedNameDefault = true;
    private boolean isInited;
    private String name;
    private String contentEncoding = "utf-8";
    private List<CheckPassable> forAllCheckPassableList;
    private String[] autoSetSeekPackages;
    private ParamSourceHandleManager paramSourceHandleManager;


    PorterConf()
    {
        seekPackages = new SeekPackages();
        stateListenerSet = new HashSet<>();
        contextChecks = new ArrayList<>();
        forAllCheckPassableList = new ArrayList<>();
        userInitParam = new InitParamSourceImpl();
        contextAutoSetMap = new HashMap<>();
        contextAutoGenImplMap = new HashMap<>();
        paramSourceHandleManager=new ParamSourceHandleManager();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 用于扫描包中含有{@linkplain cn.xishan.oftenporter.porter.core.annotation.AutoSetSeek}注解的类，进而注入里面的变量。
     *
     * @param autoSetSeekPackages
     */
    public void setAutoSetSeekPackages(String... autoSetSeekPackages)
    {
        this.autoSetSeekPackages = autoSetSeekPackages;
    }

    public String[] getAutoSetSeekPackages()
    {
        return autoSetSeekPackages;
    }

    public List<CheckPassable> getForAllCheckPassableList()
    {
        return forAllCheckPassableList;
    }

    /**
     * 所有的时期都会调用它,并且该检测最先被调用。
     *
     * @param forAllCheckPassable
     */
    public void addForAllCheckPassable(CheckPassable forAllCheckPassable)
    {
        forAllCheckPassableList.add(forAllCheckPassable);
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
     * 用于添加接口实现.
     *
     * @param name      名称
     * @param implClass 实现类。
     */
    public void addContextAutoGenImpl(String name, Class<?> implClass)
    {
        contextAutoGenImplMap.put(name, implClass);
    }

    public Map<String, Class<?>> getContextAutoGenImplMap()
    {
        return contextAutoGenImplMap;
    }

    public boolean isResponseWhenException()
    {
        return responseWhenException;
    }


    public SeekPackages getSeekPackages()
    {
        return seekPackages;
    }

    public void addStateListener(StateListener stateListener)
    {
        checkInited();
        stateListenerSet.add(stateListener);
    }

    /**
     * 添加针对当前context有效的全局检测对象。
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

    public Set<StateListener> getStateListenerSet()
    {
        checkInited();
        return stateListenerSet;
    }

    public InitParamSource getUserInitParam()
    {
        checkInited();
        return userInitParam;
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
        contextAutoGenImplMap = null;
        stateListenerSet = null;
        forAllCheckPassableList = null;
        autoSetSeekPackages = null;
        paramSourceHandleManager=null;
    }
}
