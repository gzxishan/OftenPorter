package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.*;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.pbridge.PBridge;
import cn.xishan.oftenporter.porter.core.pbridge.PLinker;
import cn.xishan.oftenporter.porter.core.pbridge.PName;
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
public final class PorterMain {
    private PortExecutor portExecutor;

    private boolean isInit;
    private static final Logger LOGGER = LoggerFactory.getLogger(PorterMain.class);
    private final InnerBridge innerBridge;
    private final PLinker pLinker;

    /**
     * @param pName  框架名称。
     * @param bridge 只能访问当前实例的bridge。
     */
    public PorterMain(PName pName, PBridge bridge) {
        this.innerBridge = new InnerBridge();
        pLinker = new DefaultPLinker(pName, bridge);
        pLinker.setPorterAttr(contextName -> {
            Context context = portExecutor == null ? null : portExecutor.getContext(contextName);
            ClassLoader classLoader = null;
            if (context != null) {
                classLoader = context.portContext.getClassLoader();
            }
            return classLoader;
        });
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
        portExecutor = new PortExecutor(pLinker.currentPName(), pLinker, urlDecoder, responseWhenException);
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

    public synchronized void startOne(PorterBridge bridge) throws RuntimeException {
        checkInit();
        if (WPTool.isEmpty(bridge.contextName())) {
            throw new RuntimeException("Context name is empty!");
        } else if (portExecutor.containsContext(bridge.contextName())) {
            throw new RuntimeException("Context named '" + bridge.contextName() + "' already exist!");
        }


        if (innerBridge.allGlobalChecksTemp != null) {//全局检测，在没有启动任何context时有效。
            CheckPassable[] alls = innerBridge.allGlobalChecksTemp.toArray(new CheckPassable[0]);
            innerBridge.allGlobalChecksTemp = null;
            portExecutor.initAllGlobalChecks(alls);
        }


        PorterConf porterConf = bridge.porterConf();
        PortContext portContext = new PortContext();
        portContext.setClassLoader(porterConf.getClassLoader());

        LOGGER.debug(":{}/{} beforeSeek...", pLinker.currentPName(), porterConf.getContextName());

        StateListenerForAll stateListenerForAll = new StateListenerForAll(porterConf.getStateListenerSet());
        ParamSourceHandleManager paramSourceHandleManager = bridge.paramSourceHandleManager();

        stateListenerForAll.beforeSeek(porterConf.getUserInitParam(), porterConf, paramSourceHandleManager);

        InnerContextBridge innerContextBridge = new InnerContextBridge(porterConf.getClassLoader(), innerBridge,
                                                                       porterConf.getContextAutoSetMap(), porterConf.getContextAutoGenImplMap(),
                                                                       porterConf.isEnableTiedNameDefault(), bridge, porterConf.isResponseWhenException());

        portContext.initSeek(porterConf, innerContextBridge);
        LOGGER.debug(":{}/{} afterSeek...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterSeek(porterConf.getUserInitParam(), paramSourceHandleManager);

        portContext.start();

        LOGGER.debug(":{}/{} afterStart...", pLinker.currentPName(), porterConf.getContextName());
        stateListenerForAll.afterStart(porterConf.getUserInitParam());
        portExecutor.addContext(bridge, portContext, stateListenerForAll, innerContextBridge, porterConf.getForAllCheckPassableList().toArray(new CheckPassable[0]));
        porterConf.initOk();
        LOGGER.debug(":{}/{} started!", pLinker.currentPName(), porterConf.getContextName());

    }

    public synchronized void destroyAll() {
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
    }


    private void destroyOne(Context context) {
        checkInit();
        if (context != null && context.stateListenerForAll != null) {
            String contextName = context.getName();
            StateListener stateListenerForAll = context.stateListenerForAll;
            LOGGER.debug("Context [{}] beforeDestroy...", contextName);
            stateListenerForAll.beforeDestroy();
            context.portContext.destroy();
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
