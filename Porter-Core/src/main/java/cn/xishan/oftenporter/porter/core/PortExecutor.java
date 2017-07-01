package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.InObj;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.init.PorterBridge;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamsSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PortExecutor
{


    private final Logger _LOGGER;
    private Map<String, Context> contextMap = new ConcurrentHashMap<>();

    private CheckPassable[] allGlobalChecks;
    private UrlDecoder urlDecoder;
    private boolean responseWhenException;
    private PName pName;
    private DeliveryBuilder deliveryBuilder;
    private PortUtil portUtil;

    public PortExecutor(PName pName, PLinker pLinker, UrlDecoder urlDecoder,
            boolean responseWhenException)
    {
        _LOGGER = LogUtil.logger(PortExecutor.class);
        portUtil = new PortUtil();
        this.pName = pName;
        this.urlDecoder = urlDecoder;
        this.responseWhenException = responseWhenException;
        deliveryBuilder = DeliveryBuilder.getBuilder(true, pLinker);
    }

    private Logger logger(WObject wObject)
    {
        return wObject == null ? _LOGGER : LogUtil.logger(wObject, PortExecutor.class);
    }

    public void initAllGlobalChecks(CheckPassable[] allGlobalChecks)
    {
        this.allGlobalChecks = allGlobalChecks;
    }


    public void addContext(PorterBridge bridge, ContextPorter contextPorter, StateListener stateListenerForAll,
            InnerContextBridge innerContextBridge, CheckPassable[] contextChecks, CheckPassable[] forAllCheckPassables)
    {
        PorterConf porterConf = bridge.porterConf();
        Context context = new Context(deliveryBuilder, contextPorter, contextChecks,
                bridge.paramSourceHandleManager(), stateListenerForAll, innerContextBridge, forAllCheckPassables);
        context.name = bridge.contextName();
        context.contentEncoding = porterConf.getContentEncoding();
        contextMap.put(bridge.contextName(), context);
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
            return new PreRequest(context, result);
        }
    }

    public void doRequest(PreRequest req, WRequest request, WResponse response)
    {
        try
        {
            Context context = req.context;
            UrlDecoder.Result result = req.result;

            Porter classPort = context.contextPorter.getClassPort(result.classTied());

            PorterOfFun funPort;

            InnerContextBridge innerContextBridge = context.innerContextBridge;

            if (classPort == null)
            {
                exNotFoundClassPort(request, response, innerContextBridge.responseWhenException);
                return;
            } else if ((funPort = classPort.getChild(result, request.getMethod())) == null)
            {
                exNotFoundFun(response, result, innerContextBridge.responseWhenException);
                return;
            }

            ABOption abOption = null;
            if (request instanceof PRequest)
            {
                PRequest pRequest = (PRequest) request;
                abOption = pRequest._getABOption_();
                if (funPort.getMethodPortIn()
                        .getPortFunType() == PortFunType.INNER && (abOption == null && abOption.portFunType !=
                        PortFunType.INNER))
                {
                    exNotFoundClassPort(request, response, innerContextBridge.responseWhenException);
                    return;
                }
            }


            WObjectImpl wObject = new WObjectImpl(pName, result, request, response, context);

            if (funPort.getMethodPortIn().getTiedType() == TiedType.REST || funPort.getMethodPortIn()
                    .getTiedType() == TiedType.FORCE_REST)
            {
                wObject.restValue = result.funTied();
            }

            if (abOption != null)
            {
                wObject._otherObject = abOption._otherObject;
                if (abOption.abInvokeOrder == ABInvokeOrder._OTHER_BEFORE)
                {
                    if (funPort.getPortBefores().length == 0)
                    {
                        abOption = abOption.clone(ABInvokeOrder.ORIGIN_FIRST);
                    } else
                    {
                        abOption = abOption.clone(ABInvokeOrder.OTHER);
                    }
                } else if (abOption.abInvokeOrder == ABInvokeOrder._OTHER_AFTER)
                {
                    if (funPort.getPortAfters().length == 0)
                    {
                        abOption = abOption.clone(ABInvokeOrder.FINAL_LAST);
                    } else
                    {
                        abOption = abOption.clone(ABInvokeOrder.OTHER);
                    }
                }
            } else
            {
                //*********
                //**初始情况***
                //*********
                ABInvokeOrder abInvokeOrder;
                if (funPort.getPortBefores().length == 0 && funPort.getPortAfters().length == 0)
                {
                    abInvokeOrder = ABInvokeOrder.BOTH_FIRST_LAST;
                } else if (funPort.getPortBefores().length == 0)
                {
                    abInvokeOrder = ABInvokeOrder.ORIGIN_FIRST;
                } else if (funPort.getPortAfters().length == 0)
                {
                    abInvokeOrder = ABInvokeOrder.FINAL_LAST;
                } else
                {
                    abInvokeOrder = ABInvokeOrder.OTHER;
                }
                abOption = new ABOption(null, PortFunType.DEFAULT, abInvokeOrder);
            }
            wObject.abOption = abOption;


            ParamSource paramSource = getParamSource(wObject, classPort, funPort);
            wObject.setParamSource(paramSource);
            //全局通过检测
            dealtOfGlobalCheck(context, funPort, wObject, innerContextBridge, result);
        } catch (Exception e)
        {
            Throwable ex = getCause(e);
            ex(null, response, ex, responseWhenException);
        }
    }

    private void exNotFoundFun(WResponse response, UrlDecoder.Result result, boolean responseWhenException)
    {
        response.toErr();
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("fun:" + result.toString());
            try
            {
                response.write(jResponse);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                logger(null).warn(ex.getMessage(), ex);
            }
        }
        close(response);
    }

    private void exNotFoundClassPort(WRequest request, WResponse response, boolean responseWhenException)
    {
        response.toErr();
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("method=" + request.getMethod().name() + ",path=" + request.getPath());
            try
            {
                response.write(jResponse);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                logger(null).warn(ex.getMessage(), ex);
            }
        }
        close(response);
    }


    /**
     * 用于处理对象绑定。
     *
     * @param inObj
     * @param isInClass
     * @param wObjectImpl
     * @param currentTypeParserStore
     * @return
     */
    private ParamDealt.FailedReason paramDealOfPortInObj(boolean ignoreTypeParser, Context context, InObj inObj,
            boolean isInClass,
            WObjectImpl wObjectImpl, TypeParserStore currentTypeParserStore)
    {
        ParamDealt.FailedReason reason = null;
        if (inObj == null)
        {
            return null;
        }
        One[] ones = inObj.ones;
        Object[] inObjects = new Object[ones.length];
        if (isInClass)
        {
            wObjectImpl.cinObjs = inObjects;
        } else
        {
            wObjectImpl.finObjs = inObjects;
        }
        for (int i = 0; i < ones.length; i++)
        {
            One one = ones[i];
            Object object = portUtil
                    .paramDealOne(ignoreTypeParser, context.innerContextBridge.paramDealt, one,
                            wObjectImpl.getParamSource(),
                            currentTypeParserStore);
            if (object instanceof ParamDealt.FailedReason)
            {
                return (ParamDealt.FailedReason) object;
            } else
            {
                inObjects[i] = object;
            }
        }

        return reason;
    }

    private final void dealtOfGlobalCheck(Context context, PorterOfFun funPort, WObjectImpl wObject,
            InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        CheckPassable[] allGlobal = this.allGlobalChecks;

        if (allGlobal.length == 0)
        {
            dealtOfContextCheck(context, funPort, wObject, innerContextBridge, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, wObject, DuringType.ON_GLOBAL,
                    allGlobal,
                    new CheckHandle(result, funPort.getFinalPorterObject(), funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType(),
                            wObject.abOption)
                    {

                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfContextCheck(context, funPort, wObject, innerContextBridge, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfContextCheck(Context context, PorterOfFun funPort,
            WObjectImpl wObject, InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        CheckPassable[] contextChecks = context.contextChecks;
        if (contextChecks.length == 0)
        {
            dealtOfClassParam(funPort, wObject, context, innerContextBridge, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(null, wObject,
                    DuringType.ON_CONTEXT, contextChecks,
                    new CheckHandle(result, funPort.getFinalPorterObject(), funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType(),
                            wObject.abOption)
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfClassParam(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfClassParam(PorterOfFun funPort, WObjectImpl wObject, Context context,
            InnerContextBridge innerContextBridge,
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


        TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;


        //类参数处理
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(clazzPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.cn, wObject.cu,
                        wObject.getParamSource(),
                        typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }

        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(clazzPIn.ignoreTypeParser(), context, classPort.getInObj(), true, wObject,
                typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.forAllChecksNotZeroLen == null)
        {
            doBefore_beforeFunParamsDealt(funPort, wObject, context, innerContextBridge, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, wObject, DuringType.ON_CLASS,
                    new CheckHandle(result, funPort.getFinalPorterObject(), funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType(), wObject.abOption)
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                doBefore_beforeFunParamsDealt(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }

    }

    private void doBefore_beforeFunParamsDealt(PorterOfFun funPort, WObjectImpl wObject,
            Context context, InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        if (funPort.getPortBefores().length > 0)
        {
            PortBeforeAfterDealt portBeforeAfterDealt = new PortBeforeAfterDealt(wObject, funPort);
            portBeforeAfterDealt.startBefore((isOked, object) ->
            {
                if (isOked)
                {
                    dealtOfBeforeFunParam(funPort, wObject, context, innerContextBridge, result);
                } else
                {
                    responseObject(wObject, object, true);
                }
            });
            return;
        } else
        {
            dealtOfBeforeFunParam(funPort, wObject, context, innerContextBridge, result);
        }
    }

    private void dealtOfBeforeFunParam(PorterOfFun funPort, WObjectImpl wObject,
            Context context, InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        _PortIn funPIn = funPort.getMethodPortIn();

        //函数通过检测,参数没有准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.forAllChecksNotZeroLen == null)
        {
            dealtOfFunParam(funPort, wObject, context, innerContextBridge, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, wObject,
                    DuringType.BEFORE_METHOD,
                    new CheckHandle(result, funPort.getFinalPorterObject(), funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType(),
                            wObject.abOption)
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfFunParam(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private void dealtOfFunParam(PorterOfFun funPort, WObjectImpl wObject,
            Context context, InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        _PortIn funPIn = funPort.getMethodPortIn();
        //函数参数初始化
        InNames inNames = funPIn.getInNames();
        wObject.fn = PortUtil.newArray(inNames.nece);
        wObject.fu = PortUtil.newArray(inNames.unece);
        wObject.finner = PortUtil.newArray(inNames.inner);
        wObject.fInNames = inNames;


        //函数参数处理
        TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;
        ParamDealt.FailedReason failedReason = portUtil
                .paramDeal(funPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.fn, wObject.fu,
                        wObject.getParamSource(),
                        typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(funPIn.ignoreTypeParser(), context, funPort.getInObj(), false, wObject,
                typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //函数通过检测,参数已经准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.forAllChecksNotZeroLen == null)
        {
            dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result);
        } else
        {
            PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, wObject, DuringType.ON_METHOD,
                    new CheckHandle(result, funPort.getFinalPorterObject(), funPort.getObject(), funPort.getMethod(),
                            funPort.getPortOut().getOutType(),
                            wObject.abOption)
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result);
                            }
                        }
                    }, funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                    funPIn.getChecks());
            portExecutorCheckers.check();
        }

    }

    private void dealtOfInvokeMethod(Context context, WObjectImpl wObject, PorterOfFun funPort,
            InnerContextBridge innerContextBridge, UrlDecoder.Result result)
    {
        dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result,
                PortBeforeAfterDealt.DoState.DoInvoke, true, null);
    }


    private void dealtOfInvokeMethod(Context context, WObjectImpl wObject, PorterOfFun funPort,
            InnerContextBridge innerContextBridge, UrlDecoder.Result result, PortBeforeAfterDealt.DoState doState,
            boolean isOk, Object returnObject)
    {
        Method javaMethod = funPort.getMethod();
        _PortIn funPIn = funPort.getMethodPortIn();
        try
        {
            if (!isOk)
            {
                wObject.getResponse().toErr();
                responseObject(wObject, returnObject, true);
                return;
            }


            if (doState == PortBeforeAfterDealt.DoState.DoInvoke)
            {
                if (funPort.getArgCount() == 0)
                {
                    returnObject = javaMethod.invoke(funPort.getObject());
                } else
                {
                    returnObject = javaMethod.invoke(funPort.getObject(), wObject);
                }
                doState = PortBeforeAfterDealt.DoState.DoMethodCheck;
            }

            if (doState == PortBeforeAfterDealt.DoState.DoMethodCheck)
            {
                if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                        .getChecksForWholeClass().length == 0 && context.forAllChecksNotZeroLen == null)
                {
                    doState = PortBeforeAfterDealt.DoState.DoAfter;
                } else
                {
                    Object finalReturnObject = returnObject;
                    CheckHandle checkHandle = new CheckHandle(finalReturnObject, result, funPort.getFinalPorterObject(),
                            funPort.getObject(),
                            funPort.getMethod(),
                            funPort.getPortOut().getOutType(), wObject.abOption)
                    {
                        @Override
                        public void go(Object failedObject)
                        {
                            if (failedObject != null)
                            {
                                exCheckPassable(wObject, failedObject, innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result,
                                        PortBeforeAfterDealt.DoState.DoAfter, true, finalReturnObject);
                            }
                        }
                    };
                    PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, wObject,
                            DuringType.AFTER_METHOD, checkHandle,
                            funPort.getPorter().getWholeClassCheckPassableGetter().getChecksForWholeClass(),
                            funPIn.getChecks());
                    portExecutorCheckers.check();
                    return;
                }
            }

            if (doState == PortBeforeAfterDealt.DoState.DoAfter && funPort.getPortAfters().length > 0)
            {
                PortBeforeAfterDealt portBeforeAfterDealt = new PortBeforeAfterDealt(wObject, funPort);
                portBeforeAfterDealt.startAfter((isOked, object) ->
                {
                    dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result,
                            isOked ? PortBeforeAfterDealt.DoState.DoResponse : null, isOked, object);
                });
                return;
            } else
            {
                doState = PortBeforeAfterDealt.DoState.DoResponse;
            }

            if (doState == PortBeforeAfterDealt.DoState.DoResponse)
            {
                dealtOfResponse(wObject, funPort.getPortOut().getOutType(), returnObject);
            }

        } catch (Exception e)
        {
            Throwable ex = getCause(e);
            if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                    .getChecksForWholeClass().length == 0 && context.forAllChecksNotZeroLen == null)
            {
                ex(wObject, wObject.getResponse(), ex, responseWhenException);
            } else
            {
                logger(wObject).warn(ex.getMessage(), ex);
                CheckHandle checkHandle = new CheckHandle(ex, result, funPort.getFinalPorterObject(),
                        funPort.getObject(),
                        funPort.getMethod(), funPort.getPortOut().getOutType(), wObject.abOption)
                {
                    @Override
                    public void go(Object failedObject)
                    {
                        if (failedObject != null)
                        {
                            if (!(failedObject instanceof JResponse))
                            {
                                JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                                if (failedObject instanceof Throwable)
                                {
                                    jResponse.setDescription(WPTool.getMessage((Throwable) failedObject));
                                }
                                jResponse.setExtra(failedObject);
                                failedObject = jResponse;
                            }
                            dealtOfResponse(wObject, OutType.OBJECT, failedObject);
                        } else
                        {
                            JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                            jResponse.setDescription(WPTool.getMessage(ex));
                            jResponse.setExtra(ex);
                            dealtOfResponse(wObject, OutType.OBJECT, jResponse);
                        }
                    }
                };
                PortExecutorCheckers portExecutorCheckers = new PortExecutorCheckers(context, wObject,
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
        if (handle == null)
        {
            handle = context.paramSourceHandleManager.fromMethod(wObject.getRequest().getMethod());
        }
        ParamSource ps;
        if (handle == null)
        {
            ps = new DefaultParamsSource(result, wObject.getRequest());
        } else
        {
            ps = handle.get(wObject, classPort.getClazz(), funPort.getMethod());
            if (ps == null)
            {
                ps = new DefaultParamsSource(result, wObject.getRequest());
            }
        }
        return ps;
    }

////////////////////////////////////////////////
    //////////////////////////////////////////

    private void dealtOfResponse(WObjectImpl wObject, OutType outType, Object rs)
    {
        switch (outType)
        {
            case NO_RESPONSE:
                break;
            case OBJECT:
                responseObject(wObject, rs, true);
                break;
            case AUTO:
                responseObject(wObject, rs, false);
                break;
        }
    }


    private void responseObject(WObject wObject, Object object, boolean nullClose)
    {
        if (object != null)
        {
            Logger LOGGER = logger(wObject);
            try
            {
                if (object != null && object instanceof JResponse && ((JResponse) object).isNotSuccess())
                {
                    wObject.getResponse().toErr();
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("{}", object);

                    } else if (LOGGER.isInfoEnabled())
                    {
                        LOGGER.info("{}", object);
                    }
                }
                wObject.getResponse().write(object);
            } catch (IOException e)
            {
                wObject.getResponse().toErr();
                Throwable ex = getCause(e);
                LOGGER.warn(ex.getMessage(), ex);
            }
            close(wObject);
        } else if (nullClose)
        {
            close(wObject);
        }
    }

    private final void close(WObject wObject)
    {
        WPTool.close(wObject.getResponse());
    }

    private void close(WResponse response)
    {
        WPTool.close(response);
    }

    private void ex(@MayNull WObject wObject, WResponse response, Throwable throwable, boolean responseWhenException)
    {
        response.toErr();
        Logger LOGGER = logger(wObject);
        if (LOGGER.isWarnEnabled())
        {
            LOGGER.warn(throwable.getMessage(), throwable);
        }
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(WPTool.getMessage(throwable));
            try
            {
                response.write(jResponse);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                LOGGER.warn(ex.getMessage(), ex);
            }
        }
        close(response);
    }


    private void exCheckPassable(WObject wObject, Object obj, boolean responseWhenException)
    {
        wObject.getResponse().toErr();
        Logger LOGGER = logger(wObject);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("{}", obj);
        }
        if (obj instanceof JResponse)
        {
            try
            {
                wObject.getResponse().write(obj);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                LOGGER.warn(ex.getMessage(), ex);
            }
        } else if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.ACCESS_DENIED);
            jResponse.setDescription(String.valueOf(obj));
            try
            {
                wObject.getResponse().write(jResponse);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                LOGGER.warn(ex.getMessage(), ex);
            }
        }
        close(wObject);
    }

    private JResponse toJResponse(ParamDealt.FailedReason reason, WObject wObject)
    {
        JResponse jResponse = new JResponse();
        jResponse.setCode(ResultCode.PARAM_DEAL_EXCEPTION);
        jResponse.setDescription(reason.desc() + "(" + wObject.url() + ":" + wObject.getRequest().getMethod() + ")");
        jResponse.setExtra(reason.toJSON());
        return jResponse;
    }

    private void exParamDeal(WObject wObject, ParamDealt.FailedReason reason, boolean responseWhenException)
    {
        Logger LOGGER = logger(wObject);
        JResponse jResponse = null;
        if (LOGGER.isDebugEnabled() || responseWhenException)
        {
            jResponse = toJResponse(reason, wObject);
            LOGGER.debug("{}", jResponse);
        }
        if (responseWhenException)
        {
            if (jResponse == null)
            {
                jResponse = toJResponse(reason, wObject);
            }
            try
            {
                wObject.getResponse().write(jResponse);
            } catch (IOException e)
            {
                Throwable ex = getCause(e);
                LOGGER.warn(ex.getMessage(), ex);
            }
        }
        close(wObject);
    }

    private final Throwable getCause(Throwable e)
    {
        Throwable cause = e.getCause();
        if (cause == null)
        {
            cause = e;
        }
        return cause;
    }

}