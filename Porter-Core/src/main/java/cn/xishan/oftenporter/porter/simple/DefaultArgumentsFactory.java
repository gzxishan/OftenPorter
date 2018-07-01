package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.WCallException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public class DefaultArgumentsFactory implements IArgumentsFactory
{
    public interface ArgHandle
    {
        Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap);
    }

    public static class WObjectArgHandle implements ArgHandle
    {

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject;
        }
    }

    public static class NeceArgHandle implements ArgHandle
    {
        private InNames.Name name;
        private String className;
        private TypeParserStore typeParserStore;
        private _Nece nece;

        public NeceArgHandle(_Nece nece, InNames.Name name, String className, TypeParserStore typeParserStore)
        {
            this.nece = nece;
            this.name = name;
            this.className = className;
            this.typeParserStore = typeParserStore;
        }

        private final Object get(Map<String, Object> optionArgMap)
        {
            Object v;
            if (className != null)
            {
                v = optionArgMap.get(className);
                if (v == null)
                {
                    v = optionArgMap.get(name.varName);
                }
            } else
            {
                v = optionArgMap.get(name.varName);
            }
            return v;
        }

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = get(optionArgMap);
            if (v == null)
            {
                v = DefaultParamDealt.getParam(wObject, name.varName, wObject.getParamSource(),
                        typeParserStore.byId(name.typeParserId), name.getDealt());
                if (v == null && nece.isNece(wObject))
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

    public static class UneceArgHandle implements ArgHandle
    {
        private InNames.Name name;
        private String className;
        private TypeParserStore typeParserStore;

        public UneceArgHandle(InNames.Name name, String className, TypeParserStore typeParserStore)
        {
            this.name = name;
            this.className = className;
            this.typeParserStore = typeParserStore;
        }

        private final Object get(Map<String, Object> optionArgMap)
        {
            Object v;
            if (className != null)
            {
                v = optionArgMap.get(className);
                if (v == null)
                {
                    v = optionArgMap.get(name.varName);
                }
            } else
            {
                v = optionArgMap.get(name.varName);
            }
            return v;
        }

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = get(optionArgMap);
            if (v == null)
            {
                v = DefaultParamDealt.getParam(wObject, name.varName, wObject.getParamSource(),
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

    public static abstract class IArgsHandleImpl implements IArgsHandle
    {
        private ArgHandle[] argHandles;
        private Set<Class> types;

        public IArgsHandleImpl(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            Method method = porterOfFun.getMethod();
            Class<?>[] methodArgTypes = method.getParameterTypes();
            Annotation[][] methodAnnotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();

            AnnotationDealt annotationDealt = AnnotationDealt.newInstance(true);

            List<ArgHandle> argHandleList = new ArrayList<>();
            this.types = new HashSet<>(methodArgTypes.length);

            for (int i = 0; i < methodArgTypes.length; i++)
            {
                Class<?> paramType = methodArgTypes[i];
                this.types.add(paramType);
                Annotation[] paramAnnotations = methodAnnotations[i];
                String paramName = parameters[i].getName();
                ArgHandle argHandle = newHandle(annotationDealt,porterOfFun,typeParserStore,paramType,paramName,paramAnnotations);
                argHandleList.add(argHandle);
            }
            this.argHandles = argHandleList.toArray(new ArgHandle[0]);
        }

        public abstract ArgHandle newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramType, String paramName, Annotation[] paramAnnotations) throws Exception;

        @Override
        public boolean hasParameterType(WObject wObject, Method method, Class<?> type)
        {
            return types.contains(type);
        }

        @Override
        public Object[] getInvokeArgs(WObject wObject, Method method, Object[] args)
        {
            Map<String, Object> map;
            if (args.length == 0)
            {
                map = Collections.emptyMap();
            } else
            {
                map = new HashMap<>(6);
                for (Object arg : args)
                {
                    if (arg != null)
                    {
                        map.put(arg.getClass().getName(), arg);
                    }
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

    public static class IArgsHandleImpl2 extends IArgsHandleImpl
    {

        public IArgsHandleImpl2(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            super(porterOfFun, typeParserStore);
        }

        @Override
        public ArgHandle newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramType, String paramName, Annotation[] paramAnnotations) throws Exception
        {
            if (paramType.equals(WObject.class))
            {
                return new WObjectArgHandle();
            }

            _Nece nece = annotationDealt
                    .nece(AnnoUtil.getAnnotation(paramAnnotations, Nece.class), paramName);
            _Unece unece = null;
            if (nece == null)
            {
                unece = annotationDealt.unNece(AnnoUtil.getAnnotation(paramAnnotations, Unece.class), paramName);
            }

            ArgHandle argHandle;
            String name;
            Parse parse = AnnoUtil.getAnnotation(paramAnnotations, Parse.class);
            _Parse _parse = null;
            if (parse != null)
            {
                _parse = annotationDealt.genParse(parse);
            }
            if (nece != null)
            {
                name = nece.getValue();
            } else if (unece != null)
            {
                name = unece.getValue();
            } else
            {
                name = paramName;
            }

            InNames.Name theName = porterOfFun.getPorter().getName(name, paramType, _parse, nece);
            if (nece != null)
            {
                argHandle = new NeceArgHandle(nece, theName, paramType.getName(), typeParserStore);
            } else
            {
                argHandle = new UneceArgHandle(theName, paramType.getName(), typeParserStore);
            }
            return argHandle;
        }
    }


    private Map<PorterOfFun, IArgsHandle> handleMap = new ConcurrentHashMap<>();

    public DefaultArgumentsFactory()
    {
    }

    public IArgsHandleImpl newIArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
        IArgsHandleImpl handle = new IArgsHandleImpl2(porterOfFun, typeParserStore);
        return handle;
    }

    @Override
    public final void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
        handleMap.put(porterOfFun, newIArgsHandle(porterOfFun, typeParserStore));
    }

    @Override
    public final IArgsHandle getArgsHandle(PorterOfFun porterOfFun)
    {
        IArgsHandle handle = handleMap.get(porterOfFun);
        return handle;
    }
}
