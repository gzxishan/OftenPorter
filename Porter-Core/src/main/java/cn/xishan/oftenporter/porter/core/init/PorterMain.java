package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.*;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetHandle;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.SthDeal;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.local.LocalResponse;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;
import cn.xishan.oftenporter.porter.simple.DefaultListenerAdder;
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
    public interface ForRequestListener
    {
        /**
         * 返回true表示中断后续操作。
         */
        boolean beforeDoRequest(PreRequest req, WRequest request, WResponse response, boolean isInnerRequest);
    }

    private PortExecutor portExecutor;

    private boolean isInit;
    private InnerBridge innerBridge;
    private PLinker pLinker;
    private IListenerAdder<OnPorterAddListener> IListenerAdder;
    private PorterData porterData;
    private static HashMap<String, CommonMain> commonMainHashMap = new HashMap<>();

    private Logger LOGGER;
    private static String currentPNameForLogger;
    private IArgumentsFactory defaultArgumentsFactory = new DefaultArgumentsFactory();
    private ForRequestListener forRequestListener;

    static
    {
        if (!LogUtil.isDefaultLogger)
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
    }

    public PorterMain(PName pName, CommonMain commonMain)
    {
        PBridge inner = (request, callback) ->
        {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null)
            {
                doRequest(req, request, resp, true);
            }
        };
        PBridge current = (request, callback) ->
        {
            LocalResponse resp = new LocalResponse(callback);
            PreRequest req = forRequest(request, resp);
            if (req != null)
            {
                doRequest(req, request, resp, false);
            }
        };

        initPorterMain(pName, commonMain, current, inner);
    }

    /**
     * @param pName         框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    public PorterMain(PName pName, CommonMain commonMain, PBridge currentBridge, PBridge innerBridge)
    {
        initPorterMain(pName, commonMain, currentBridge, innerBridge);
    }

    public void setForRequestListener(ForRequestListener forRequestListener)
    {
        this.forRequestListener = forRequestListener;
    }

    /**
     * @param pName         框架名称。
     * @param currentBridge 只能访问当前实例的bridge。
     * @param innerBridge
     */
    private void initPorterMain(PName pName, CommonMain commonMain, PBridge currentBridge, PBridge innerBridge)
    {
        synchronized (PorterMain.class)
        {
            this.innerBridge = new InnerBridge(commonMain.getDefaultTypeParserId());
            IListenerAdder = new DefaultListenerAdder<>();
            pLinker = new DefaultPLinker(pName, currentBridge, innerBridge);
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

    private void doGlobalCheckAutoSet(AutoSetHandle autoSetHandle, CheckPassable[] alls)
    {
        if (alls == null)
        {
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
        } catch (Throwable e)
        {
            e = WPTool.getCause(e);
            LOGGER.error(e.getMessage(), e);
            try
            {
                throw e;
            } catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }
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

        IConfigData iConfigData = porterConf.getIAnnotationConfigable().getConfig(porterConf.getAnnotationConfig());
        porterConf.addContextAutoSet(IConfigData.class, iConfigData);

        if (porterConf.isEnableAnnotationConfigable())
        {
            AnnoUtil.pushAnnotationConfigable(iConfigData, porterConf.getIAnnotationConfigable());
            if (porterConf.isDefaultIAnnotationConfigable())
            {
                AnnoUtil.setDefaultConfigable(iConfigData, porterConf.getIAnnotationConfigable());
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
                .newInstance(argumentsFactory, innerContextBridge, getPLinker(), porterData,
                        autoSetObjForAspectOfNormal, porterConf.getContextName());

        autoSetHandle.addAutoSetsForNotPorter(innerContextBridge.contextAutoSet.values());
        autoSetHandle.addAutoSetsForNotPorter(argumentsFactory);

        LOGGER.debug("add autoSet StateListener...");
        List<StateListener> stateListenerList = porterConf.getStateListenerList();
        autoSetHandle.addAutoSetsForNotPorter(stateListenerList);

        LOGGER.debug("add autoSet for addAutoSetsForNotPorter...");
        autoSetHandle.addAutoSetsForNotPorter(porterConf.getAutoSetSeekObjectsForSetter());

        LOGGER.debug(":{}/{} beforeSeek...", pLinker.currentPName(), porterConf.getContextName());
        StateListenerForAll stateListenerForAll = new StateListenerForAll(stateListenerList);
        ParamSourceHandleManager paramSourceHandleManager = bridge.paramSourceHandleManager();

        stateListenerForAll.beforeSeek(porterConf.getUserInitParam(), porterConf, paramSourceHandleManager);


        doGlobalCheckAutoSet(autoSetHandle, alls);

        Map<Class<?>, CheckPassable> classCheckPassableMap;
        SthDeal sthDeal = new SthDeal();
        List<PortIniter> portIniterList = new ArrayList<>();

        try
        {
            classCheckPassableMap = contextPorter
                    .initSeek(sthDeal, IListenerAdder, porterConf, autoSetHandle, portIniterList);
        } catch (Exception e)
        {
            throw new Error(WPTool.getCause(e));
        }

        LOGGER.debug("add autoSet CheckPassable of Class and Method...");
        autoSetHandle.addAutoSetsForNotPorter(classCheckPassableMap.values());

        LOGGER.debug(":{}/{} afterSeek...", pLinker.currentPName(), porterConf.getContextName());
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

        try
        {

            LOGGER.debug("start doAutoSet...");
            autoSetHandle.doAutoSetNormal(autoSetObjForAspectOfNormal);//变量设置处理
            autoSetHandle.doAutoSetThat(autoSetObjForAspectOfNormal);
            LOGGER.debug("doAutoSetFinished.");

            String path = "/" + porterConf.getContextName() + "/:" + AutoSet.SetOk.class
                    .getSimpleName() + "/:" + AutoSet.SetOk.class.getSimpleName();
            UrlDecoder.Result result = getUrlDecoder().decode(path);
            PRequest request = new PRequest(PortMethod.GET, path);
            WResponse response = new LocalResponse(lResponse -> {
            });
            WObject wObject = portExecutor
                    .forPortInit(getPLinker().currentPName(), result, request, response, context, true);

            LOGGER.debug("start invokeSetOk...");
            autoSetHandle.invokeSetOk(wObject);
            portExecutor.onContextStarted(context);
////////////////////////////////////////////////////////////////////////////////
            LOGGER.debug(":{}/{} beforeStart...", pLinker.currentPName(), porterConf.getContextName());

            path = "/" + porterConf.getContextName() + "/:" + PortIn.PortStart.class
                    .getSimpleName() + "/:" + PortIn.PortStart.class.getSimpleName();
            result = getUrlDecoder().decode(path);
            request = new PRequest(PortMethod.GET, path);
            response = new LocalResponse(lResponse -> {
            });

            wObject = portExecutor
                    .forPortInit(getPLinker().currentPName(), result, request, response, context, true);

            Map<String, One> entityOneMap = new HashMap<>();
            contextPorter.start(wObject, entityOneMap, sthDeal, innerContextBridge);
            portExecutor.putAllExtraEntity(entityOneMap);

            AspectHandleOfPortInUtil.invokeFinalListener_beforeFinal(wObject);
            AspectHandleOfPortInUtil.invokeFinalListener_afterFinal(wObject);

            LOGGER.debug(":{}/{} afterStart...", pLinker.currentPName(), porterConf.getContextName());
            stateListenerForAll.afterStart(porterConf.getUserInitParam());

            porterConf.initOk();
            LOGGER.debug(":{}/{} porterOne started!", pLinker.currentPName(), porterConf.getContextName());


            LOGGER.debug("*********************************");
            LOGGER.debug(":{}/{} before @PortInit...", pLinker.currentPName(), porterConf.getContextName());
            for (PortIniter portIniter : portIniterList)
            {
                portIniter.init(this.getPLinker());
            }
            LOGGER.debug(":{}/{} done @PortInit.", pLinker.currentPName(), porterConf.getContextName());
            AnnoUtil.popAnnotationConfigable();
        } catch (Exception e)
        {
            throw new Error(WPTool.getCause(e));
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

    public void doRequest(PreRequest req, WRequest request, WResponse response, boolean isInnerRequest)
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

    public PreRequest forRequest(WRequest request, WResponse response)
    {
        PreRequest preRequest = portExecutor.forRequest(request, response);
        return preRequest;
    }
}
