package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._BindEntities;
import cn.xishan.oftenporter.porter.core.annotation.sth.OftenEntities;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.bridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultFailedReason;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;
import cn.xishan.oftenporter.porter.simple.EmptyParamSource;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public final class PortExecutor
{

    private final Logger _LOGGER;
    private Map<String, Context> contextMap = new ConcurrentHashMap<>();

    private CheckPassable[] allGlobalChecks;
    private UrlDecoder urlDecoder;
    private boolean responseWhenException;
    private BridgeName bridgeName;
    private DeliveryBuilder deliveryBuilder;
    private PortUtil portUtil;
    private Map<String, One> extraEntityOneMap = new HashMap<>();

    public PortExecutor(BridgeName bridgeName, BridgeLinker bridgeLinker, UrlDecoder urlDecoder,
            boolean responseWhenException)
    {
        _LOGGER = LogUtil.logger(PortExecutor.class);
        portUtil = new PortUtil();
        this.bridgeName = bridgeName;
        this.urlDecoder = urlDecoder;
        this.responseWhenException = responseWhenException;
        deliveryBuilder = DeliveryBuilder.getBuilder(true, bridgeLinker);
    }

    public void putAllExtraEntity(Map<String, One> entityOneMap)
    {
        this.extraEntityOneMap.putAll(entityOneMap);
    }

    private final Logger logger(OftenObject oftenObject)
    {
        return oftenObject == null ? _LOGGER : LogUtil.logger(oftenObject, PortExecutor.class);
    }

    public void setAllGlobalChecks(CheckPassable[] allGlobalChecks)
    {
        this.allGlobalChecks = allGlobalChecks;
    }


    public Context newContext(PorterBridge bridge, ContextPorter contextPorter, StateListener stateListenerForAll,
            InnerContextBridge innerContextBridge, CheckPassable[] contextChecks, CheckPassable[] porterCheckPassables,
            Map<Class, ResponseHandle> responseHandles, ResponseHandle defaultResponseHandle)
    {
        PorterConf porterConf = bridge.porterConf();
        Context context = new Context(deliveryBuilder, contextPorter, contextChecks,
                bridge.paramSourceHandleManager(), stateListenerForAll, innerContextBridge,
                porterCheckPassables, porterConf.getDefaultReturnFactory(), responseHandles, defaultResponseHandle);
        context.name = bridge.contextName();
        context.contentEncoding = porterConf.getContentEncoding();

        return context;
    }

    public void onContextStarted(Context context)
    {
        contextMap.put(context.name, context);
    }

    public UrlDecoder getUrlDecoder()
    {
        return urlDecoder;
    }

    /**
     * 移除指定的context
     *
     * @param contextName context名称
     * @return 返回移除的Context，可能够为null。
     */
    public Context removeContext(String contextName)
    {
        return contextMap.remove(contextName);
    }

    /**
     * 得到指定的context。
     *
     * @param contextName
     * @return
     */
    public Context getContext(String contextName)
    {
        return contextMap.get(contextName);
    }

    public PorterOfFun getPorterOfFun(String pathWithContextName, PortMethod method) throws Exception
    {
        UrlDecoder.Result result = urlDecoder.decode(pathWithContextName);
        Context context = contextMap.get(result.contextName());
        PorterOfFun porterOfFun = null;
        if (context != null)
        {
            Porter classPort = context.contextPorter.getClassPort(result.classTied());
            if (classPort != null)
            {
                porterOfFun = classPort.getChild(result.funTied(), method);
            }
        }
        return porterOfFun;
    }

    /**
     * 是否包含指定的context
     *
     * @param contextName context名称
     * @return 存在返回true，不存在返回false。
     */
    public boolean containsContext(String contextName)
    {
        return contextMap.containsKey(contextName);
    }

    /**
     * 启用或禁用指定context
     *
     * @param contextName context名称
     * @param enable      是否启用
     * @return 返回对应Context，可能为null。
     */
    public Context enableContext(String contextName, boolean enable)
    {
        Context context = contextMap.get(contextName);
        if (context != null)
        {
            context.setEnable(enable);
        }
        return context;
    }


    public void clear()
    {
        contextMap.clear();
    }

    public int contextSize()
    {
        return contextMap.size();
    }

    public Iterator<String> contextNameIterator()
    {
        return contextMap.keySet().iterator();
    }

    public Iterator<Context> contextIterator()
    {
        return contextMap.values().iterator();
    }


    public PreRequest forRequest(OftenRequest request, final OftenResponse response)
    {
        String path = request.getPath();
        UrlDecoder.Result result = null;
        try
        {
            result = urlDecoder.decode(path);
        } catch (Exception e)
        {
            logger(null).warn(e.getMessage(), e);
            exDealUrl(request, response, e.getMessage(), responseWhenException);
            return null;
        }
        Context context;
        if (result == null || (context = contextMap.get(result.contextName())) == null || !context.isEnable)
        {
            exNotFoundClassPort(request, response, responseWhenException);
            return null;
        } else
        {
            Porter classPort = context.contextPorter.getClassPort(result.classTied());
            PorterOfFun funPort;
            InnerContextBridge innerContextBridge = context.innerContextBridge;

            if (classPort == null)
            {
                exNotFoundClassPort(request, response, innerContextBridge.responseWhenException);
                return null;
            } else if ((funPort = classPort.getChild(result, request.getMethod())) == null)
            {
                exNotFoundFun(request, response, result, innerContextBridge.responseWhenException);
                return null;
            }
            return new PreRequest(context, result, classPort, funPort);
        }
    }

    public OftenObject forPortInit(BridgeName bridgeName, UrlDecoder.Result result, OftenRequest request,
            OftenResponse response,
            Context context, boolean isInnerRequest)
    {
        OftenObjectImpl oftenObject = new OftenObjectImpl(bridgeName, result, request, response, context,
                isInnerRequest);
        oftenObject.setParamSource(new EmptyParamSource());
        return oftenObject;
    }

    public final void doRequest(PreRequest req, OftenRequest request, OftenResponse response, boolean isInnerRequest)
    {
        OftenObjectImpl oftenObject = null;
        try
        {

            PorterOfFun funPort = req.funPort;
            Porter classPort = req.classPort;

            Context context = req.context;
            UrlDecoder.Result result = req.result;


            if (!isInnerRequest && funPort.isInner())
            {
                exNotFoundClassPort(request, response, context.innerContextBridge.responseWhenException);
                return;
            }

            oftenObject = new OftenObjectImpl(bridgeName, result, request, response, context, isInnerRequest);
            oftenObject.porterOfFun = funPort;
            oftenObject.portExecutor = this;

            ParamSource paramSource = getParamSource(oftenObject, classPort, funPort);
            oftenObject.setParamSource(paramSource);

            if (isInnerRequest && funPort.isFastInner())
            {
                dealtOfFunParam(context, oftenObject, funPort, result, true);
            } else
            {
                //全局通过检测
                dealtOfGlobalCheck(context, oftenObject, funPort, result);
            }
        } catch (Exception e)
        {

            response.toErr();
            Logger logger = logger(null);
            Throwable ex = getCause(e);
            if (ex instanceof OftenCallException)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(ex.getMessage(), ex);
                }
            } else if (logger.isWarnEnabled())
            {
                logger.warn(ex.getMessage(), ex);
            }
            if (responseWhenException)
            {
                JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
                jResponse.setDescription(OftenTool.getMessage(e));
                try
                {
                    response.write(jResponse);
                } catch (IOException e1)
                {
                    logger.warn(e1.getMessage(), e1);
                }
            }
            close(response);
        }
    }

    private final void exNotFoundFun(OftenRequest request, OftenResponse response, UrlDecoder.Result result,
            boolean responseWhenException)
    {
        response.toErr();
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("fun:" + result.toString());
            doFinalWriteOf404(request, response, jResponse);
        }
        close(response);
    }

    private final void exNotFoundClassPort(OftenRequest request, OftenResponse response, boolean responseWhenException)
    {
        response.toErr();
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("method=" + request.getMethod().name() + ",path=" + request.getPath());
            doFinalWriteOf404(request, response, jResponse);
        }
        close(response);
    }

    private final void exDealUrl(OftenRequest request, OftenResponse response, String msg,
            boolean responseWhenException)
    {
        response.toErr();
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(msg);
            doFinalWriteOf404(request, response, jResponse);
        }
        close(response);
    }


    /**
     * 用于处理对象绑定。
     *
     * @param oftenEntities
     * @param isInClass
     * @param oftenObjectImpl
     * @return
     */
    private ParamDealt.FailedReason paramDealOfPortInEntities(Context context,
            OftenEntities oftenEntities,
            boolean isInClass, Porter porter, PorterOfFun porterOfFun, OftenObjectImpl oftenObjectImpl) throws Exception
    {
        if (oftenEntities == null)
        {
            return null;
        }
        One[] ones = oftenEntities.ones;
        Object[] entities = new Object[ones.length];

        for (int i = 0; i < ones.length; i++)
        {
            Object object = paramDealOfOne(context, isInClass, porter, porterOfFun, oftenObjectImpl, ones[i], null);
            if (object instanceof ParamDealt.FailedReason)
            {
                return (ParamDealt.FailedReason) object;
            } else
            {
                oftenObjectImpl.url().setParam(ones[i].clazz.getName(), object);//设置对象，从而让对应的形参可以获取。
            }
            entities[i] = object;
        }
        if (isInClass)
        {
            oftenObjectImpl.centities = entities;
        } else
        {
            oftenObjectImpl.fentities = entities;
        }
        return null;
    }

    Object getExtrwaEntity(OftenObjectImpl oftenObject, String key)
    {
        One one = extraEntityOneMap.get(key);
        if (one != null)
        {
            PorterOfFun porterOfFun = oftenObject.porterOfFun;
            Object object = paramDealOfOne(oftenObject.context, false, porterOfFun.getPorter(), porterOfFun,
                    oftenObject, one,
                    key);
            if (object instanceof ParamDealt.FailedReason)
            {
                ParamDealt.FailedReason failedReason = (ParamDealt.FailedReason) object;
                JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
                jResponse.setDescription(failedReason.desc());
                jResponse.setExtra(failedReason.toJSON());
                throw new OftenCallException(jResponse);
            }
            return object;
        }
        return null;
    }

    private Object paramDealOfOne(Context context, boolean isInClass, Porter porter, PorterOfFun porterOfFun,
            OftenObjectImpl oftenObjectImpl, One one, @MayNull String optionKey)
    {
        TypeParserStore currentTypeParserStore = context.innerContextBridge.innerBridge.globalParserStore;
        boolean ignoreTypeParser = isInClass ? porter.getPortIn().ignoreTypeParser() : porterOfFun.getMethodPortIn()
                .ignoreTypeParser();
        Object object = portUtil.paramDealOne(oftenObjectImpl, ignoreTypeParser, context.innerContextBridge.paramDealt,
                one, optionKey, oftenObjectImpl.getParamSource(), currentTypeParserStore);

        if (!(object instanceof ParamDealt.FailedReason))
        {
            _BindEntities.CLASS clazz = one.getEntityClazz();
            if (clazz != null)
            {
                try
                {
                    if (isInClass)
                    {
                        object = clazz.deal(oftenObjectImpl, porter, object);
                    } else
                    {
                        object = clazz.deal(oftenObjectImpl, porterOfFun, object);
                    }
                } catch (Exception e)
                {
                    Throwable throwable = OftenTool.getCause(e);
                    object = DefaultFailedReason.parseOftenEntitiesException(throwable.getMessage());
                    logger(oftenObjectImpl).warn(throwable.getMessage(), throwable);
                }

            }
        }
        return object;
    }

    private final void dealtOfGlobalCheck(Context context, OftenObjectImpl oftenObject, PorterOfFun funPort,
            UrlDecoder.Result result)
    {
        CheckPassable[] allGlobal = this.allGlobalChecks;

        if (allGlobal.length == 0)
        {
            dealtOfContextCheck(context, oftenObject, funPort, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, funPort, oftenObject,
                    DuringType.ON_GLOBAL,
                    allGlobal,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {

                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfContextCheck(context, oftenObject, funPort, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfContextCheck(Context context, OftenObjectImpl oftenObject, PorterOfFun funPort,
            UrlDecoder.Result result)
    {
        CheckPassable[] contextChecks = context.contextChecks;
        if (contextChecks.length == 0)
        {
            dealtOfBeforeClassParam(funPort, oftenObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, funPort, oftenObject,
                    DuringType.ON_CONTEXT, contextChecks,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeClassParam(funPort, oftenObject, context, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfBeforeClassParam(PorterOfFun funPort, OftenObjectImpl oftenObject, Context context,
            UrlDecoder.Result result)
    {
        Porter classPort = funPort.getPorter();
        _PortIn clazzPIn = classPort.getPortIn();

        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfClassParam(funPort, oftenObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                    DuringType.BEFORE_CLASS,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfClassParam(funPort, oftenObject, context, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }
    }

    private final void dealtOfClassParam(PorterOfFun funPort, OftenObjectImpl oftenObject, Context context,
            UrlDecoder.Result result)
    {
        Porter classPort = funPort.getPorter();
        //类参数初始化
        _PortIn clazzPIn = classPort.getPortIn();
        InNames inNames = clazzPIn.getInNames();
        oftenObject._cn = PortUtil.newArray(inNames.nece);
        oftenObject._cu = PortUtil.newArray(inNames.unece);
        oftenObject._cinner = PortUtil.newArray(inNames.inner);
        oftenObject._cInNames = inNames;


        TypeParserStore typeParserStore = context.innerContextBridge.innerBridge.globalParserStore;


        //类参数处理
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(oftenObject, clazzPIn.ignoreTypeParser(), context.innerContextBridge.paramDealt, inNames,
                        oftenObject._cn, oftenObject._cu, oftenObject.getParamSource(), typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(oftenObject, funPort, failedReason, responseWhenException);
            return;
        }

        ///////////////////////////
        //转换成类或接口对象
        try
        {
            failedReason = paramDealOfPortInEntities(context, classPort.getOftenEntities(), true,
                    classPort, funPort, oftenObject);
        } catch (Throwable e)
        {
            exNotNull(oftenObject, funPort, oftenObject.getResponse(), e, responseWhenException);
            return;
        }
        if (failedReason != null)
        {
            exParamDeal(oftenObject, funPort, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfBeforeFunParam(funPort, oftenObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                    DuringType.ON_CLASS,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeFunParam(funPort, oftenObject, context, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private final void dealtOfBeforeFunParam(PorterOfFun funPort, OftenObjectImpl oftenObject,
            Context context, UrlDecoder.Result result)
    {
        _PortIn funPIn = funPort.getMethodPortIn();

        //函数通过检测,参数没有准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfFunParam(context, oftenObject, funPort, result, false);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                    DuringType.BEFORE_METHOD,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfFunParam(context, oftenObject, funPort, result, false);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private final void dealtOfFunParam(Context context, OftenObjectImpl oftenObject, PorterOfFun funPort,
            UrlDecoder.Result result, boolean isFastInner)
    {
        _PortIn funPIn = funPort.getMethodPortIn();
        //函数参数初始化
        InNames inNames = funPIn.getInNames();
        oftenObject._fn = PortUtil.newArray(inNames.nece);
        oftenObject._fu = PortUtil.newArray(inNames.unece);
        oftenObject._finner = PortUtil.newArray(inNames.inner);
        oftenObject._fInNames = inNames;


        //函数参数处理
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(oftenObject, funPIn.ignoreTypeParser(), context.innerContextBridge.paramDealt, inNames,
                        oftenObject._fn, oftenObject._fu, oftenObject.getParamSource(),
                        context.innerContextBridge.innerBridge.globalParserStore);
        if (failedReason != null)
        {
            exParamDeal(oftenObject, funPort, failedReason, responseWhenException);
            return;
        }
        ///////////////////////////
        //转换成类或接口对象
        try
        {
            failedReason = paramDealOfPortInEntities(context, funPort.getOftenEntities(), false,
                    funPort.getPorter(), funPort, oftenObject);
        } catch (Throwable e)
        {
            exNotNull(oftenObject, funPort, oftenObject.getResponse(), e, responseWhenException);
            return;
        }
        if (failedReason != null)
        {
            exParamDeal(oftenObject, funPort, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////

        AspectHandleOfPortInUtil
                .tryDoHandle(AspectHandleOfPortInUtil.State.BeforeInvokeOfMethodCheck, oftenObject, funPort, null,
                        null);

        //函数通过检测,参数已经准备好
        if (isFastInner || funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfInvokeMethod(context, oftenObject, funPort, result, isFastInner);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                    DuringType.ON_METHOD,
                    new PortExecutorCheckers.CheckHandleAdapter(result, funPort.getFinalPorterObject(),
                            funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType())
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(oftenObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfInvokeMethod(context, oftenObject, funPort, result, false);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }

    private final Object invokeMethod(OftenObjectImpl oftenObject, PorterOfFun funPort) throws Throwable
    {
        Object returnObject;
        //调用函数
        if (funPort.getHandles() != null)
        {
            returnObject = AspectHandleOfPortInUtil
                    .doHandle(AspectHandleOfPortInUtil.State.Invoke, oftenObject, funPort, null, null);
        } else
        {
            returnObject = funPort.invokeByHandleArgs(oftenObject);
        }
        return returnObject;
    }

    private final void dealtOfInvokeMethod(Context context, OftenObjectImpl oftenObject, PorterOfFun funPort,
            UrlDecoder.Result result, boolean isFastInner)
    {

        _PortIn funPIn = funPort.getMethodPortIn();
        Object exReturnObject = null;
        try
        {
            AspectHandleOfPortInUtil
                    .tryDoHandle(AspectHandleOfPortInUtil.State.BeforeInvoke, oftenObject, funPort, null, null);
            //调用函数
            Object returnObject;
            if (context.defaultReturnFactory != null)
            {
                try
                {
                    returnObject = invokeMethod(oftenObject, funPort);
                } catch (Throwable e)
                {
                    exReturnObject = context.defaultReturnFactory
                            .getExReturn(oftenObject, funPort.getFinalPorterObject(),
                                    funPort.getObject(), funPort.getMethod(), e);
                    throw e;
                }
            } else
            {
                returnObject = invokeMethod(oftenObject, funPort);
            }

            AspectHandleOfPortInUtil
                    .tryDoHandle(AspectHandleOfPortInUtil.State.AfterInvoke, oftenObject, funPort, returnObject, null);


            OutType outType = funPort.getPortOut().getOutType();
            if (returnObject == null && context.defaultReturnFactory != null)
            {
                if (outType == OutType.VoidReturn)
                {
                    returnObject = context.defaultReturnFactory
                            .getVoidReturn(oftenObject, funPort.getFinalPorterObject(),
                                    funPort.getObject(), funPort.getMethod());
                } else if (outType == OutType.NullReturn)
                {
                    if (!funPort.getMethod().getReturnType().equals(Void.TYPE))
                    {
                        returnObject = context.defaultReturnFactory
                                .getNullReturn(oftenObject, funPort.getFinalPorterObject(),
                                        funPort.getObject(), funPort.getMethod());
                    }
                }
            }
            if (outType == OutType.SUCCESS)
            {
                returnObject = JResponse.success(null);
            }

            //调用检测

            if (isFastInner || funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                    .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
            {
                AspectHandleOfPortInUtil
                        .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, oftenObject, funPort, returnObject, null);
                dealtOfResponse(oftenObject, funPort, funPort.getPortOut().getOutType(), returnObject);
            } else
            {
                Object finalReturnObject = returnObject;
                CheckHandle checkHandle = new PortExecutorCheckers.CheckHandleAdapter(finalReturnObject, result,
                        funPort.getFinalPorterObject(),
                        funPort.getObject(),
                        funPort.getMethod(),
                        funPort.getPortOut().getOutType())
                {
                    @Override
                    public void go(Object failedObject)
                    {
                        AspectHandleOfPortInUtil
                                .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, oftenObject, funPort,
                                        finalReturnObject,
                                        failedObject);
                        if (failedObject != null)
                        {
                            exCheckPassable(oftenObject, funPort, failedObject,
                                    context.innerContextBridge.responseWhenException);
                        } else
                        {
                            dealtOfResponse(oftenObject, funPort, funPort.getPortOut().getOutType(), finalReturnObject);
                        }
                    }
                };
                PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                        DuringType.AFTER_METHOD, checkHandle,
                        funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                        funPIn.getChecks());
                portExecutorCheckers.check();
            }

        } catch (Throwable e)
        {
            Throwable ex = getCause(e);
            if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                    .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
            {
                AspectHandleOfPortInUtil.tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal,
                        oftenObject, funPort, exReturnObject, e);
                if (exReturnObject != null)
                {
                    dealtOfResponse(oftenObject, funPort, OutType.OBJECT, exReturnObject);
                } else
                {
                    exNotNull(oftenObject, funPort, oftenObject.getResponse(), ex, responseWhenException);
                }
            } else
            {
                Logger logger = logger(oftenObject);
                if (ex instanceof OftenCallException)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(ex.getMessage(), ex);
                    }
                } else if (logger.isWarnEnabled())
                {
                    logger.warn(ex.getMessage(), ex);
                }

                Object finalExReturnObject = exReturnObject;
                CheckHandle checkHandle = new PortExecutorCheckers.CheckHandleAdapter(ex, result,
                        funPort.getFinalPorterObject(),
                        funPort.getObject(),
                        funPort.getMethod(), funPort.getPortOut().getOutType())
                {
                    @Override
                    public void go(Object failedObject)
                    {
                        AspectHandleOfPortInUtil
                                .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, oftenObject, funPort,
                                        finalExReturnObject,
                                        failedObject);
                        if (failedObject != null)
                        {
                            if (!(failedObject instanceof JResponse))
                            {
                                JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                                if (failedObject instanceof Throwable)
                                {
                                    jResponse.setDescription(OftenTool.getMessage((Throwable) failedObject));
                                    jResponse.setExCause((Throwable) failedObject);
                                }
                                jResponse.setExtra(failedObject);
                                failedObject = jResponse;
                            }
                        } else if (finalExReturnObject != null)
                        {
                            failedObject = finalExReturnObject;
                        } else
                        {
                            JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                            jResponse.setDescription(OftenTool.getMessage(ex));
                            jResponse.setExCause(ex);
                            jResponse.setExtra(ex);
                            failedObject = jResponse;
                        }
                        dealtOfResponse(oftenObject, funPort, OutType.OBJECT, failedObject);
                    }
                };
                PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, oftenObject,
                        DuringType.ON_METHOD_EXCEPTION, checkHandle, funPIn.getChecks(),
                        funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass());
                portExecutorCheckers.check();
            }

        }
    }


    /**
     * 整合地址栏查询参数。
     *
     * @return
     */
    private ParamSource getParamSource(OftenObjectImpl oftenObject, Porter classPort,
            PorterOfFun funPort) throws Exception
    {
        UrlDecoder.Result result = oftenObject.url();
        Context context = oftenObject.context;
        ParamSourceHandle handle = context.paramSourceHandleManager.fromName(result.classTied());
        boolean isName = true;
        if (handle == null)
        {
            isName = false;
            //1/2.确保通过类绑定名未查找到参数源时，通过方法名查找
            handle = context.paramSourceHandleManager.fromMethod(oftenObject.getRequest().getMethod());
        }
        ParamSource ps;
        if (handle == null)
        {
            ps = new DefaultParamSource(oftenObject.getRequest());
        } else
        {
            ps = handle.get(oftenObject, classPort.getClazz(), funPort.getMethod());

            if (ps == null && isName)
            {//2/2.确保通过类绑定名未查找到参数源时，通过方法名查找
                handle = context.paramSourceHandleManager.fromMethod(oftenObject.getRequest().getMethod());
                if (handle != null)
                {
                    ps = handle.get(oftenObject, classPort.getClazz(), funPort.getMethod());
                }
            }

            if (ps == null)
            {
                ps = new DefaultParamSource(oftenObject.getRequest());
            }
        }
        ps.setUrlResult(result);
        return ps;
    }

////////////////////////////////////////////////
    //////////////////////////////////////////

    private final void dealtOfResponse(OftenObjectImpl oftenObject, PorterOfFun porterOfFun, OutType outType, Object rs)
    {
        switch (outType)
        {
            case NO_RESPONSE:
                break;
            case OBJECT:
            case SUCCESS:
                responseObject(oftenObject, porterOfFun, rs, true);
                break;
            case AUTO:
            case VoidReturn:
            case NullReturn:
                responseObject(oftenObject, porterOfFun, rs, false);
                break;
            case CLOSE:
                responseObject(oftenObject, porterOfFun, rs, true);
                break;
        }
    }


    private void responseObject(OftenObjectImpl oftenObject, PorterOfFun porterOfFun, Object object, boolean nullClose)
    {
        if (object != null)
        {
            Logger LOGGER = logger(oftenObject);

            if (object instanceof JResponse && ((JResponse) object).isNotSuccess())
            {
                JResponse jResponse = (JResponse) object;
                Throwable throwable = jResponse.getExCause();
                if(throwable!=null){
                    throwable=OftenTool.getCause(throwable);
                }
                if (throwable instanceof OftenCallException)
                {
                    JResponse jr = ((OftenCallException) throwable).theJResponse();
                    if (jr != null)
                    {
                        object = jr;
                    }
                }
                oftenObject.getResponse().toErr();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("{}:{}", oftenObject.url(), object);

                } else if (LOGGER.isInfoEnabled())
                {
                    LOGGER.info("{}:{}", oftenObject.url(), object);
                }
            }
            if (doWriteAndWillClose(oftenObject, porterOfFun, object))
            {
                close(oftenObject);
            }
        } else if (nullClose)
        {
            close(oftenObject);
        }
    }

    private final void close(OftenObject oftenObject)
    {
        OftenResponse response = oftenObject.getResponse();
        try
        {
            response.close();
        } catch (Exception e)
        {
            Logger logger = logger(oftenObject);
            if (logger.isErrorEnabled())
            {
                logger(oftenObject).error(oftenObject.url() + ":" + e.getMessage(), e);
            }
        }

    }

    private void close(OftenResponse response)
    {
        OftenTool.close(response);
    }

    private void exNotNull(@NotNull OftenObjectImpl oftenObject, PorterOfFun porterOfFun, OftenResponse response,
            Throwable throwable, boolean responseWhenException)
    {
        response.toErr();
        Logger logger = logger(oftenObject);
        if (throwable instanceof OftenCallException)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(oftenObject.url() + ":" + throwable.getMessage(), throwable);
            }
        } else if (logger.isWarnEnabled())
        {
            logger.warn(oftenObject.url() + ":" + throwable.getMessage(), throwable);
        }

        if (responseWhenException)
        {
            Object object;
            if (throwable instanceof OftenCallException)
            {
                OftenCallException oftenCallException = (OftenCallException) throwable;
                JResponse jr = oftenCallException.theJResponse();
                if (jr != null)
                {
                    object = jr;
                } else
                {
                    JSONObject json = oftenCallException.toJSON();
                    object = json.toJSONString();
                }
            } else
            {
                JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
                jResponse.setDescription(OftenTool.getMessage(throwable));
                jResponse.setExCause(throwable);
                object = jResponse;
            }

            if (doWriteAndWillClose(oftenObject, porterOfFun, object))
            {
                close(response);
            }
        } else
        {
            close(response);
        }
    }


    private final void exCheckPassable(OftenObjectImpl oftenObject, PorterOfFun porterOfFun, Object obj,
            boolean responseWhenException)
    {
        oftenObject.getResponse().toErr();
        Logger LOGGER = logger(oftenObject);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("{}:{}", oftenObject.url(), obj);
        }
        if (obj instanceof JResponse)
        {
            if (doWriteAndWillClose(oftenObject, porterOfFun, obj))
            {
                close(oftenObject);
            }
        } else if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(String.valueOf(obj));
            if (doWriteAndWillClose(oftenObject, porterOfFun, jResponse))
            {
                close(oftenObject);
            }
        } else
        {
            close(oftenObject);
        }
    }

    private JResponse toJResponse(ParamDealt.FailedReason reason, OftenObject oftenObject)
    {
        JResponse jResponse = new JResponse();
        jResponse.setCode(ResultCode.PARAM_DEAL_EXCEPTION);
        jResponse.setDescription(
                reason.desc() + "(" + oftenObject.url() + ":" + oftenObject.getRequest().getMethod() + ")");
        jResponse.setExtra(reason.toJSON());
        return jResponse;
    }

    private void exParamDeal(OftenObjectImpl oftenObject, PorterOfFun porterOfFun, ParamDealt.FailedReason reason,
            boolean responseWhenException)
    {
        Logger LOGGER = logger(oftenObject);
        JResponse jResponse = null;
        if (LOGGER.isDebugEnabled() || responseWhenException)
        {
            jResponse = toJResponse(reason, oftenObject);
            LOGGER.debug("{}:{}", oftenObject.url(), jResponse);
        }
        if (responseWhenException)
        {
            if (jResponse == null)
            {
                jResponse = toJResponse(reason, oftenObject);
            }
            if (doWriteAndWillClose(oftenObject, porterOfFun, jResponse))
            {
                close(oftenObject);
            }
        } else
        {
            close(oftenObject);
        }
    }

    private final Throwable getCause(Throwable e)
    {
        return OftenTool.getCause(e);
    }


    /**
     * 最后成功或异常的输出都调用这里。
     *
     * @param oftenObject
     * @param object
     */
    private final boolean doWriteAndWillClose(OftenObjectImpl oftenObject, PorterOfFun porterOfFun,
            @NotNull Object object)
    {
        try
        {
            ResponseHandle responseHandle = oftenObject.context.responseHandles.get(object.getClass());
            if (responseHandle == null)
            {
                responseHandle = oftenObject.context.defaultResponseHandle;
            }
            if (!oftenObject.isInnerRequest() && responseHandle != null && responseHandle
                    .hasDoneWrite(oftenObject, porterOfFun, object))
            {
                return false;
            }
            oftenObject.getResponse().write(object);
        } catch (Throwable e)
        {
            Logger logger = logger(oftenObject);
            Throwable ex = getCause(e);
            if (ex instanceof OftenCallException)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(ex.getMessage(), ex);
                }
            } else if (logger.isWarnEnabled())
            {
                logger.warn(ex.getMessage(), ex);
            }
        }
        return true;
    }

    /**
     * 最后成功或异常的输出都调用这里。
     *
     * @param response
     * @param jResponse
     */
    private final void doFinalWriteOf404(OftenRequest request, OftenResponse response, JResponse jResponse)
    {
        try
        {
            Object rs = jResponse;
            response.write(rs);
        } catch (IOException e)
        {
            Logger logger = logger(null);
            if (logger.isWarnEnabled())
            {
                Throwable ex = getCause(e);
                if (ex instanceof OftenCallException)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(ex.getMessage(), ex);
                    }
                } else if (logger.isWarnEnabled())
                {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }

}