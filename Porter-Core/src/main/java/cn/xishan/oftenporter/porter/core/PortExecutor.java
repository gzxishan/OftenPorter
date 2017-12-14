package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.InObj;
import cn.xishan.oftenporter.porter.core.annotation.sth.One;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
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
    private ResponseHandle responseHandle;
    private IAttributeFactory iAttributeFactory, defaultIAttributeFactory;

    public PortExecutor(ResponseHandle responseHandle, PName pName, PLinker pLinker, UrlDecoder urlDecoder,
            boolean responseWhenException)
    {
        _LOGGER = LogUtil.logger(PortExecutor.class);
        this.responseHandle = responseHandle;
        portUtil = new PortUtil();
        this.pName = pName;
        this.urlDecoder = urlDecoder;
        this.responseWhenException = responseWhenException;
        deliveryBuilder = DeliveryBuilder.getBuilder(true, pLinker);
        defaultIAttributeFactory = wObject -> new IAttribute()
        {
            HashMap<String, Object> attrsMap = new HashMap<>();

            @Override
            public IAttribute setAttribute(String key, Object value)
            {
                attrsMap.put(key, value);
                return this;
            }

            @Override
            public <T> T getAttribute(String key)
            {
                Object obj = attrsMap.get(key);
                return (T) obj;
            }

            @Override
            public <T> T removeAttribute(String key)
            {
                Object obj = attrsMap.remove(key);
                return (T) obj;
            }
        };
        iAttributeFactory = defaultIAttributeFactory;
    }

    public void setIAttributeFactory(IAttributeFactory iAttributeFactory)
    {
        this.iAttributeFactory = iAttributeFactory;
    }

    private Logger logger(WObject wObject)
    {
        return wObject == null ? _LOGGER : LogUtil.logger(wObject, PortExecutor.class);
    }

    public void initAllGlobalChecks(CheckPassable[] allGlobalChecks)
    {
        this.allGlobalChecks = allGlobalChecks;
    }


    public Context addContext(PorterBridge bridge, ContextPorter contextPorter, StateListener stateListenerForAll,
            InnerContextBridge innerContextBridge, CheckPassable[] contextChecks, CheckPassable[] porterCheckPassables)
    {
        PorterConf porterConf = bridge.porterConf();
        Context context = new Context(deliveryBuilder, contextPorter, contextChecks,
                bridge.paramSourceHandleManager(), stateListenerForAll, innerContextBridge,
                porterCheckPassables, porterConf.getDefaultReturnFactory());
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
            Context context)
    {
        WObjectImpl wObject = new WObjectImpl(pName, result, request, response, context);
        wObject.setIAttribute(defaultIAttributeFactory);
        wObject.setParamSource(new EmptyParamSource());
        return wObject;
    }

    public void doRequest(PreRequest req, WRequest request, WResponse response, boolean isInnerRequest)
    {
        try
        {

            PorterOfFun funPort = req.funPort;
            Porter classPort = req.classPort;

            Context context = req.context;
            InnerContextBridge innerContextBridge = context.innerContextBridge;
            UrlDecoder.Result result = req.result;


            if (!isInnerRequest && funPort.getMethodPortIn()
                    .getPortFunType() == PortFunType.INNER)
            {
                exNotFoundClassPort(request, response, innerContextBridge.responseWhenException);
                return;
            }


            WObjectImpl wObject = new WObjectImpl(pName, result, request, response, context);
            IAttributeFactory attributeFactory = iAttributeFactory;
            Object originRequest = request.getOriginalRequest();
            if (originRequest != null && originRequest instanceof IAttributeFactory)
            {
                attributeFactory = (IAttributeFactory) originRequest;
            }
            wObject.setIAttribute(attributeFactory);

            if (funPort.getMethodPortIn().getTiedType().isRest())
            {
                wObject.restValue = result.funTied();
            }

            ParamSource paramSource = getParamSource(wObject, classPort, funPort);
            wObject.setParamSource(paramSource);
            //全局通过检测
            dealtOfGlobalCheck(context, funPort, wObject, innerContextBridge, result);
        } catch (Exception e)
        {
            Throwable ex = getCause(e);
            response.toErr();
            Logger LOGGER = logger(null);
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(ex.getMessage(), ex);
            }
            if (responseWhenException)
            {
                JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
                jResponse.setDescription(WPTool.getMessage(ex));
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

    private void exNotFoundFun(WRequest request, WResponse response, UrlDecoder.Result result,
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

    private void exNotFoundClassPort(WRequest request, WResponse response, boolean responseWhenException)
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
                    .paramDealOne(wObjectImpl, ignoreTypeParser, context.innerContextBridge.paramDealt, one,
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
                                        innerContextBridge.responseWhenException);
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
            dealtOfBeforeClassParam(funPort, wObject, context, innerContextBridge, result);
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
                                        innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeClassParam(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    });
            portExecutorCheckers.check();
        }

    }

    private final void dealtOfBeforeClassParam(PorterOfFun funPort, WObjectImpl wObject, Context context,
            InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        Porter classPort = funPort.getPorter();
        _PortIn clazzPIn = classPort.getPortIn();

        //类通过检测
        if (clazzPIn.getChecks().length == 0 && classPort.getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfClassParam(funPort, wObject, context, innerContextBridge, result);
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
                                        innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfClassParam(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
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
                .paramDeal(wObject, clazzPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.cn,
                        wObject.cu,
                        wObject.getParamSource(),
                        typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }

        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(clazzPIn.ignoreTypeParser(), context, classPort.getInObj(), true, wObject,
                typeParserStore);
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
            dealtOfBeforeFunParam(funPort, wObject, context, innerContextBridge, result);
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
                                        innerContextBridge.responseWhenException);
                            } else
                            {
                                dealtOfBeforeFunParam(funPort, wObject, context, innerContextBridge, result);
                            }
                        }
                    }, classPort.getWholeClassCheckPassableGetter().getChecksForWholeClass(), clazzPIn.getChecks());
            portExecutorCheckers.check();
        }

    }


    private void dealtOfBeforeFunParam(PorterOfFun funPort, WObjectImpl wObject,
            Context context, InnerContextBridge innerContextBridge,
            UrlDecoder.Result result)
    {
        _PortIn funPIn = funPort.getMethodPortIn();

        //函数通过检测,参数没有准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfFunParam(funPort, wObject, context, innerContextBridge, result);
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
                                        innerContextBridge.responseWhenException);
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
                .paramDeal(wObject, funPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.fn,
                        wObject.fu,
                        wObject.getParamSource(),
                        typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }
        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(funPIn.ignoreTypeParser(), context, funPort.getInObj(), false, wObject,
                typeParserStore);
        if (failedReason != null)
        {
            exParamDeal(wObject, funPort, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////

        AspectHandleUtil.tryDoHandle(AspectHandleUtil.State.BeforeInvokeOfMethodCheck, wObject, funPort, null, null);

        //函数通过检测,参数已经准备好
        if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
        {
            dealtOfInvokeMethod(context, wObject, funPort, innerContextBridge, result);
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
                                        innerContextBridge.responseWhenException);
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

        _PortIn funPIn = funPort.getMethodPortIn();
        try
        {
            AspectHandleUtil.tryDoHandle(AspectHandleUtil.State.BeforeInvoke, wObject, funPort, null, null);
            Object returnObject;
            //调用函数
            if (funPort.getHandles() != null)
            {
                returnObject = AspectHandleUtil.doHandle(AspectHandleUtil.State.Invoke, wObject, funPort, null, null);
            } else
            {
                returnObject = funPort.invoke(wObject, null);
            }


            AspectHandleUtil.tryDoHandle(AspectHandleUtil.State.AfterInvoke, wObject, funPort, returnObject, null);


            OutType outType = funPort.getPortOut().getOutType();
            if (returnObject == null && context.defaultReturnFactory != null)
            {
                if (outType == OutType.VoidReturn)
                {
                    if (funPort.getMethod().getReturnType().equals(Void.TYPE))
                    {
                        returnObject = context.defaultReturnFactory
                                .getVoidReturn(wObject, funPort.getFinalPorterObject(),
                                        funPort.getObject(), funPort.getMethod());
                    }
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

            if (funPIn.getChecks().length == 0 && funPort.getPorter().getWholeClassCheckPassableGetter()
                    .getChecksForWholeClass().length == 0 && context.porterCheckPassables == null)
            {
                AspectHandleUtil
                        .tryDoHandle(AspectHandleUtil.State.OnFinal, wObject, funPort, returnObject, null);
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
                        AspectHandleUtil
                                .tryDoHandle(AspectHandleUtil.State.OnFinal, wObject, funPort, finalReturnObject,
                                        failedObject);
                        if (failedObject != null)
                        {
                            exCheckPassable(wObject, funPort, failedObject,
                                    innerContextBridge.responseWhenException);
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
                AspectHandleUtil
                        .tryDoHandle(AspectHandleUtil.State.OnFinal, wObject, funPort, null, e);
                exNotNull(wObject, funPort, wObject.getResponse(), ex, responseWhenException);
            } else
            {
                logger(wObject).warn(ex.getMessage(), ex);
                CheckHandle checkHandle = new PortExecutorCheckers.CheckHandleAdapter(ex, result,
                        funPort.getFinalPorterObject(),
                        funPort.getObject(),
                        funPort.getMethod(), funPort.getPortOut().getOutType())
                {
                    @Override
                    public void go(Object failedObject)
                    {
                        AspectHandleUtil
                                .tryDoHandle(AspectHandleUtil.State.OnFinal, wObject, funPort, null, failedObject);
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
                        } else
                        {
                            JResponse jResponse = new JResponse(ResultCode.INVOKE_METHOD_EXCEPTION);
                            jResponse.setDescription(WPTool.getMessage(ex));
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

    private void dealtOfResponse(WObjectImpl wObject, PorterOfFun porterOfFun, OutType outType, Object rs)
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


    private void responseObject(WObject wObject, PorterOfFun porterOfFun, Object object, boolean nullClose)
    {
        if (object != null)
        {
            Logger LOGGER = logger(wObject);

            if (object != null && object instanceof JResponse && ((JResponse) object).isNotSuccess())
            {
                wObject.getResponse().toErr();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("{}:{}", wObject.url(), object);

                } else if (LOGGER.isInfoEnabled())
                {
                    LOGGER.info("{}:{}", wObject.url(), object);
                }
            }
            doFinalWrite(wObject, porterOfFun, object);
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

    private void exNotNull(@NotNull WObject wObject, PorterOfFun porterOfFun, WResponse response, Throwable throwable,
            boolean responseWhenException)
    {
        response.toErr();
        Logger LOGGER = logger(wObject);
        if (LOGGER.isWarnEnabled())
        {
            LOGGER.warn((wObject.url() + ":") + throwable.getMessage(), throwable);
        }
        if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(WPTool.getMessage(throwable));
            doFinalWrite(wObject, porterOfFun, jResponse);
        }
        close(response);
    }


    private void exCheckPassable(WObject wObject, PorterOfFun porterOfFun, Object obj, boolean responseWhenException)
    {
        wObject.getResponse().toErr();
        Logger LOGGER = logger(wObject);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("{}:{}", wObject.url(), obj);
        }
        if (obj instanceof JResponse)
        {
            doFinalWrite(wObject, porterOfFun, obj);
        } else if (responseWhenException)
        {
            JResponse jResponse = new JResponse(ResultCode.ACCESS_DENIED);
            jResponse.setDescription(String.valueOf(obj));
            doFinalWrite(wObject, porterOfFun, jResponse);
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

    private void exParamDeal(WObject wObject, PorterOfFun porterOfFun, ParamDealt.FailedReason reason,
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
            doFinalWrite(wObject, porterOfFun, jResponse);
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


    /**
     * 最后成功或异常的输出都调用这里。
     *
     * @param wObject
     * @param object
     */
    private final void doFinalWrite(WObject wObject, PorterOfFun porterOfFun, Object object)
    {
        try
        {
            if (object != null && responseHandle != null)
            {
                Porter porter = porterOfFun.getPorter();
                object = responseHandle
                        .toResponse(wObject, porter.getFinalPorterObject(), porter.getObj(), porterOfFun.getMethod(),
                                object);
            }
            wObject.getResponse().write(object);
        } catch (IOException e)
        {
            Throwable ex = getCause(e);
            Logger LOGGER = logger(wObject);
            LOGGER.warn(ex.getMessage(), ex);
        }
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
            if (responseHandle != null)
            {
                rs = responseHandle.toResponseOf404(request, response, jResponse);
            }
            response.write(rs);
        } catch (IOException e)
        {
            Throwable ex = getCause(e);
            Logger LOGGER = logger(null);
            LOGGER.warn(ex.getMessage(), ex);
        }
    }

}