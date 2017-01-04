package cn.xishan.oftenporter.porter.core;

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
import cn.xishan.oftenporter.porter.core.util.WPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public class PortExecutor {


    private static final Logger LOGGER = LoggerFactory.getLogger(PortExecutor.class);
    private Map<String, Context> contextMap = new ConcurrentHashMap<>();

    private CheckPassable[] allGlobalChecks;
    private UrlDecoder urlDecoder;
    private boolean responseWhenException;
    private PName pName;
    private DeliveryBuilder deliveryBuilder;

    public PortExecutor(PName pName, Delivery delivery, UrlDecoder urlDecoder,
                        boolean responseWhenException) {
        this.pName = pName;
        this.urlDecoder = urlDecoder;
        this.responseWhenException = responseWhenException;
        deliveryBuilder = DeliveryBuilder.getBuilder(true, delivery);
    }

    public void initAllGlobalChecks(CheckPassable[] allGlobalChecks) {
        this.allGlobalChecks = allGlobalChecks;
    }

    public void addContext(PorterBridge bridge, PortContext portContext, StateListener stateListenerForAll,
                           InnerContextBridge innerContextBridge) {
        PorterConf porterConf = bridge.porterConf();
        Context context = new Context(deliveryBuilder, portContext,
                                      porterConf.getContextChecks().toArray(new CheckPassable[0]),
                                      bridge.paramSourceHandleManager(), stateListenerForAll, innerContextBridge);
        context.name = bridge.contextName();
        context.contentEncoding = porterConf.getContentEncoding();
        contextMap.put(bridge.contextName(), context);
    }

    public UrlDecoder getUrlDecoder() {
        return urlDecoder;
    }

    /**
     * 移除指定的context
     *
     * @param contextName context名称
     * @return 返回移除的Context，可能够为null。
     */
    public Context removeContext(String contextName) {
        return contextMap.remove(contextName);
    }

    /**
     * 得到指定的context。
     *
     * @param contextName
     * @return
     */
    public Context getContext(String contextName) {
        return contextMap.get(contextName);
    }

    /**
     * 是否包含指定的context
     *
     * @param contextName context名称
     * @return 存在返回true，不存在返回false。
     */
    public boolean containsContext(String contextName) {
        return contextMap.containsKey(contextName);
    }

    /**
     * 启用或禁用指定context
     *
     * @param contextName context名称
     * @param enable      是否启用
     * @return 返回对应Context，可能为null。
     */
    public Context enableContext(String contextName, boolean enable) {
        Context context = contextMap.get(contextName);
        if (context != null) {
            context.setEnable(enable);
        }
        return context;
    }

    public void doRequest(PreRequest req, WRequest request, WResponse response) {
        try {
            _doRequest(req, request, response);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ex(response, e, responseWhenException);
        }
    }

    public void clear() {
        contextMap.clear();
    }

    public Iterator<Context> contextIterator() {
        return contextMap.values().iterator();
    }


    public PreRequest forRequest(WRequest request, final WResponse response, PLinker pLinker) {
        String path = request.getPath();
//        if (path.startsWith("/="))
//        {
//
//            pLinker.toAllBridge().request(new PRequestWrap(request, ':' + path.substring(2)), new PCallback()
//            {
//                @Override
//                public void onResponse(PResponse lResponse)
//                {
//                    try
//                    {
//                        response.write(lResponse.getResponse());
//                    } catch (IOException e)
//                    {
//                        LOGGER.error(e.getMessage(), e);
//                    } finally
//                    {
//                        WPTool.close(response);
//                    }
//                }
//            });
//
//            return null;
//        }
        UrlDecoder.Result result = urlDecoder.decode(path);
        Context context;
        if (result == null || (context = contextMap.get(result.contextName())) == null || !context.isEnable) {
            exNotFoundClassPort(request, response, responseWhenException);
            return null;
        } else {
            return new PreRequest(context, result);
        }
    }

    private void _doRequest(PreRequest req, WRequest request,
                            WResponse response) throws InvocationTargetException, IllegalAccessException {
        Context context = req.context;
        UrlDecoder.Result result = req.result;

        WObjectImpl wObject = new WObjectImpl(pName, result, request, response, context);

        Porter classPort = context.portContext.getClassPort(result.classTied());

        PorterOfFun funPort;

        InnerContextBridge innerContextBridge = context.innerContextBridge;
        boolean responseWhenException = innerContextBridge.responseWhenException;

        if (classPort == null || (funPort = classPort.getChild(result, request.getMethod())) == null) {
            exNotFoundClassPort(request, response, responseWhenException);
            return;
        }

        //全局通过检测
        Object rs = globalCheck(context, wObject);
        if (rs != null) {
            exCheckPassable(wObject, rs, responseWhenException);
            return;
        }

        //类参数初始化
        _PortIn clazzPIn = classPort.getPortIn();
        InNames inNames = clazzPIn.getInNames();
        wObject.cn = PortUtil.newArray(inNames.nece);
        wObject.cu = PortUtil.newArray(inNames.unece);
        wObject.cinner = PortUtil.newArray(inNames.inner);
        wObject.cInNames = inNames;

        ParamSource paramSource = getParamSource(context, result, request);

        TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;


        //类参数处理
        ParamDealt.FailedReason failedReason = PortUtil
                .paramDeal(clazzPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.cn, wObject.cu,
                           paramSource,
                           typeParserStore);
        if (failedReason != null) {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }


        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(clazzPIn.ignoreTypeParser(), context, classPort.getInObj(), true, wObject,
                                            paramSource, typeParserStore);
        if (failedReason != null) {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //类通过检测
        if (clazzPIn.getChecks().length > 0) {
            rs = willPass(context, clazzPIn.getChecks(), wObject, DuringType.ON_CLASS, null);
            if (rs != null) {
                exCheckPassable(wObject, rs, responseWhenException);
                return;
            }
        }

        //////////////////////////
        //////////////////////////


        if (funPort == null) {
            exNotFoundFun(wObject, result, responseWhenException);
            return;
        }
        _PortIn funPIn = funPort.getPortIn();
        if (funPIn.getTiedType() == TiedType.REST) {
            wObject.restValue = result.funTied();
        }

        //函数参数初始化
        inNames = funPIn.getInNames();
        wObject.fn = PortUtil.newArray(inNames.nece);
        wObject.fu = PortUtil.newArray(inNames.unece);
        wObject.finner = PortUtil.newArray(inNames.inner);
        wObject.fInNames = inNames;

        //函数参数处理

        failedReason = PortUtil
                .paramDeal(funPIn.ignoreTypeParser(), innerContextBridge.paramDealt, inNames, wObject.fn, wObject.fu,
                           paramSource,
                           typeParserStore);
        if (failedReason != null) {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        ///////////////////////////
        //转换成类或接口对象
        failedReason = paramDealOfPortInObj(funPIn.ignoreTypeParser(), context, funPort.getInObj(), false, wObject,
                                            paramSource, typeParserStore);
        if (failedReason != null) {
            exParamDeal(wObject, failedReason, responseWhenException);
            return;
        }
        //////////////////////////////


        //函数通过检测
        if (funPIn.getChecks().length > 0) {
            rs = willPass(context, funPIn.getChecks(), wObject, DuringType.ON_METHOD, null);
            if (rs != null) {
                exCheckPassable(wObject, rs, responseWhenException);
                return;
            }
        }

        Method javaMethod = funPort.getMethod();

        try {
            if (funPort.getArgCount() == 0) {
                rs = javaMethod.invoke(classPort.getObject());
            } else {
                rs = javaMethod.invoke(classPort.getObject(), wObject);
            }
            if (funPIn.getChecks().length > 0) {
                Object object = willPass(context, funPIn.getChecks(), wObject, DuringType.AFTER_METHOD, new Aspect(rs));
                if (object != null) {
                    exCheckPassable(wObject, object, responseWhenException);
                    return;
                }
            }
        } catch (Exception e) {
            if (funPIn.getChecks().length > 0) {
                Throwable cause = e.getCause();
                rs = willPass(context, funPIn.getChecks(), wObject, DuringType.ON_METHOD_EXCEPTION, new Aspect(cause));
                if (rs != null) {
                    exCheckPassable(wObject, rs, responseWhenException);
                } else {
                    close(wObject);
                }
                return;
            } else {
                throw e;
            }
        }

        switch (funPort.getPortOut().getOutType()) {
            case NoResponse:
                break;
            case Object:
                responseObject(wObject, rs);
                break;
        }

    }

    /**
     * 用于处理对象绑定。
     *
     * @param inObj
     * @param isInClass
     * @param wObjectImpl
     * @param paramSource
     * @param currentTypeParserStore
     * @return
     */
    private ParamDealt.FailedReason paramDealOfPortInObj(boolean ignoreTypeParser, Context context, InObj inObj,
                                                         boolean isInClass,
                                                         WObjectImpl wObjectImpl,
                                                         ParamSource paramSource, TypeParserStore currentTypeParserStore) {
        ParamDealt.FailedReason reason = null;
        if (inObj == null) {
            return null;
        }
        One[] ones = inObj.ones;
        Object[] inObjects = new Object[ones.length];
        if (isInClass) {
            wObjectImpl.cinObjs = inObjects;
        } else {
            wObjectImpl.finObjs = inObjects;
        }
        for (int i = 0; i < ones.length; i++) {
            One one = ones[i];
            Object object = PortUtil
                    .paramDealOne(ignoreTypeParser, context.innerContextBridge.paramDealt, one, paramSource,
                                  currentTypeParserStore);
            if (object instanceof ParamDealt.FailedReason) {
                return (ParamDealt.FailedReason) object;
            } else {
                inObjects[i] = object;
            }
        }

        return reason;
    }

    private final Object globalCheck(Context context, WObject wObject) {
        CheckPassable[] allGlobal = this.allGlobalChecks;

        for (int i = 0; i < allGlobal.length; i++) {
            Object rs = allGlobal[i].willPass(wObject, DuringType.ON_GLOBAL, null);
            if (rs != null) {
                return rs;
            }
        }

        CheckPassable[] contextChecks = context.contextChecks;
        for (int i = 0; i < contextChecks.length; i++) {
            Object rs = contextChecks[i].willPass(wObject, DuringType.ON_CONTEXT_GLOBAL, null);
            if (rs != null) {
                return rs;
            }
        }
        return null;
    }

    /**
     * 通过检测
     */
    private Object willPass(Context context, Class<? extends CheckPassable>[] cps, WObject wObject,
                            DuringType type, Aspect aspect) {
        PortContext portContext = context.portContext;
        for (int i = 0; i < cps.length; i++) {
            CheckPassable cp = portContext.getCheckPassable(cps[i]);
            Object rs = cp.willPass(wObject, type, aspect);
            if (rs != null) {
                return rs;
            }
        }
        return null;
    }


    /**
     * 整合地址栏查询参数。
     *
     * @return
     */
    private ParamSource getParamSource(Context context, final UrlDecoder.Result result, WRequest request) {
        ParamSourceHandle handle = context.paramSourceHandleManager.fromName(result.classTied());
        if (handle == null) {
            handle = context.paramSourceHandleManager.fromMethod(request.getMethod());
        }
        ParamSource ps;
        if (handle == null) {
            ps = new ParamsSourceDefault(result, request);
        } else {
            ps = handle.get(request, result);
        }
        return ps;
    }

////////////////////////////////////////////////
    //////////////////////////////////////////

    private final void close(WObject wObject) {
        WPTool.close(wObject.getResponse());
    }

    private void close(WResponse response) {
        WPTool.close(response);
    }

    private void ex(WResponse response, Throwable throwable, boolean responseWhenException) {
        if (responseWhenException) {
            JResponse jResponse = new JResponse(ResultCode.EXCEPTION);
            jResponse.setDescription(WPTool.getMessage(throwable));
            try {
                response.write(jResponse);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(response);
    }

    private void responseObject(WObject wObject, Object object) {
        if (object != null) {
            try {
                wObject.getResponse().write(object);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(wObject);
    }

    private void exCheckPassable(WObject wObject, Object obj, boolean responseWhenException) {
        if (responseWhenException) {
            JResponse jResponse = new JResponse(ResultCode.ACCESS_DENIED);
            jResponse.setDescription(String.valueOf(obj));
            try {
                wObject.getResponse().write(jResponse);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(wObject);
    }

    private void exParamDeal(WObject wObject, ParamDealt.FailedReason reason, boolean responseWhenException) {
        if (responseWhenException) {
            JResponse jResponse = new JResponse();
            jResponse.setCode(ResultCode.PARAM_DEAL_EXCEPTION);
            jResponse.setDescription(reason.desc());
            jResponse.setResult(reason.toJSON());
            try {
                wObject.getResponse().write(jResponse);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(wObject);
    }

    private void exNotFoundFun(WObject wObject, UrlDecoder.Result result, boolean responseWhenException) {
        if (responseWhenException) {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("fun:" + result.toString());
            try {
                wObject.getResponse().write(jResponse);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(wObject);
    }

    private void exNotFoundClassPort(WRequest request, WResponse response, boolean responseWhenException) {
        if (responseWhenException) {
            JResponse jResponse = new JResponse(ResultCode.NOT_AVAILABLE);
            jResponse.setDescription("method:" + request.getMethod() + ",path:" + request.getPath());
            try {
                response.write(jResponse);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        close(response);
    }


}