package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.*;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
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
public final class PorterMain {
    private PortExecutor portExecutor;

    private boolean isInit;
    private InnerBridge innerBridge;
    private PLinker pLinker;
    private ListenerAdderImpl listenerAdder;
    private PorterData porterData;
    private static HashMap<String, CommonMain> commonMainHashMap = new HashMap<>();

    private Logger LOGGER;
    private static String currentPNameForLogger;

    static {
        if (!LogUtil.isDefaultLogger) {
            LogUtil.setDefaultOnGetLoggerListener(new LogUtil.OnGetLoggerListener() {
                @Override
                public Logger getLogger(String name) {
                    return LoggerFactory
                            .getLogger(currentPNameForLogger == null ? name : name + "." + currentPNameForLogger);
                }
            });
        }
    }

    public PorterMain(PName pName, CommonMain commonMain) {
        PBridge inner = (request, callback) ->
        {
            ABOption abOption = request._getABOption_();
            if (abOption == null) {
                abOption = new ABOption(null, PortFunType.INNER, ABInvokeOrder.OTHER);
                request._setABOption_(abOption);
            }
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null) {
                doRequest(req, request, resp);
            }
        };
        PBridge current = (request, callback) ->
        {
            ABOption abOption = request._getABOption_();
            if (abOption == null) {
                abOption = new ABOption(null, PortFunType.INNER, ABInvokeOrder._OTHER_BEFORE);
                request._setABOption_(abOption);
            }
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null) {
                doRequest(req, request, resp);
            }
        };

        initPorterMain(pName, commonMain, current, inner);
    }

    /**
     * @param pName         框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    public PorterMain(PName pName, CommonMain commonMain, PBridge currentBridge, PBridge innerBridge) {
        initPorterMain(pName, commonMain, currentBridge, innerBridge);
    }

    /**
     * @param pName         框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    private void initPorterMain(PName pName, CommonMain commonMain, PBridge currentBridge, PBridge innerBridge) {
        synchronized (PorterMain.class) {
            this.innerBridge = new InnerBridge();
            listenerAdder = new ListenerAdderImpl();
            pLinker = new DefaultPLinker(pName, currentBridge, innerBridge);
            pLinker.setPorterAttr(contextName ->
            {
                Context context = portExecutor == null ? null : portExecutor.getContext(contextName);
                ClassLoader classLoader = null;
                if (context != null) {
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

    public ListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder() {
        return listenerAdder;
    }

    /**
     * 根据名称获取。
     *
     * @param pName
     * @return
     */
    public synchronized static CommonMain getMain(String pName) {
        return commonMainHashMap.get(pName);
    }

    public PorterConf newPorterConf() {
        return new PorterConf();
    }


    public synchronized void addGlobalCheck(CheckPassable checkPassable) throws RuntimeException {
        if (innerBridge.allGlobalChecksTemp == null) {
            throw new RuntimeException("just for the time when has no context!");
        }
        innerBridge.allGlobalChecksTemp.add(checkPassable);
    }

    public synchronized void init(UrlDecoder urlDecoder, boolean responseWhenException) {
        if (isInit) {
            throw new RuntimeException("already init!");
        }
        isInit = true;
        currentPNameForLogger = getPLinker().currentPName().getName();
        portExecutor = new PortExecutor(pLinker.currentPName(), pLinker, urlDecoder, responseWhenException);
        porterData = new PorterDataImpl(portExecutor);
        currentPNameForLogger = null;
    }

    public UrlDecoder getUrlDecoder() {
        return portExecutor.getUrlDecoder();
    }

    public PLinker getPLinker() {
        return pLinker;
    }

    private void checkInit() {
        if (!isInit) {
            throw new RuntimeException("not init!");
        }
    }

    public synchronized void addGlobalTypeParser(ITypeParser typeParser) {
        innerBridge.globalParserStore.putParser(typeParser);
    }

    public synchronized void addGlobalAutoSet(String name, Object object) {
        Object last = innerBridge.globalAutoSet.put(name, object);
        if (last != null) {
            LOGGER.warn("the global object named '{}' added before [{}]", name, last);
        }
    }

    private void doGlobalCheckAutoSet(AutoSetHandle autoSetHandle, CheckPassable[] alls) {
        if (alls == null) {
            return;
        }
        LOGGER.debug("add doGlobalCheckAutoSet...");
        autoSetHandle.addAutoSetsForNotPorter(alls);
    }

    /**
     * 注意：
     *
     * @param bridge
     */
    public synchronized void startOne(PorterBridge bridge) {
        LogUtil.LogKey logKey = new LogUtil.LogKey(KeyUtil.random48Key());
        try {
            if (WPTool.isEmpty(bridge.contextName())) {
                throw new RuntimeException("Context name is empty!");
            } else if (portExecutor.containsContext(bridge.contextName())) {
                throw new RuntimeException("Context named '" + bridge.contextName() + "' already exist!");
            }
            LogUtil.setOrRemoveOnGetLoggerListener(logKey,
                    name -> LoggerFactory.getLogger(
                            name + "." + getPLinker().currentPName().getName() + "." + bridge.contextName()));
            checkInit();
            currentPNameForLogger = getPLinker().currentPName().getName();
            _startOne(bridge);
            currentPNameForLogger = null;
        } catch (Exception e) {
            throw e;
        } finally {
            LogUtil.setOrRemoveOnGetLoggerListener(logKey, null);
        }
    }

    public PorterData getPorterData() {
        return porterData;
    }

    private void _startOne(PorterBridge bridge) {
        CheckPassable[] alls = null;
        if (innerBridge.allGlobalChecksTemp != null) {//全局检测，在没有启动任何context时有效。
            alls = innerBridge.allGlobalChecksTemp.toArray(new CheckPassable[0]);
            innerBridge.allGlobalChecksTemp = null;
            portExecutor.initAllGlobalChecks(alls);
        }
        Logger LOGGER = LogUtil.logger(PorterMain.class);

        PorterConf porterConf = bridge.porterConf();
        ContextPorter contextPorter = new ContextPorter();
        contextPorter.setClassLoader(porterConf.getClassLoader());

        InnerContextBridge innerContextBridge = new InnerContextBridge(porterConf.getClassLoader(), innerBridge,
                porterConf.getContextAutoSetMap(), porterConf.getContextAutoGenImplMap(),
                porterConf.isEnableTiedNameDefault(), bridge, porterConf.getDefaultPortOutType(), porterConf.isResponseWhenException());

        AutoSetHandle autoSetHandle = AutoSetHandle
                .newInstance(innerContextBridge, getPLinker(), porterData, porterConf.getContextName());

        LOGGER.debug("do autoSet StateListener...");
        Set<StateListener> stateListenerSet = porterConf.getStateListenerSet();
        autoSetHandle.addAutoSetsForNotPorter(stateListenerSet.toArray(new StateListener[0]));

        LOGGER.debug(":{}/{} beforeSeek...", pLinker.currentPName(), porterConf.getContextName());
        StateListenerForAll stateListenerForAll = new StateListenerForAll(stateListenerSet);
        ParamSourceHandleManager paramSourceHandleManager = bridge.paramSourceHandleManager();

        stateListenerForAll.beforeSeek(porterConf.getUserInitParam(), porterConf, paramSourceHandleManager);


        doGlobalCheckAutoSet(autoSetHandle, alls);

        Map<Class<?>, CheckPassable> classCheckPassableMap;
        SthDeal sthDeal = new SthDeal();

        try {
            classCheckPassableMap = contextPorter.initSeek(sthDeal, listenerAdder, porterConf, autoSetHandle);
        } catch (Exception e) {
            throw new Error(WPTool.getCause(e));
        }

        LOGGER.debug("add autoSet CheckPassable of Class and Method...");
        autoSetHandle.addAutoSetsForNotPorter(classCheckPassableMap.values().toArray(new CheckPassable[0]));

        LOGGER.debug(":{}/{} afterSeek...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterSeek(porterConf.getUserInitParam(), paramSourceHandleManager);

        LOGGER.debug("add autoSetSeek...");
        autoSetHandle.addAutoSetSeek(porterConf.getAutoSetSeekPackages(), porterConf.getClassLoader());

        LOGGER.debug("add staticAutoSet...");
        autoSetHandle.addStaticAutoSet(porterConf.getStaticAutoSetPackages(), porterConf.getStaticAutoSetClassStrs(),
                porterConf.getStaticAutoSetClasses(), porterConf.getClassLoader());


        CheckPassable[] forAllCheckPassables = porterConf.getForAllCheckPassableList().toArray(new CheckPassable[0]);
        LOGGER.debug("add autoSet ForAllCheckPassable...");
        autoSetHandle.addAutoSetsForNotPorter(forAllCheckPassables);


        CheckPassable[] contextChecks = porterConf.getContextChecks().toArray(new CheckPassable[0]);
        LOGGER.debug("add autoSet ForContextCheckPassable...");
        autoSetHandle.addAutoSetsForNotPorter(contextChecks);

        Context context = portExecutor.addContext(bridge, contextPorter, stateListenerForAll, innerContextBridge, contextChecks,
                forAllCheckPassables);

        try {
            autoSetHandle.doAutoSet();//变量设置处理
            sthDeal.dealPortAB(portExecutor.getContext(porterConf.getContextName()), portExecutor);//处理After和Before

            String path = "/" + porterConf.getContextName() + "/:" + AutoSet.SetOk.class.getSimpleName() + "/:" + AutoSet.SetOk.class.getSimpleName();
            UrlDecoder.Result result = getUrlDecoder().decode(path);
            PRequest request = new PRequest(path);
            WResponse response = new LocalResponse(new PCallback() {
                @Override
                public void onResponse(PResponse lResponse) {

                }
            });
            WObject wObject = portExecutor.forPortInit(getPLinker().currentPName(), result, request, response, context);

            autoSetHandle.invokeSetOk(wObject);

        } catch (Exception e) {
            throw new Error(WPTool.getCause(e));
        }

        LOGGER.debug(":{}/{} beforeStart...", pLinker.currentPName(), porterConf.getContextName());

        String path = "/" + porterConf.getContextName() + "/:" + PortIn.PortStart.class.getSimpleName() + "/:" + PortIn.PortStart.class.getSimpleName();
        UrlDecoder.Result result = getUrlDecoder().decode(path);
        PRequest request = new PRequest(path);
        WResponse response = new LocalResponse(new PCallback() {
            @Override
            public void onResponse(PResponse lResponse) {

            }
        });
        WObject wObject = portExecutor.forPortInit(getPLinker().currentPName(), result, request, response, context);
        contextPorter.start(wObject);

        LOGGER.debug(":{}/{} afterStart...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterStart(porterConf.getUserInitParam());

        porterConf.initOk();
        LOGGER.debug(":{}/{} porterOne started!", pLinker.currentPName(), porterConf.getContextName());

    }

    /**
     * 销毁所有的，以后不能再用。
     */
    public void destroyAll() {
        synchronized (PorterMain.class) {
            checkInit();
            LOGGER.debug("[{}] destroyAll...", getPLinker().currentPName());
            Iterator<Context> iterator = portExecutor.contextIterator();
            while (iterator.hasNext()) {
                Context context = iterator.next();
                context.setEnable(false);
                destroyOne(context);
            }
            portExecutor.clear();
            LOGGER.debug("[{}] destroyAll end!", getPLinker().currentPName());
            commonMainHashMap.remove(pLinker.currentPName().getName());
        }
    }


    private void destroyOne(Context context) {
        checkInit();
        if (context != null && context.stateListenerForAll != null) {
            String contextName = context.getName();
            StateListener stateListenerForAll = context.stateListenerForAll;
            LOGGER.debug("Context [{}] beforeDestroy...", contextName);
            stateListenerForAll.beforeDestroy();
            context.contextPorter.destroy();
            LOGGER.debug("Context [{}] destroyed!", contextName);
            stateListenerForAll.afterDestroy();
        }
    }

    public synchronized void destroyOne(String contextName) {
        checkInit();
        Context context = portExecutor.removeContext(contextName);
        destroyOne(context);
    }

    public synchronized void enableContext(String contextName, boolean enable) {
        checkInit();
        portExecutor.enableContext(contextName, enable);
    }

    public void doRequest(PreRequest req, WRequest request, WResponse response) {
        portExecutor.doRequest(req, request, response);
    }

    public PreRequest forRequest(WRequest request, WResponse response) {
        PreRequest preRequest = portExecutor.forRequest(request, response);
        return preRequest;
    }
}
