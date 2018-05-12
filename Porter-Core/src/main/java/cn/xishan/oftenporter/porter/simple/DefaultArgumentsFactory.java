package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.NeceParam;
import cn.xishan.oftenporter.porter.core.annotation.UneceParam;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.porter.core.util.ConcurrentKeyLock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public class DefaultArgumentsFactory implements IArgumentsFactory
{
    interface ArgHandle
    {
        Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap);
    }

    static class WObjectArgHandle implements ArgHandle
    {

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject;
        }
    }

    static class NeceArgHandle implements ArgHandle
    {
        private InNames.Name name;
        private TypeParserStore typeParserStore;

        public NeceArgHandle(InNames.Name name, TypeParserStore typeParserStore)
        {
            this.name = name;
            this.typeParserStore = typeParserStore;
        }

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = optionArgMap.get(name);
            if (v == null)
            {
                v = DefaultParamDealt.getParam(name.varName, wObject.getParamSource(),
                        typeParserStore.byId(name.typeParserId), name.getDealt());
                if (v == null)
                {
                    v = DefaultFailedReason.lackNecessaryParams("Lack necessary params!", name.varName);
                }
            }
            if (v != null && (v instanceof ParamDealt.FailedReason))
            {
                ParamDealt.FailedReason failedReason = (ParamDealt.FailedReason) v;
                JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
                jResponse.setExtra(failedReason.toJSON());
                jResponse.setDescription(failedReason.desc());
                throw new WCallException(jResponse);
            }
            return v;
        }
    }

    static class UneceArgHandle implements ArgHandle
    {
        private InNames.Name name;
        private TypeParserStore typeParserStore;

        public UneceArgHandle(InNames.Name name, TypeParserStore typeParserStore)
        {
            this.name = name;
            this.typeParserStore = typeParserStore;
        }

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = optionArgMap.get(name.varName);
            if (v == null)
            {
                v = DefaultParamDealt.getParam(name.varName, wObject.getParamSource(),
                        typeParserStore.byId(name.typeParserId), name.getDealt());
            }
            if (v instanceof ParamDealt.FailedReason)
            {
                ParamDealt.FailedReason failedReason = (ParamDealt.FailedReason) v;
                JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
                jResponse.setExtra(failedReason.toJSON());
                jResponse.setDescription(failedReason.desc());
                throw new WCallException(jResponse);
            }
            return v;
        }
    }

    static class IArgsHandleImpl implements IArgsHandle
    {
        private ArgHandle[] argHandles;

        public IArgsHandleImpl(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws ClassNotFoundException
        {
            Method method = porterOfFun.getMethod();
            Class<?>[] methodArgTypes = method.getParameterTypes();
            Annotation[][] methodAnnotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();

            List<ArgHandle> argHandleList = new ArrayList<>();

            for (int i = 0; i < methodArgTypes.length; i++)
            {
                Class<?> type = methodArgTypes[i];
                if (type.equals(WObject.class))
                {
                    argHandleList.add(new WObjectArgHandle());
                    continue;
                }

                Annotation[] annotations = methodAnnotations[i];
                NeceParam neceParam = AnnoUtil.getAnnotation(annotations, NeceParam.class);
                UneceParam uneceParam = AnnoUtil.getAnnotation(annotations, UneceParam.class);
                String name = "";
                if (neceParam != null)
                {
                    name = neceParam.value();
                } else if (uneceParam != null)
                {
                    name = uneceParam.value();
                }
                if (name.equals(""))
                {
                    Parameter parameter = parameters[i];
                    name = parameter.getName();
                }
                InNames.Name theName = porterOfFun.getPorter().getName(name, type);
                ArgHandle argHandle = neceParam != null ? new NeceArgHandle(theName,
                        typeParserStore) : new UneceArgHandle(theName, typeParserStore);
                argHandleList.add(argHandle);
            }
            this.argHandles = argHandleList.toArray(new ArgHandle[0]);
        }

        @Override
        public Object[] getInvokeArgs(WObject wObject, Method method, Object[] args)
        {
            Map<String, Object> map = new HashMap<>();
            for (Object arg : args)
            {
                if (arg != null)
                {
                    map.put(arg.getClass().getName(), arg);
                }
            }
            Object[] newArgs = new Object[argHandles.length];
            for (int i = 0; i < newArgs.length; i++)
            {
                newArgs[i] = argHandles[i].getArg(wObject, method, map);
            }
            return newArgs;
        }
    }


    private Map<PorterOfFun, IArgsHandle> handleMap = new ConcurrentHashMap<>();
    private ConcurrentKeyLock<PorterOfFun> keyLock = new ConcurrentKeyLock<>();

    public DefaultArgumentsFactory()
    {
    }

    @Override
    public void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
//        IArgsHandle handle = handleMap.get(porterOfFun);
//        if (handle == null)
//        {
//            keyLock.lock(porterOfFun);
//            try
//            {
//                handle = handleMap.get(porterOfFun);
//                if (handle == null)
//                {
//                    handle = new IArgsHandleImpl(porterOfFun, typeParserStore);
//                    handleMap.put(porterOfFun, handle);
//                }
//            } finally
//            {
//                keyLock.unlock(porterOfFun);
//            }
//        }
        IArgsHandle handle = new IArgsHandleImpl(porterOfFun, typeParserStore);
        handleMap.put(porterOfFun, handle);
    }

    @Override
    public IArgsHandle getArgsHandle(PorterOfFun porterOfFun) throws Exception
    {
        IArgsHandle handle = handleMap.get(porterOfFun);
        return handle;
    }
}
