package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._BindEntities;
import cn.xishan.oftenporter.porter.core.annotation.sth.OPEntities;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.porter.core.init.*;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;
import cn.xishan.oftenporter.porter.simple.EmptyParamSource;
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
    private PName pName;
    private DeliveryBuilder deliveryBuilder;
    private PortUtil portUtil;
    private Map<String, One> extraEntityOneMap = new HashMap<>();

    public PortExecutor(PName pName, PLinker pLinker, UrlDecoder urlDecoder, boolean responseWhenException)
    {
        _LOGGER = LogUtil.logger(PortExecutor.class);
        portUtil = new PortUtil();
        this.pName = pName;
        this.urlDecoder = urlDecoder;
        this.responseWhenException = responseWhenException;
        deliveryBuilder = DeliveryBuilder.getBuilder(true, pLinker);
    }

    public void putAllExtraEntity(Map<String, One> entityOneMap)
    {
        this.extraEntityOneMap.putAll(entityOneMap);
    }

    private final Logger logger(WObject wObject)
    {
        return wObject == null ? _LOGGER : LogUtil.logger(wObject, PortExecutor.class);
    }

    public void setAllGlobalChecks(CheckPassable[] allGlobalChecks)
    {
        this.allGlobalChecks = allGlobalChecks;
    }


    public Context addContext(PorterBridge bridge, ContextPorter contextPorter, StateListener stateListenerForAll,
            InnerContextBridge innerContextBridge, CheckPassable[] contextChecks, CheckPassable[] porterCheckPassables,
            Map<Class, ResponseHandle> responseHandles, ResponseHandle defaultResponseHandle)
    {
        PorterConf porterConf = bridge.porterConf();
        Context context = new Context(deliveryBuilder, contextPorter, contextChecks,
                bridge.paramSourceHandleManager(), stateListenerForAll, innerContextBridge,
                porterCheckPassables, porterConf.getDefaultReturnFactory(), responseHandles, defaultResponseHandle);
        context.name = bridge.contextName();
        context.contentEncoding = porterConf.getContentEncoding();
        contextMap.put(bridge.contextName(), context);
        return context;
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

    public PorterOfFun getPorterOfFun(String pathWithContextName, PortMethod method)
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

    public Iterator<String> contextNameIterator()
    {
        return contextMap.keySet().iterator();
    }

    public Iterator<Context> contextIterator()
    {
        return contextMap.values().iterator();
    }


    public PreRequest forRequest(WRequest request, final WResponse response)
    {
        String path = request.getPath();
        UrlDecoder.Result result = urlDecoder.decode(path);
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

    public WObject forPortInit(PName pName, UrlDecoder.Result result, WRequest request, WResponse response,
            Context context, boolean isInnerRequest)
    {
        WObjectImpl wObject = new WObjectImpl(pName, result, request, response, context, isInnerRequest);
        wObject.setParamSource(new EmptyParamSource());
        return wObject;
    }

    public final void doRequest(PreRequest req, WRequest request, WResponse response, boolean isInnerRequest)
    {
        WObjectImpl wObject = null;
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

            wObject = new WObjectImpl(pName, result, request, response, context, isInnerRequest);
            wObject.porterOfFun = funPort;
            wObject.portExecutor = this;

            if (funPort.getMethodPortIn().getTiedType().isRest())
            {
                wObject.restValue = result.funTied();
            }

            ParamSource paramSource = getParamSource(wObject, classPort, funPort);
            wObject.setParamSource(paramSource);

            if (isInnerRequest && funPort.isFastInner())
            {
                dealtOfFunParam(context, wObject, funPort, result, true);
            } else
            {
                //全局通过检测
                dealtOfGlobalCheck(context, wObject, funPort, result);
            }
        } catch (Exception e)
        {

            response.toErr();
            Logger LOGGER = logger(null);
            if (LOGGER.isWarnEnabled())
            {
                Throwable ex = getCause(e);
                if (ex instanceof WCallException)
                {
                    LOGGER.warn(ex.getMessage(), ex);
                } else
                {
                    LOGGER.warn(ex.getMessage(), ex);
                }
            }
            if (responseWhenException)
            {
                JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
                jResponse.setDescription(WPTool.getMessage(e));
                try
                {
                    response.write(jResponse);
                } catch (IOException e1)
                {
                    LOGGER.warn(e1.getMessage(), e1);
                }
            }
            close(response);
        }
    }

    private final void exNotFoundFun(WRequest request, WResponse response, UrlDecoder.Result result,
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

    private final void exNotFoundClassPort(WRequest request, WResponse response, boolean responseWhenException)
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


    /**
     * 用于处理对象绑定。
     *
     * @param opEntities
     * @param isInClass
     * @param wObjectImpl
     * @return
     */
    private ParamDealt.FailedReason paramDealOfPortInEntities(Context context,
            OPEntities opEntities,
            boolean isInClass, Porter porter, PorterOfFun porterOfFun, WObjectImpl wObjectImpl)
    {
        if (opEntities == null)
        {
            return null;
        }
        One[] ones = opEntities.ones;
        Object[] entities = new Object[ones.length];

        for (int i = 0; i < ones.length; i++)
        {
            Object object = paramDealOfOne(context, isInClass, porter, porterOfFun, wObjectImpl, ones[i]);
            if (object instanceof ParamDealt.FailedReason)
            {
                return (ParamDealt.FailedReason) object;
            }
            entities[i] = object;
        }
        if (isInClass)
        {
            wObjectImpl.centities = entities;
        } else
        {
            wObjectImpl.fentities = entities;
        }
        return null;
//        for (int i = 0; i < ones.length; i++)
//        {
//            One one = ones[i];
//            Object object = portUtil
//                    .paramDealOne(wObjectImpl, ignoreTypeParser, context.innerContextBridge.paramDealt, one,
//                            wObjectImpl.getParamSource(),
//                            currentTypeParserStore);
//            if (object instanceof ParamDealt.FailedReason)
//            {
//                reason = (ParamDealt.FailedReason) object;
//                break;
//            } else
//            {
//                entities[i] = object;
//            }
//        }
//        if (reason == null)
//        {
//            for (int i = 0; i < ones.length; i++)
//            {
//                One one = ones[i];
//                _BindEntities.CLASS clazz = one.getEntityClazz();
//                if (clazz != null)
//                {
//                    if (isInClass)
//                    {
//                        entities[i] = clazz.deal(wObjectImpl, porter, entities[i]);
//                    } else
//                    {
//                        entities[i] = clazz.deal(wObjectImpl, porterOfFun, entities[i]);
//                    }
//                    if (entities[i] instanceof ParamDealt.FailedReason)
//                    {
//                        reason = (ParamDealt.FailedReason) entities[i];
//                        break;
//                    }
//                }
//            }
//        }

    }

    Object getExtrwaEntity(WObjectImpl wObject, String key)
    {
        One one = extraEntityOneMap.get(key);
        if (one != null)
        {
            PorterOfFun porterOfFun = wObject.porterOfFun;
            Object object = paramDealOfOne(wObject.context, false, porterOfFun.getPorter(), porterOfFun, wObject, one);
            if (object instanceof ParamDealt.FailedReason)
            {
                ParamDealt.FailedReason failedReason = (ParamDealt.FailedReason) object;
                JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
                jResponse.setDescription(failedReason.desc());
                jResponse.setExtra(failedReason.toJSON());
                throw new WCallException(jResponse);
            }
            return object;
        }
        return null;
    }

    private Object paramDealOfOne(Context context, boolean isInClass, Porter porter, PorterOfFun porterOfFun,
            WObjectImpl wObjectImpl, One one)
    {
        TypeParserStore currentTypeParserStore = context.innerContextBridge.innerBridge.globalParserStore;
        boolean ignoreTypeParser = isInClass ? porter.getPortIn().ignoreTypeParser() : porterOfFun.getMethodPortIn()
                .ignoreTypeParser();
        Object object = portUtil.paramDealOne(wObjectImpl, ignoreTypeParser, context.innerContextBridge.paramDealt,
                one, wObjectImpl.getParamSource(), currentTypeParserStore);

        if (!(object instanceof ParamDealt.FailedReason))
        {
            _BindEntities.CLASS clazz = one.getEntityClazz();
            if (clazz != null)
            {
                if (isInClass)
                {
                    object = clazz.deal(wObjectImpl, porter, object);
                } else
                {
                    object = clazz.deal(wObjectImpl, porterOfFun, object);
                }

            }
        }
        return object;
    }

    private final void dealtOfGlobalCheck(Context context, WObjectImpl wObject, PorterOfFun funPort,
            UrlDecoder.Result result)
    {
        CheckPassable[] allGlobal = this.allGlobalChecks;

        if (allGlobal.length == 0)
        {
            dealtOfContextCheck(context, wObject, funPort, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfContextCheck(context, wObject, funPort, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfContextCheck(Context context, WObjectImpl wObject, PorterOfFun funPort,
            UrlDecoder.Result result)
    {
        CheckPassable[] contextChecks = context.contextChecks;
        if (contextChecks.length == 0)
        {
            dealtOfBeforeClassParam(funPort, wObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeClassParam(funPort, wObject, context, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfBeforeClassParam(PorterOfFun funPort, WObjectImpl wObject, Context context,
            UrlDecoder.Result result)
    {
        Porter classPort = funPort.getPorter();
        _PortIn clazzPIn = classPort.getPortIn();

        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfClassParam(funPort, wObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfClassParam(funPort, wObject, context, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }
    }

    private final void dealtOfClassParam(PorterOfFun funPort, WObjectImpl wObject, Context context,
            UrlDecoder.Result result)
    {
        Porter classPort = funPort.getPorter();
        //类参数初始化
        _PortIn clazzPIn = classPort.getPortIn();
        InNames inNames = clazzPIn.getInNames();
        wObject.cn = PortUtil.newArray(inNames.nece);
        wObject.cu = PortUtil.newArray(inNames.unece);
        wObject.cinner = PortUtil.newArray(inNames.inner);
        wObject.cInNames = inNames;


        TypeParserStore typeParserStore = context.innerContextBridge.innerBridge.globalParserStore;


        //类参数处理
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(wObject, clazzPIn.ignoreTypeParser(), context.innerContextBridge.paramDealt, inNames,
                        wObject.cn, wObject.cu, wObject.getParamSource(), typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }

        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInEntities(context, classPort.getOPEntities(), true,
                classPort, funPort, wObject);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfBeforeFunParam(funPort, wObject, context, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeFunParam(funPort, wObject, context, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private final void dealtOfBeforeFunParam(PorterOfFun funPort, WObjectImpl wObject,
            Context context, UrlDecoder.Result result)
    {
        _PortIn funPIn = funPort.getMethodPortIn();

        //函数通过检测,参数没有准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfFunParam(context, wObject, funPort, result, false);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfFunParam(context, wObject, funPort, result, false);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private final void dealtOfFunParam(Context context, WObjectImpl wObject, PorterOfFun funPort,
            UrlDecoder.Result result, boolean isFastInner)
    {
        _PortIn funPIn = funPort.getMethodPortIn();
        //函数参数初始化
        InNames inNames = funPIn.getInNames();
        wObject.fn = PortUtil.newArray(inNames.nece);
        wObject.fu = PortUtil.newArray(inNames.unece);
        wObject.finner = PortUtil.newArray(inNames.inner);
        wObject.fInNames = inNames;


        //函数参数处理
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(wObject, funPIn.ignoreTypeParser(), context.innerContextBridge.paramDealt, inNames,
                        wObject.fn,
                        wObject.fu,
                        wObject.getParamSource(),
                        context.innerContextBridge.innerBridge.globalParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }
        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInEntities(context, funPort.getOPEntities(), false,
                funPort.getPorter(), funPort, wObject);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////

        AspectHandleOfPortInUtil
                .tryDoHandle(AspectHandleOfPortInUtil.State.BeforeInvokeOfMethodCheck, wObject, funPort, null, null);

        //函数通过检测,参数已经准备好
        if (isFastInner || funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfInvokeMethod(context, wObject, funPort, result, isFastInner);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
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
                                exCheckPassable(wObject, funPort, failedObject,
                                        context.innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfInvokeMethod(context, wObject, funPort, result, false);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfInvokeMethod(Context context, WObjectImpl wObject, PorterOfFun funPort,
            UrlDecoder.Result result, boolean isFastInner)
    {

        _PortIn funPIn = funPort.getMethodPortIn();
        try
        {
            AspectHandleOfPortInUtil
                    .tryDoHandle(AspectHandleOfPortInUtil.State.BeforeInvoke, wObject, funPort, null, null);
            Object returnObject;
            //调用函数
            if (funPort.getHandles() != null)
            {
                returnObject = AspectHandleOfPortInUtil
                        .doHandle(AspectHandleOfPortInUtil.State.Invoke, wObject, funPort, null, null);
            } else
            {
                returnObject = funPort.invokeByHandleArgs(wObject);
            }


            AspectHandleOfPortInUtil
                    .tryDoHandle(AspectHandleOfPortInUtil.State.AfterInvoke, wObject, funPort, returnObject, null);


            OutType outType = funPort.getPortOut().getOutType();
            if (returnObject == null && context.defaultReturnFactory != null)
            {
                if (outType == OutType.VoidReturn)
                {
                    returnObject = context.defaultReturnFactory
                            .getVoidReturn(wObject, funPort.getFinalPorterObject(),
                                    funPort.getObject(), funPort.getMethod());
                } else if (outType == OutType.NullReturn)
                {
                    if (!funPort.getMethod().getReturnType().equals(Void.TYPE))
                    {
                        returnObject = context.defaultReturnFactory
                                .getNullReturn(wObject, funPort.getFinalPorterObject(),
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
                        .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, wObject, funPort, returnObject, null);
                dealtOfResponse(wObject, funPort, funPort.getPortOut().getOutType(), returnObject);
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
                                .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, wObject, funPort,
                                        finalReturnObject,
                                        failedObject);
                        if (failedObject != null)
                        {
                            exCheckPassable(wObject, funPort, failedObject,
                                    context.innerContextBridge.responseWhenException);
                        } else
                        {
                            dealtOfResponse(wObject, funPort, funPort.getPortOut().getOutType(), finalReturnObject);
                        }
                    }
                };
                PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
                        DuringType.AFTER_METHOD, checkHandle,
                        funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                        funPIn.getChecks());
                portExecutorCheckers.check();
            }

        } catch (Exception e)
        {
            Throwable ex = getCause(e);
            if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                    .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
            {
                AspectHandleOfPortInUtil
                        .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, wObject, funPort, null, e);
                exNotNull(wObject, funPort, wObject.getResponse(), ex, responseWhenException);
            } else
            {
                Logger logger = logger(wObject);
                if (logger.isWarnEnabled())
                {
                    if(ex instanceof WCallException){
                        logger.warn(ex.getMessage(), ex);
                    }else{
                        logger.warn(ex.getMessage(), ex);
                    }

                }

                CheckHandle checkHandle = new PortExecutorCheckers.CheckHandleAdapter(ex, result,
                        funPort.getFinalPorterObject(),
                        funPort.getObject(),
                        funPort.getMethod(), funPort.getPortOut().getOutType())
                {
                    @Override
                    public void go(Object failedObject)
                    {
                        AspectHandleOfPortInUtil
                                .tryDoHandle(AspectHandleOfPortInUtil.State.OnFinal, wObject, funPort, null,
                                        failedObject);
                        if (failedObject != null)
                        {
                            if (!(failedObject instanceof JResponse))
                            {
                                JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                                if (failedObject instanceof Throwable)
                                {
                                    jResponse.setDescription(WPTool.getMessage((Throwable) failedObject));
                                    jResponse.setExCause((Throwable) failedObject);
                                }
                                jResponse.setExtra(failedObject);
                                failedObject = jResponse;
                            }
                        } else
                        {
                            JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                            jResponse.setDescription(WPTool.getMessage(ex));
                            jResponse.setExCause(ex);
                            jResponse.setExtra(ex);
                            failedObject = jResponse;
                        }
                        dealtOfResponse(wObject, funPort, OutType.OBJECT, failedObject);

                    }
                };
                PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, funPort, wObject,
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
    private ParamSource getParamSource(WObjectImpl wObject, Porter classPort, PorterOfFun funPort) throws Exception
    {
        UrlDecoder.Result result = wObject.url();
        Context context = wObject.context;
        ParamSourceHandle handle = context.paramSourceHandleManager.fromName(result.classTied());
        boolean isName = true;
        if (handle == null)
        {
            isName = false;
            //1/2.确保通过类绑定名未查找到参数源时，通过方法名查找
            handle = context.paramSourceHandleManager.fromMethod(wObject.getRequest().getMethod());
        }
        ParamSource ps;
        if (handle == null)
        {
            ps = new DefaultParamSource(wObject.getRequest());
        } else
        {
            ps = handle.get(wObject, classPort.getClazz(), funPort.getMethod());

            if (ps == null && isName)
            {//2/2.确保通过类绑定名未查找到参数源时，通过方法名查找
                handle = context.paramSourceHandleManager.fromMethod(wObject.getRequest().getMethod());
                if (handle != null)
                {
                    ps = handle.get(wObject, classPort.getClazz(), funPort.getMethod());
                }
            }

            if (ps == null)
            {
                ps = new DefaultParamSource(wObject.getRequest());
            }
        }
        ps.setUrlResult(result);
        return ps;
    }

////////////////////////////////////////////////
    //////////////////////////////////////////

    private final void dealtOfResponse(WObjectImpl wObject, PorterOfFun porterOfFun, OutType outType, Object rs)
    {
        switch (outType)
        {
            case NO_RESPONSE:
                break;
            case OBJECT:
            case SUCCESS:
                responseObject(wObject, porterOfFun, rs, true);
                break;
            case AUTO:
            case VoidReturn:
            case NullReturn:
                responseObject(wObject, porterOfFun, rs, false);
                break;
            case CLOSE:
                responseObject(wObject, porterOfFun, rs, true);
                break;
        }
    }


    private void responseObject(WObjectImpl wObject, PorterOfFun porterOfFun, Object object, boolean nullClose)
    {
        if (object != null)
        {
            Logger LOGGER = logger(wObject);

            if (object instanceof JResponse && ((JResponse) object).isNotSuccess())
            {
                JResponse jResponse = (JResponse) object;
                Throwable throwable = jResponse.getExCause();
                if (throwable instanceof WCallException)
                {
                    JResponse exJR = ((WCallException) throwable).theJResponse();
                    if (exJR != null)
                    {
                        object = exJR;
                    }
                }
                wObject.getResponse().toErr();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("{}:{}", wObject.url(), object);

                } else if (LOGGER.isInfoEnabled())
                {
                    LOGGER.info("{}:{}", wObject.url(), object);
                }
            }
            if (doWriteAndWillClose(wObject, porterOfFun, object))
            {
                close(wObject);
            }
        } else if (nullClose)
        {
            close(wObject);
        }
    }

    private final void close(WObject wObject)
    {
        WResponse response = wObject.getResponse();
        try
        {
            response.close();
        } catch (Exception e)
        {
            Logger logger = logger(wObject);
            if (logger.isErrorEnabled())
            {
                logger(wObject).error(wObject.url() + ":" + e.getMessage(), e);
            }
        }

    }

    private void close(WResponse response)
    {
        WPTool.close(response);
    }

    private void exNotNull(@NotNull WObjectImpl wObject, PorterOfFun porterOfFun, WResponse response,
            Throwable throwable,
            boolean responseWhenException)
    {
        response.toErr();
        Logger logger = logger(wObject);
        if (logger.isWarnEnabled())
        {
            if (throwable instanceof WCallException)
            {
                logger.warn(wObject.url() + ":" + throwable.getMessage(), throwable);
            } else
            {
                logger.warn(wObject.url() + ":" + throwable.getMessage(), throwable);
            }
        }

        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(WPTool.getMessage(throwable));
            jResponse.setExCause(throwable);
            if (doWriteAndWillClose(wObject, porterOfFun, jResponse))
            {
                close(response);
            }
        } else
        {
            close(response);
        }
    }


    private final void exCheckPassable(WObjectImpl wObject, PorterOfFun porterOfFun, Object obj,
            boolean responseWhenException)
    {
        wObject.getResponse().toErr();
        Logger LOGGER = logger(wObject);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("{}:{}", wObject.url(), obj);
        }
        if (obj instanceof JResponse)
        {
            if (doWriteAndWillClose(wObject, porterOfFun, obj))
            {
                close(wObject);
            }
        } else if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(String.valueOf(obj));
            if (doWriteAndWillClose(wObject, porterOfFun, jResponse))
            {
                close(wObject);
            }
        } else
        {
            close(wObject);
        }
    }

    private JResponse toJResponse(ParamDealt.FailedReason reason, WObject wObject)
    {
        JResponse jResponse = new JResponse();
        jResponse.setCode(ResultCode.PARAM_DEAL_EXCEPTION);
        jResponse.setDescription(reason.desc() + "(" + wObject.url() + ":" + wObject.getRequest().getMethod() + ")");
        jResponse.setExtra(reason.toJSON());
        return jResponse;
    }

    private void exParamDeal(WObjectImpl wObject, PorterOfFun porterOfFun, ParamDealt.FailedReason reason,
            boolean responseWhenException)
    {
        Logger LOGGER = logger(wObject);
        JResponse jResponse = null;
        if (LOGGER.isDebugEnabled() || responseWhenException)
        {
            jResponse = toJResponse(reason, wObject);
            LOGGER.debug("{}:{}", wObject.url(), jResponse);
        }
        if (responseWhenException)
        {
            if (jResponse == null)
            {
                jResponse = toJResponse(reason, wObject);
            }
            if(doWriteAndWillClose(wObject, porterOfFun, jResponse)){
                close(wObject);
            }
        }else{
            close(wObject);
        }
    }

    private final Throwable getCause(Throwable e)
    {
        return WPTool.getCause(e);
    }


    /**
     * 最后成功或异常的输出都调用这里。
     *
     * @param wObject
     * @param object
     */
    private final boolean doWriteAndWillClose(WObjectImpl wObject, PorterOfFun porterOfFun, @NotNull Object object)
    {
        try
        {
            ResponseHandle responseHandle = wObject.context.responseHandles.get(object.getClass());
            if (responseHandle == null)
            {
                responseHandle = wObject.context.defaultResponseHandle;
            }
            if (responseHandle != null && responseHandle.hasDoneWrite(wObject, porterOfFun, object))
            {
                return false;
            }
            wObject.getResponse().write(object);
        } catch (Throwable e)
        {
            Logger logger = logger(wObject);
            if (logger.isWarnEnabled())
            {
                Throwable ex = getCause(e);
                if (ex instanceof WCallException)
                {
                    logger.warn(ex.getMessage(), ex);
                } else
                {
                    logger.warn(ex.getMessage(), ex);
                }
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
    private final void doFinalWriteOf404(WRequest request, WResponse response, JResponse jResponse)
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
                if (ex instanceof WCallException)
                {
                    logger.warn(ex.getMessage(), ex);
                } else
                {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }

}