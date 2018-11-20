package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.*;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import cn.xishan.oftenporter.porter.simple.DefaultBridgeLinker;
import cn.xishan.oftenporter.porter.simple.DefaultListenerAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
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
    public interface ForRequestListener
    {
        /**
         * 返回true表示中断后续操作。
         */
        boolean beforeDoRequest(PreRequest req, OftenRequest request, OftenResponse response, boolean isInnerRequest);
    }

    private PortExecutor portExecutor;

    private boolean isInit;
    private InnerBridge innerBridge;
    private CommonMain commonMain;
    private BridgeLinker bridgeLinker;
    private IListenerAdder<OnPorterAddListener> IListenerAdder;
    private PorterData porterData;
    private static HashMap<String, CommonMain> commonMainHashMap = new HashMap<>();

    private Logger LOGGER;
    private static String currentBridgeNameForLogger;
    private IArgumentsFactory defaultArgumentsFactory = new DefaultArgumentsFactory();
    private ForRequestListener forRequestListener;


    static
    {
        if (!LogUtil.isDefaultLogger())
        {
            LogUtil.setDefaultOnGetLoggerListener(new LogUtil.OnGetLoggerListener()
            {
                @Override
                public Logger getLogger(String name)
                {
                    return LoggerFactory.getLogger(currentBridgeNameForLogger == null ?
                            name : name + "." + currentBridgeNameForLogger);
                }
            });
        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            private final Logger logger = LoggerFactory.getLogger(PorterMain.class);

            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("uncaughtException:" + t.getName() + ":" + e.getMessage(), e);
            }
        });
    }

    public PorterMain(BridgeName bridgeName, CommonMain commonMain)
    {
        IBridge inner = (request, callback) ->
        {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null)
            {
                doRequest(req, request, resp, true);
            }
        };
        IBridge current = (request, callback) ->
        {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null)
            {
                doRequest(req, request, resp, false);
            }
        };

        initPorterMain(bridgeName, commonMain, current, inner);
    }

    /**
     * @param bridgeName    框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    public PorterMain(BridgeName bridgeName, CommonMain commonMain, IBridge currentBridge, IBridge innerBridge)
    {
        initPorterMain(bridgeName, commonMain, currentBridge, innerBridge);
    }

    public void setForRequestListener(ForRequestListener forRequestListener)
    {
        this.forRequestListener = forRequestListener;
    }

    /**
     * @param bridgeName    框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    private void initPorterMain(BridgeName bridgeName, CommonMain commonMain, IBridge currentBridge,
            IBridge innerBridge)
    {
        this.commonMain = commonMain;
        this.innerBridge = new InnerBridge(commonMain.getDefaultTypeParserId());
        IListenerAdder = new DefaultListenerAdder<>();
        bridgeLinker = new DefaultBridgeLinker(bridgeName, currentBridge, innerBridge);
        bridgeLinker.setPorterAttr(contextName ->
        {
            Context context = portExecutor == null ? null : portExecutor.getContext(contextName);
            ClassLoader classLoader = null;
            if (context != null)
            {
                classLoader = context.contextPorter.getClassLoader();
            }
            return classLoader;
        });
        currentBridgeNameForLogger = bridgeName.getName();
        LOGGER = LogUtil.logger(PorterMain.class);
        currentBridgeNameForLogger = null;
    }

    public IListenerAdder<OnPorterAddListener> getOnPorterAddListenerAdder()
    {
        return IListenerAdder;
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

    /**
     * 添加针对{@linkplain DuringType#ON_GLOBAL}有效的全局检测对象,按顺序调用。
     *
     * @param checkPassable
     */
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
        currentBridgeNameForLogger = getBridgeLinker().currentName().getName();
        portExecutor = new PortExecutor(bridgeLinker.currentName(), bridgeLinker, urlDecoder, responseWhenException);
        porterData = new PorterDataImpl(portExecutor);
        currentBridgeNameForLogger = null;
    }

    public UrlDecoder getUrlDecoder()
    {
        return portExecutor.getUrlDecoder();
    }

    public BridgeLinker getBridgeLinker()
    {
        return bridgeLinker;
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

    private void doGlobalCheckAutoSet(AutoSetHandle autoSetHandle, CheckPassable[] alls)
    {
        if (alls == null)
        {
            return;
        }
        LOGGER.debug("add doGlobalCheckAutoSet...");
        autoSetHandle.addAutoSetsForNotPorter(alls);
    }

    public synchronized void seekImporter(PorterConf porterConf, Class[] importers) throws Throwable
    {
        if (OftenTool.isEmpty(importers))
        {
            return;
        }
        LOGGER.debug("seek importers...");
        for (Class clazz : importers)
        {
            Annotation[] annotations = AnnoUtil.getAnnotations(clazz);
            for (Annotation annotation : annotations)
            {
                Importer importer = AnnoUtil.getAnnotation(annotation.annotationType(), Importer.class);
                if (importer != null)
                {
                    Class[] confClasses = importer.value();
                    for (Class confClass : confClasses)
                    {
                        try
                        {
                            LOGGER.debug("init importer:class={}", confClass);
                            Object object = OftenTool.newObject(confClass);
                            if (OftenTool.isAssignable(confClass, Importer.Configable.class))
                            {
                                LOGGER.debug("init for:{}", Importer.Configable.class);
                                Importer.Configable configable = (Importer.Configable) object;
                                configable.beforeCustomerConfig(porterConf, annotation);
                            }
                            LOGGER.debug("init importer finished:class={}", confClass);
                        } catch (Throwable e)
                        {
                            if (importer.exceptionThrow())
                            {
                                throw e;
                            } else
                            {
                                LOGGER.debug(e.getMessage(), e);
                            }
                        }
                    }

                }
            }
        }
        LOGGER.debug("seek importers finished.");
    }

    /**
     * 注意：
     *
     * @param bridge
     */
    public synchronized void startOne(PorterBridge bridge)
    {
        LogUtil.LogKey logKey = new LogUtil.LogKey(OftenKeyUtil.random48Key());
        try
        {
            if (OftenTool.isEmpty(bridge.contextName()))
            {
                throw new RuntimeException("Context name is empty!");
            } else if (portExecutor.containsContext(bridge.contextName()))
            {
                throw new RuntimeException("Context named '" + bridge.contextName() + "' already exist!");
            }
            LogUtil.setOrRemoveOnGetLoggerListener(logKey,
                    name -> LoggerFactory.getLogger(
                            name + "." + getBridgeLinker().currentName().getName() + "." + bridge.contextName()));
            checkInit();
            currentBridgeNameForLogger = getBridgeLinker().currentName().getName();
            commonMainHashMap.put(this.bridgeLinker.currentName().getName(), commonMain);
            _startOne(bridge);
            currentBridgeNameForLogger = null;
        } catch (Throwable e)
        {
            throw new Error(OftenTool.getCause(e));
        } finally
        {
            LogUtil.setOrRemoveOnGetLoggerListener(logKey, null);
        }
    }

    public PorterData getPorterData()
    {
        return porterData;
    }

    private void _startOne(PorterBridge bridge) throws Throwable
    {
        long time;
        CheckPassable[] alls = null;
        if (innerBridge.allGlobalChecksTemp != null)
        {//全局检测，在没有启动任何context时有效。
            alls = innerBridge.allGlobalChecksTemp.toArray(new CheckPassable[0]);
            innerBridge.allGlobalChecksTemp = null;
            portExecutor.setAllGlobalChecks(alls);
        }
        Logger LOGGER = LogUtil.logger(PorterMain.class);

        PorterConf porterConf = bridge.porterConf();
        ContextPorter contextPorter = new ContextPorter();
        contextPorter.setClassLoader(porterConf.getClassLoader());

        IConfigData iConfigData = porterConf.getConfigData();
        porterConf.addContextAutoSet(IConfigData.class, iConfigData);

        if (porterConf.isEnableAnnotationConfigable())
        {
            AnnoUtil.pushAnnotationConfigable(porterConf.getIAnnotationConfigable());
            if (porterConf.isDefaultIAnnotationConfigable())
            {
                AnnoUtil.setDefaultConfigable(porterConf.getIAnnotationConfigable());
            }
        }

        IAutoSetListener[] autoSetListeners = porterConf.getAutoSetListenerList().toArray(new IAutoSetListener[0]);
        InnerContextBridge innerContextBridge = new InnerContextBridge(porterConf.getClassLoader(), innerBridge,
                porterConf.getContextAutoSetMap(), porterConf.isEnableTiedNameDefault(), bridge,
                porterConf.getDefaultPortOutType(), autoSetListeners, porterConf.isResponseWhenException());

        IArgumentsFactory argumentsFactory = porterConf.getArgumentsFactory();
        if (argumentsFactory == null)
        {
            argumentsFactory = defaultArgumentsFactory;
        }


        AutoSetObjForAspectOfNormal autoSetObjForAspectOfNormal = null;
        if (porterConf.isEnableAspectOfNormal())
        {
            autoSetObjForAspectOfNormal = new AutoSetObjForAspectOfNormal();
            LOGGER.debug("{} is enabled!", AspectOperationOfNormal.class.getSimpleName());
        }

        AutoSetHandle autoSetHandle = AutoSetHandle
                .newInstance(iConfigData, argumentsFactory, innerContextBridge, getBridgeLinker(), porterData,
                        autoSetObjForAspectOfNormal, porterConf.getContextName());

        autoSetHandle.addAutoSetsForNotPorter(innerContextBridge.contextAutoSet.values());
        autoSetHandle.addAutoSetsForNotPorter(argumentsFactory);

        LOGGER.debug("add autoSet StateListener...");
        List<StateListener> stateListenerList = porterConf.getStateListenerList();
        autoSetHandle.addAutoSetsForNotPorter(stateListenerList);

        LOGGER.debug("add autoSet for addAutoSetsForNotPorter...");
        autoSetHandle.addAutoSetsForNotPorter(porterConf.getAutoSetSeekObjectsForSetter());

        LOGGER.debug(":{}/{} beforeSeek...", bridgeLinker.currentName(), porterConf.getContextName());
        StateListenerForAll stateListenerForAll = new StateListenerForAll(stateListenerList);
        ParamSourceHandleManager paramSourceHandleManager = bridge.paramSourceHandleManager();

        stateListenerForAll.beforeSeek(porterConf.getUserInitParam(), porterConf, paramSourceHandleManager);


        doGlobalCheckAutoSet(autoSetHandle, alls);

        Map<Class<?>, CheckPassable> classCheckPassableMap;
        SthDeal sthDeal = new SthDeal();
        List<PortIniter> portIniterList = new ArrayList<>();


        LOGGER.debug("start seek...");
        time = System.currentTimeMillis();
        classCheckPassableMap = contextPorter
                .initSeek(sthDeal, IListenerAdder, porterConf, autoSetHandle, portIniterList);
        LOGGER.debug("seek finished,time={}ms", System.currentTimeMillis() - time);


        LOGGER.debug("add autoSet CheckPassable of Class and Method...");
        autoSetHandle.addAutoSetsForNotPorter(classCheckPassableMap.values());

        LOGGER.debug(":{}/{} afterSeek...", bridgeLinker.currentName(), porterConf.getContextName());
        stateListenerForAll.afterSeek(porterConf.getUserInitParam(), paramSourceHandleManager);

        LOGGER.debug("add autoSetSeek...");
        autoSetHandle.addAutoSetSeekPackages(porterConf.getAutoSetSeekPackages(), porterConf.getClassLoader());

        LOGGER.debug("add staticAutoSet...");
        autoSetHandle.addStaticAutoSet(porterConf.getStaticAutoSetPackages(), porterConf.getStaticAutoSetClassStrs(),
                porterConf.getStaticAutoSetClasses(), porterConf.getClassLoader());


        CheckPassable[] porterCheckPassables = porterConf.getPorterCheckPassableList().toArray(new CheckPassable[0]);
        LOGGER.debug("add autoSet ForAllCheckPassable...");
        autoSetHandle.addAutoSetsForNotPorter(porterCheckPassables);


        LOGGER.debug("add autoSet ForContextCheckPassable...");
        autoSetHandle.addAutoSetsForNotPorter(porterConf.getContextChecks());
        autoSetHandle.addAutoSetsForNotPorter(porterConf.getResponseHandles().values());
        autoSetHandle.addAutoSetsForNotPorter(porterConf.getDefaultResponseHandle());

        CheckPassable[] contextChecks = porterConf.getContextChecks().toArray(new CheckPassable[0]);
        Context context = portExecutor.newContext(bridge, contextPorter, stateListenerForAll,
                innerContextBridge, contextChecks, porterCheckPassables, porterConf.getResponseHandles(),
                porterConf.getDefaultResponseHandle());

        {
            Map<String, One> entityOneMap = new HashMap<>();
            contextPorter.initPorter(entityOneMap, sthDeal, innerContextBridge, autoSetHandle);
            portExecutor.putAllExtraEntity(entityOneMap);

            LOGGER.debug("start doAutoSet...");
            time = System.currentTimeMillis();
            autoSetHandle.doAutoSetNormal(autoSetObjForAspectOfNormal);//变量设置处理
            autoSetHandle.doAutoSetThat(autoSetObjForAspectOfNormal);
            LOGGER.debug("doAutoSetFinished,time={}ms", System.currentTimeMillis() - time);

            String path = "/" + porterConf.getContextName() + "/:" + AutoSet.SetOk.class
                    .getSimpleName() + "/:" + AutoSet.SetOk.class.getSimpleName();
            UrlDecoder.Result result = getUrlDecoder().decode(path);
            BridgeRequest request = new BridgeRequest(PortMethod.GET, path);
            OftenResponse response = new LocalResponse(lResponse -> {
            });
            OftenObject oftenObject = portExecutor
                    .forPortInit(getBridgeLinker().currentName(), result, request, response, context, true);

            LOGGER.debug("start invokeSetOk...");
            autoSetHandle.invokeSetOk(oftenObject);
            portExecutor.onContextStarted(context);
////////////////////////////////////////////////////////////////////////////////
            LOGGER.debug(":{}/{} beforeStart...", bridgeLinker.currentName(), porterConf.getContextName());

            path = "/" + porterConf.getContextName() + "/:" + PortStart.class
                    .getSimpleName() + "/:" + PortStart.class.getSimpleName();
            result = getUrlDecoder().decode(path);
            request = new BridgeRequest(PortMethod.GET, path);
            response = new LocalResponse(lResponse -> {
            });

            oftenObject = portExecutor
                    .forPortInit(getBridgeLinker().currentName(), result, request, response, context, true);


            contextPorter.start(oftenObject, iConfigData);

            AspectHandleOfPortInUtil.invokeFinalListener_beforeFinal(oftenObject);
            AspectHandleOfPortInUtil.invokeFinalListener_afterFinal(oftenObject);

            LOGGER.debug(":{}/{} afterStart...", bridgeLinker.currentName(), porterConf.getContextName());
            stateListenerForAll.afterStart(porterConf.getUserInitParam());

            porterConf.initOk();
            LOGGER.debug(":{}/{} porterOne started!", bridgeLinker.currentName(), porterConf.getContextName());


            LOGGER.debug("*********************************");
            LOGGER.debug(":{}/{} before @PortInit...", bridgeLinker.currentName(), porterConf.getContextName());
            for (PortIniter portIniter : portIniterList)
            {
                portIniter.init(this.getBridgeLinker());
            }
            LOGGER.debug(":{}/{} done @PortInit.", bridgeLinker.currentName(), porterConf.getContextName());
            AnnoUtil.popAnnotationConfigable();
            if (autoSetObjForAspectOfNormal != null)
            {
                autoSetObjForAspectOfNormal.clearCache();
            }
            OftenTool.clearCache();
            AnnoUtil.clearCache();
        }
    }

    /**
     * 销毁所有的，以后不能再用。
     */
    public void destroyAll()
    {
        synchronized (PorterMain.class)
        {
            checkInit();
            LOGGER.debug("[{}] destroyAll...", getBridgeLinker().currentName());
            Iterator<Context> iterator = portExecutor.contextIterator();
            while (iterator.hasNext())
            {
                Context context = iterator.next();
                context.setEnable(false);
                destroyOne(context);
            }
            portExecutor.clear();
            LOGGER.debug("[{}] destroyAll end!", getBridgeLinker().currentName());
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
            if (portExecutor.contextSize() == 0)
            {
                commonMainHashMap.remove(bridgeLinker.currentName().getName());
            }
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

    public void doRequest(PreRequest req, OftenRequest request, OftenResponse response, boolean isInnerRequest)
    {
        if (forRequestListener != null)
        {
            if (forRequestListener.beforeDoRequest(req, request, response, isInnerRequest))
            {
                return;
            }
        }
        portExecutor.doRequest(req, request, response, isInnerRequest);
    }

    public PreRequest forRequest(OftenRequest request, OftenResponse response)
    {
        PreRequest preRequest = portExecutor.forRequest(request, response);
        return preRequest;
    }
}
