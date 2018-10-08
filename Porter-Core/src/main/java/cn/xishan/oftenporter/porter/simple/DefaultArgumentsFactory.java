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
    public interface ArgDealt
    {
        /**
         * @param wObject
         * @param method
         * @param optionArgMap 提供的可选参数
         * @return
         */
        Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap);
    }

    public static class BindEntityDealtArgHandle implements ArgDealt
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

    public static class WObjectArgHandle implements ArgDealt
    {

        @Override
        public final Object getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject;
        }
    }

    public static class NeceArgHandle implements ArgDealt
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

    public static class UneceArgHandle implements ArgDealt
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
        private ArgDealt[] argHandles;
        private Set<Class> types;

        public IArgsHandleImpl(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            Class realClass = porterOfFun.getPorter().getClazz();

            Method method = porterOfFun.getMethod();
            Annotation[][] methodAnnotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();

            AnnotationDealt annotationDealt = AnnotationDealt.newInstance(true);
            List<ArgDealt> argHandleList = new ArrayList<>();

            Class[] realMethodArgTypes = getParameterRealTypes(realClass, method);
            this.types = new HashSet<>(realMethodArgTypes.length);
            for (int i = 0; i < realMethodArgTypes.length; i++)
            {
                Class<?> paramType = realMethodArgTypes[i];
                this.types.add(paramType);
                Annotation[] paramAnnotations = methodAnnotations[i];
                String paramName = parameters[i].getName();
                ArgDealt argHandle = newHandle(annotationDealt, porterOfFun, typeParserStore, paramType, paramName,
                        paramAnnotations);
                argHandleList.add(argHandle);
            }
            annotationDealt.clearCache();
            this.argHandles = argHandleList.toArray(new ArgDealt[0]);
        }

        public abstract ArgDealt newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramRealType, String paramName, Annotation[] paramAnnotations) throws Exception;

        @Override
        public boolean hasParameterType(WObject wObject, PorterOfFun fun, Method method, Class<?> type)
        {
            return types.contains(type);
        }

        @Override
        public Object[] getInvokeArgs(WObject wObject, PorterOfFun fun, Method method, Object[] args)
        {
            Map<String, Object> map;
            PorterOfFun.ArgData argData = fun == null ? null : fun.getArgData(wObject);
            if (args.length == 0 && argData == null)
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
                if (argData != null)
                {
                    map.putAll(argData.getDataMap());
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
        public ArgDealt newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
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

            ArgDealt argHandle;

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
    public IArgsHandle getArgsHandle(PorterOfFun porterOfFun)
    {
        IArgsHandle handle = handleMap.get(porterOfFun);
        return handle;
    }

    /**
     * 获取实际的参数类型类别，支持泛型。
     *
     * @param realClass 实际的类
     * @param method    方法
     * @return
     */
    public static Class[] getParameterRealTypes(Class realClass, Method method)
    {
        int argCount = method.getParameterCount();
        Class[] types = new Class[argCount];
        for (int i = 0; i < argCount; i++)
        {
            Class<?> paramType = AnnoUtil.Advance.getRealTypeOfMethodParameter(realClass, method, i);
            types[i] = paramType;
        }
        return types;
    }
}
