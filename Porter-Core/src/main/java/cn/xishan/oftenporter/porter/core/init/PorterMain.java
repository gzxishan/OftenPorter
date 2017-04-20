package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.*;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.FatalInitException;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultPLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * 接口入口对象。
 * <pre>
 *     请求格式为[=pname]/contextName/ClassTied/[funTied|restValue][?name1=value1&name2=value2...]
 * </pre>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public final class PorterMain
{
    private PortExecutor portExecutor;

    private boolean isInit, isGlobalAutoSet = false;
    private final InnerBridge innerBridge;
    private final PLinker pLinker;
    private ListenerAdderImpl listenerAdder;
    private PorterData porterData;
    private static HashMap<String, CommonMain> commonMainHashMap = new HashMap<>();

    private final Logger LOGGER;
    private static String currentPNameForLogger;

    static
    {
        LogUtil.setDefaultOnGetLoggerListener(new LogUtil.OnGetLoggerListener()
        {
            @Override
            public Logger getLogger(String name)
            {
                return LoggerFactory
                        .getLogger(currentPNameForLogger == null ? name : name + "." + currentPNameForLogger);
            }
        });
    }

    /**
     * @param pName  框架名称。
     * @param bridge 只能访问当前实例的bridge。
     */
    public PorterMain(PName pName, CommonMain commonMain, PBridge bridge)
    {
        synchronized (PorterMain.class)
        {
            this.innerBridge = new InnerBridge();
            listenerAdder = new ListenerAdderImpl();
            pLinker = new DefaultPLinker(pName, bridge);
            pLinker.setPorterAttr(contextName ->
            {
                Context context = portExecutor == null ? null : portExecutor.getContext(contextName);
                ClassLoader classLoader = null;
                if (context != null)
                {
                    classLoader = context.contextPorter.getClassLoader();
                }
                return classLoader;
            });
            commonMainHashMap.put(pName.getName(), commonMain);
            currentPNameForLogger = pName.getName();
            LOGGER = LogUtil.logger(PorterMain.class);
            currentPNameForLogger = null;
        }
    }

    public ListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
    {
        return listenerAdder;
    }

    /**
     * 根据名称获取。
     *
     * @param pName
     * @return
     */
    public synchronized static CommonMain getMain(String pName)
    {
        return commonMainHashMap.get(pName);
    }

    public PorterConf newPorterConf()
    {
        return new PorterConf();
    }


    public synchronized void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException
    {
        if (innerBridge.allGlobalChecksTemp == null)
        {
            throw new RuntimeException("just for the time when has no context!");
        }
        innerBridge.allGlobalChecksTemp.add(checkPassable);
    }

    public synchronized void init(UrlDecoder urlDecoder, boolean responseWhenException)
    {
        if (isInit)
        {
            throw new RuntimeException("already init!");
        }
        isInit = true;
        currentPNameForLogger = getPLinker().currentPName().getName();
        portExecutor = new PortExecutor(pLinker.currentPName(), pLinker, urlDecoder, responseWhenException);
        porterData = new PorterDataImpl(portExecutor);
        currentPNameForLogger = null;
    }

    public UrlDecoder getUrlDecoder()
    {
        return portExecutor.getUrlDecoder();
    }

    public PLinker getPLinker()
    {
        return pLinker;
    }

    private void checkInit()
    {
        if (!isInit)
        {
            throw new RuntimeException("not init!");
        }
    }

    public synchronized void addGlobalTypeParser(ITypeParser typeParser)
    {
        innerBridge.globalParserStore.putParser(typeParser);
    }

    public synchronized void addGlobalAutoSet(String name, Object object)
    {
        Object last = innerBridge.globalAutoSet.put(name, object);
        if (last != null)
        {
            LOGGER.warn("the global object named '{}' added before [{}]", name, last);
        }
    }

    private void doGlobalCheckAutoSet(AutoSetHandle autoSetHandle)
    {
        if (isGlobalAutoSet)
        {
            return;
        }
        LOGGER.debug("do doGlobalCheckAutoSet...");
        isGlobalAutoSet = true;
        CheckPassable[] alls = portExecutor.getAllGlobalChecks();
        autoSetHandle.addAutoSetsForNotPorter(alls);
    }

    /**
     * 注意：
     *
     * @param bridge
     */
    public synchronized void startOne(PorterBridge bridge)
    {
        LogUtil.LogKey logKey = new LogUtil.LogKey(KeyUtil.random48Key());
        try
        {
            if (WPTool.isEmpty(bridge.contextName()))
            {
                throw new RuntimeException("Context name is empty!");
            } else if (portExecutor.containsContext(bridge.contextName()))
            {
                throw new RuntimeException("Context named '" + bridge.contextName() + "' already exist!");
            }
            LogUtil.setOrRemoveOnGetLoggerListener(logKey,
                    name -> LoggerFactory.getLogger(
                            name + "." + getPLinker().currentPName().getName() + "." + bridge.contextName()));
            checkInit();
            currentPNameForLogger = getPLinker().currentPName().getName();
            _startOne(bridge);
            currentPNameForLogger = null;
        } catch (Exception e)
        {
            throw e;
        } finally
        {
            LogUtil.setOrRemoveOnGetLoggerListener(logKey, null);
        }
    }

    public PorterData getPorterData()
    {
        return porterData;
    }

    private void _startOne(PorterBridge bridge)
    {

        if (innerBridge.allGlobalChecksTemp != null)
        {//全局检测，在没有启动任何context时有效。
            CheckPassable[] alls = innerBridge.allGlobalChecksTemp.toArray(new CheckPassable[0]);
            innerBridge.allGlobalChecksTemp = null;
            portExecutor.initAllGlobalChecks(alls);
        }
        Logger LOGGER = LogUtil.logger(PorterMain.class);

        PorterConf porterConf = bridge.porterConf();
        ContextPorter contextPorter = new ContextPorter();
        contextPorter.setClassLoader(porterConf.getClassLoader());

        InnerContextBridge innerContextBridge = new InnerContextBridge(porterConf.getClassLoader(), innerBridge,
                porterConf.getContextAutoSetMap(), porterConf.getContextAutoGenImplMap(),
                porterConf.isEnableTiedNameDefault(), bridge, porterConf.isResponseWhenException());

        AutoSetHandle autoSetHandle = AutoSetHandle.newInstance(innerContextBridge, getPLinker(), porterData);

        LOGGER.debug("do autoSet StateListener...");
        Set<StateListener> stateListenerSet = porterConf.getStateListenerSet();
        autoSetHandle.addAutoSetsForNotPorter(stateListenerSet.toArray(new StateListener[0]));

        LOGGER.debug(":{}/{} beforeSeek...", pLinker.currentPName(), porterConf.getContextName());
        StateListenerForAll stateListenerForAll = new StateListenerForAll(stateListenerSet);
        ParamSourceHandleManager paramSourceHandleManager = bridge.paramSourceHandleManager();

        stateListenerForAll.beforeSeek(porterConf.getUserInitParam(), porterConf, paramSourceHandleManager);


        doGlobalCheckAutoSet(autoSetHandle);

        Map<Class<?>, CheckPassable> classCheckPassableMap = null;
        try
        {
            classCheckPassableMap = contextPorter.initSeek(listenerAdder, porterConf, autoSetHandle);
        } catch (FatalInitException e)
        {
            throw new Error(e);
        }

        LOGGER.debug("add autoSet CheckPassable of Class and Method...");
        autoSetHandle.addAutoSetsForNotPorter(classCheckPassableMap.values().toArray(new CheckPassable[0]));

        LOGGER.debug(":{}/{} afterSeek...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterSeek(porterConf.getUserInitParam(), paramSourceHandleManager);

        LOGGER.debug("add autoSetSeek...");
        autoSetHandle.addAutoSetSeek(porterConf.getAutoSetSeekPackages(), porterConf.getClassLoader());


        CheckPassable[] checkPassables = porterConf.getForAllCheckPassableList().toArray(new CheckPassable[0]);
        LOGGER.debug("do autoSet ForAllCheckPassable...");
        autoSetHandle.addAutoSetsForNotPorter(checkPassables);

        portExecutor.addContext(bridge, contextPorter, stateListenerForAll, innerContextBridge,
                checkPassables);

        try
        {
            autoSetHandle.doAutoSet();
        } catch (FatalInitException e)
        {
            throw new Error(e);
        }

        contextPorter.start();

        LOGGER.debug(":{}/{} afterStart...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterStart(porterConf.getUserInitParam());

        porterConf.initOk();
        LOGGER.debug(":{}/{} started!", pLinker.currentPName(), porterConf.getContextName());

    }

    /**
     * 销毁所有的，以后不能再用。
     */
    public void destroyAll()
    {
        synchronized (PorterMain.class)
        {
            checkInit();
            LOGGER.debug("[{}] destroyAll...", getPLinker().currentPName());
            Iterator<Context> iterator = portExecutor.contextIterator();
            while (iterator.hasNext())
            {
                Context context = iterator.next();
                context.setEnable(false);
                destroyOne(context);
            }
            portExecutor.clear();
            LOGGER.debug("[{}] destroyAll end!", getPLinker().currentPName());
            commonMainHashMap.remove(pLinker.currentPName().getName());
        }
    }


    private void destroyOne(Context context)
    {
        checkInit();
        if (context != null && context.stateListenerForAll != null)
        {
            String contextName = context.getName();
            StateListener stateListenerForAll = context.stateListenerForAll;
            LOGGER.debug("Context [{}] beforeDestroy...", contextName);
            stateListenerForAll.beforeDestroy();
            context.contextPorter.destroy();
            LOGGER.debug("Context [{}] destroyed!", contextName);
            stateListenerForAll.afterDestroy();
        }
    }

    public synchronized void destroyOne(String contextName)
    {
        checkInit();
        Context context = portExecutor.removeContext(contextName);
        destroyOne(context);
    }

    public synchronized void enableContext(String contextName, boolean enable)
    {
        checkInit();
        portExecutor.enableContext(contextName, enable);
    }

    public void doRequest(PreRequest req, WRequest request, WResponse response)
    {
        portExecutor.doRequest(req, request, response);
    }

    public PreRequest forRequest(WRequest request, WResponse response)
    {
        PreRequest preRequest = portExecutor.forRequest(request, response);
        return preRequest;
    }
}
