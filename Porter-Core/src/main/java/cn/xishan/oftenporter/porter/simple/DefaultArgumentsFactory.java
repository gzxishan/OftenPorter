package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
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
        /**
         * @param wObject
         * @param method
         * @param optionArgMap 提供的可选参数
         * @return
         */
        Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap);
    }

    public static class BindEntityDealtArgHandle implements ArgHandle
    {

        private String key;

        public BindEntityDealtArgHandle(Class realType, PorterOfFun porterOfFun)
        {
            key = realType.getName();
            porterOfFun.putExtraEntity(key, realType);
        }

        @Override
        public Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject.extraEntity(key);
        }
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

    protected static abstract class IArgsHandleImpl implements IArgsHandle
    {
        private ArgHandle[] argHandles;
        private Set<Class> types;

        public IArgsHandleImpl(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            Class realClass = porterOfFun.getPorter().getClazz();

            Method method = porterOfFun.getMethod();
            Annotation[][] methodAnnotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();

            AnnotationDealt annotationDealt = AnnotationDealt.newInstance(true);
            int argCount = method.getParameterCount();

            List<ArgHandle> argHandleList = new ArrayList<>();
            this.types = new HashSet<>(argCount);

            for (int i = 0; i < argCount; i++)
            {
                Class<?> paramType = AnnoUtil.Advanced.getRealTypeOfMethodParameter(realClass, method, i);
                this.types.add(paramType);
                Annotation[] paramAnnotations = methodAnnotations[i];
                String paramName = parameters[i].getName();
                ArgHandle argHandle = newHandle(annotationDealt, porterOfFun, typeParserStore, paramType, paramName,
                        paramAnnotations);
                argHandleList.add(argHandle);
            }
            this.argHandles = argHandleList.toArray(new ArgHandle[0]);
        }

        public abstract ArgHandle newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramRealType, String paramName, Annotation[] paramAnnotations) throws Exception;

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
                        map.put(PortUtil.getRealClass(arg).getName(), arg);
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
                Class<?> paramRealType, String paramName, Annotation[] paramAnnotations) throws Exception
        {
            if (paramRealType.equals(WObject.class))
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

            if (AnnoUtil.isOneOfAnnotationsPresent(paramRealType, BindEntityDealt.class))
            {
                argHandle = new BindEntityDealtArgHandle(paramRealType, porterOfFun);
            } else
            {
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

                InNames.Name theName = porterOfFun.getPorter().getName(name, paramRealType, _parse, nece);
                if (nece != null)
                {
                    argHandle = new NeceArgHandle(nece, theName, paramRealType.getName(), typeParserStore);
                } else
                {
                    argHandle = new UneceArgHandle(theName, paramRealType.getName(), typeParserStore);
                }
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
