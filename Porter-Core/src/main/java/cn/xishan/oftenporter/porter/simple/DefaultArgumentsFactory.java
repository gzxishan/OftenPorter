package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.advanced.*;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.proxy.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


/**
 * 支持参数：{@linkplain FunParam}
 *
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public class DefaultArgumentsFactory implements IArgumentsFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArgumentsFactory.class);

    public interface ArgDealt
    {
        /**
         * @param oftenObject
         * @param method
         * @param optionArgMap 提供的可选参数
         * @return
         */
        Object getArg(OftenObject oftenObject, Method method, Map<String, Object> optionArgMap);
    }

    public static class BindEntityDealtArgDealt implements ArgDealt
    {

        private String key;

        public BindEntityDealtArgDealt(Class realType, PorterOfFun porterOfFun)
        {
            key = porterOfFun.getPath() + "@" + realType.getName();
            porterOfFun.putExtraEntity(key, realType);
        }

        @Override
        public Object getArg(OftenObject oftenObject, Method method, Map<String, Object> optionArgMap)
        {
            return oftenObject.extraEntity(key);
        }
    }

    public static class WObjectArgDealt implements ArgDealt
    {

        @Override
        public final Object getArg(OftenObject oftenObject, Method method, Map<String, Object> optionArgMap)
        {
            return oftenObject;
        }
    }

    public static class NeceArgDealt implements ArgDealt
    {
        private InNames.Name name;
        private String className;
        private TypeParserStore typeParserStore;

        public NeceArgDealt(InNames.Name name, String className, TypeParserStore typeParserStore)
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
                if (OftenTool.isNullOrEmptyCharSequence(v))
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
        public final Object getArg(OftenObject oftenObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = get(optionArgMap);
            if (OftenTool.isNullOrEmptyCharSequence(v))
            {
                v = DefaultParamDealt.getParam(oftenObject, name, oftenObject.getParamSource(),
                        typeParserStore.byId(name.typeParserId), name.getDealt());
                if (OftenTool.isNullOrEmptyCharSequence(v) && name.isNece(oftenObject))
                {
                    v = DefaultFailedReason.lackNecessaryParams("Lack necessary param:" + name.varName, name.varName);
                } else
                {
                    v = name.dealString(v);
                }
            }
            return v;
        }
    }

    public static class UneceArgDealt implements ArgDealt
    {
        private InNames.Name name;
        private String className;
        private TypeParserStore typeParserStore;

        public UneceArgDealt(InNames.Name name, String className, TypeParserStore typeParserStore)
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
                if (OftenTool.isNullOrEmptyCharSequence(v))
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
        public final Object getArg(OftenObject oftenObject, Method method, Map<String, Object> optionArgMap)
        {
            Object v = get(optionArgMap);
            if (OftenTool.isNullOrEmptyCharSequence(v))
            {
                v = DefaultParamDealt.getParam(oftenObject, name, oftenObject.getParamSource(),
                        typeParserStore.byId(name.typeParserId), name.getDealt());
            }

            v = name.dealString(v);

            return v;
        }
    }

    protected static abstract class IArgsHandleImpl implements IArgsHandle
    {
        private ArgDealt[] argHandles;
        private Set<Class> types;
        private int[] argsIndex;

        //argsIndex可选择指定的参数
        public IArgsHandleImpl(@MayNull PorterOfFun porterOfFun, Class realClass, Method method,
                TypeParserStore typeParserStore, int[] argsIndex) throws Exception
        {
            Annotation[][] methodAnnotations = method.getParameterAnnotations();
            Parameter[] parameters = method.getParameters();

            AnnotationDealt annotationDealt = AnnotationDealt.newInstance(true);
            List<ArgDealt> argHandleList = new ArrayList<>();

            Class[] realMethodArgTypes = AnnoUtil.Advance.getParameterRealTypes(realClass, method);

            if (argsIndex == null)
            {
                this.types = new HashSet<>(realMethodArgTypes.length);
                argsIndex = new int[realMethodArgTypes.length];
                for (int i = 0; i < argsIndex.length; i++)
                {
                    argsIndex[i] = i;
                }
            } else
            {
                this.types = new HashSet<>(argsIndex.length);
            }
            this.argsIndex = argsIndex;

            for (int k = 0; k < argsIndex.length; k++)
            {
                int i = argsIndex[k];
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

        public IArgsHandleImpl(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            this(porterOfFun, porterOfFun.getPorter().getClazz(), porterOfFun.getMethod(), typeParserStore, null);
        }

        public abstract ArgDealt newHandle(AnnotationDealt annotationDealt, @MayNull PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramRealType, String paramName, Annotation[] paramAnnotations) throws Exception;

        @Override
        public int[] getArgsIndex()
        {
            return argsIndex;
        }

        @Override
        public boolean hasParameterType(OftenObject oftenObject, PorterOfFun fun, Method method, Class<?> type)
        {
            return types.contains(type);
        }

        @Override
        public Object[] getInvokeArgs(OftenObject oftenObject, PorterOfFun fun, Method method, Object[] args)
        {
            Map<String, Object> map;
            PorterOfFun.ArgData argData = fun == null ? null : fun.getArgData(oftenObject);
            if (OftenTool.isEmptyOf(args) && argData == null)
            {
                map = new HashMap<>(1);
            } else
            {
                map = new HashMap<>(6);

                if (args != null)
                {
                    for (Object arg : args)
                    {
                        if (arg != null)
                        {
                            if (arg instanceof FunParam)
                            {
                                FunParam funParam = (FunParam) arg;
                                map.put(funParam.getName(), funParam.getValue());
                            } else
                            {
                                map.put(PortUtil.getRealClass(arg).getName(), arg);
                            }
                        }
                    }
                }

                if (argData != null)
                {
                    map.putAll(argData.getDataMap());
                }
            }

            if (fun != null)
            {
                map.put(PorterOfFun.class.getName(), fun);
            }
            Object[] newArgs = new Object[argHandles.length];
            for (int i = 0; i < newArgs.length; i++)
            {
                Object value = argHandles[i].getArg(oftenObject, method, map);
                if (value instanceof ParamDealt.FailedReason)
                {
                    ParamDealt.FailedReason failedReason = (ParamDealt.FailedReason) value;
                    JResponse jResponse = new JResponse(ResultCode.PARAM_DEAL_EXCEPTION);
                    jResponse.setDescription(failedReason.desc());
                    jResponse.setExtra(failedReason.toJSON());
                    throw new OftenCallException(jResponse);
                } else
                {
                    newArgs[i] = value;
                }
            }
            return newArgs;
        }
    }

    public static class IArgsHandleImpl2 extends IArgsHandleImpl
    {

        public IArgsHandleImpl2(Class realClass, Method method,
                TypeParserStore typeParserStore, int[] argsIndex) throws Exception
        {
            super(null, realClass, method, typeParserStore, argsIndex);
        }

        public IArgsHandleImpl2(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            super(porterOfFun, typeParserStore);
        }

        @Override
        public ArgDealt newHandle(AnnotationDealt annotationDealt, @MayNull PorterOfFun porterOfFun,
                TypeParserStore typeParserStore,
                Class<?> paramRealType, String paramName, Annotation[] paramAnnotations) throws Exception
        {
            if (paramRealType.equals(OftenObject.class))
            {
                return new WObjectArgDealt();
            }
            _NeceUnece neceUnece;
            _Nece nece = annotationDealt.nece(AnnoUtil.getAnnotation(paramAnnotations, Nece.class), paramName);
            _Unece unece = null;
            if (nece == null)
            {
                unece = annotationDealt.unNece(AnnoUtil.getAnnotation(paramAnnotations, Unece.class), paramName);
                neceUnece = unece;
            } else
            {
                neceUnece = nece;
            }

            ArgDealt argHandle;

            if (porterOfFun != null && AnnoUtil.isOneOfAnnotationsPresent(paramRealType, BindEntityDealt.class))
            {
                argHandle = new BindEntityDealtArgDealt(paramRealType, porterOfFun);
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
                    name = nece.getVarName();
                } else if (unece != null)
                {
                    name = unece.getVarName();
                } else
                {
                    name = paramName;
                }

                InNames.Name theName = Porter
                        .getName(annotationDealt, name, paramRealType, _parse, neceUnece, typeParserStore);
                if (nece != null)
                {
                    argHandle = new NeceArgDealt(theName, paramRealType.getName(), typeParserStore);
                } else
                {
                    argHandle = new UneceArgDealt(theName, paramRealType.getName(), typeParserStore);
                }
            }


            return argHandle;
        }
    }


    public DefaultArgumentsFactory()
    {
    }

    public IArgsHandleImpl newIArgsHandle(Class realType, Method method, @MayNull int[] argsIndex,
            TypeParserStore typeParserStore) throws Exception
    {
        IArgsHandleImpl handle = new IArgsHandleImpl2(realType, method, typeParserStore, argsIndex);
        return handle;
    }

    public IArgsHandleImpl newIArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
        IArgsHandleImpl handle = new IArgsHandleImpl2(porterOfFun, typeParserStore);
        return handle;
    }

    @Override
    public final void initArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
        IArgsHandle iArgsHandle = newIArgsHandle(porterOfFun, typeParserStore);
        porterOfFun.setArgsHandle(iArgsHandle);
    }


    /**
     * 形参支持{@linkplain Property}注解。
     *
     * @param configData
     * @param object
     * @param method
     * @param optionArgs
     * @return
     * @throws Exception
     */
    public static Object invokeWithArgs(@MayNull IConfigData configData, Object object, Method method,
            Object... optionArgs) throws Exception
    {
        Class currentClass = object == null ? method.getDeclaringClass() : ProxyUtil.unwrapProxyForGeneric(object);

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++)
        {
            Class realType = AnnoUtil.Advance.getRealTypeOfMethodParameter(currentClass, method, i);
            if (configData != null)
            {
                Parameter parameter = parameters[i];
                Property property = AnnoUtil.getAnnotation(parameter, Property.class);
                if (property != null)
                {
                    Object value = configData.getValue(object, currentClass, method, i, realType, property);
                    args[i] = value;
                    continue;//为property参数，继续处理下一个
                }
            }

            Object obj = null;
            for (Object o : optionArgs)
            {
                if (OftenTool.isAssignable(o, realType))
                {
                    obj = o;
                    break;
                }
            }
            args[i] = obj;
        }
        try
        {
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Exception e)
        {
            LOGGER.warn("invoke method failed: method={},object={},args={}", method, object, args);
            throw e;
        }
    }

}
