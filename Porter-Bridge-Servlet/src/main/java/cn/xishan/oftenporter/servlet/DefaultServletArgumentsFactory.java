package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnotationDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.simple.DefaultArgumentsFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
public class DefaultServletArgumentsFactory extends DefaultArgumentsFactory
{

    @AutoSet
    static ServletContext servletContext;

    static class RequestArgDealt implements ArgDealt
    {
        @Override
        public final HttpServletRequest getArg(@MayNull IConfigData configData, OftenObject oftenObject, Method method,
                Map<String, Object> optionArgMap)
        {
            return oftenObject.getRequest().getOriginalRequest();
        }

        @Override
        public Class getArgType()
        {
            return HttpServletRequest.class;
        }
    }

    static class ResponseArgDealt implements ArgDealt
    {
        @Override
        public final HttpServletResponse getArg(@MayNull IConfigData configData, OftenObject oftenObject, Method method,
                Map<String, Object> optionArgMap)
        {
            return oftenObject.getRequest().getOriginalResponse();
        }

        @Override
        public Class getArgType()
        {
            return HttpServletResponse.class;
        }
    }

    static class ContextArgDealt implements ArgDealt
    {
        @Override
        public final ServletContext getArg(@MayNull IConfigData configData, OftenObject oftenObject, Method method,
                Map<String, Object> optionArgMap)
        {
            return servletContext;
        }

        @Override
        public Class getArgType()
        {
            return ServletContext.class;
        }
    }


    static class SessionArgDealt implements ArgDealt
    {
        @Override
        public final HttpSession getArg(@MayNull IConfigData configData, OftenObject oftenObject, Method method,
                Map<String, Object> optionArgMap)
        {
            HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
            return request.getSession();
        }

        @Override
        public Class getArgType()
        {
            return HttpSession.class;
        }
    }


    static class IArgsHandleImpl3 extends IArgsHandleImpl2
    {
        public IArgsHandleImpl3(@MayNull IConfigData configData, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore) throws Exception
        {
            super(configData, porterOfFun, typeParserStore);
        }

        public IArgsHandleImpl3(@MayNull IConfigData configData, Class realClass, Method method,
                int[] argsIndex, TypeParserStore typeParserStore) throws Exception
        {
            super(configData, realClass, method, argsIndex, typeParserStore);
        }

        @Override
        public ArgDealt newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore, Class realClass, Class<?> paramType, String paramName, int paramIndex,
                Annotation[] paramAnnotations) throws Exception
        {
            ArgDealt argHandle;
            if (paramType.equals(HttpServletRequest.class))
            {
                argHandle = new RequestArgDealt();
            } else if (paramType.equals(HttpServletResponse.class))
            {
                argHandle = new ResponseArgDealt();
            } else if (paramType.equals(HttpSession.class))
            {
                argHandle = new SessionArgDealt();
            } else if (paramType.equals(ServletContext.class))
            {
                argHandle = new ContextArgDealt();
            } else
            {
                argHandle = super.newHandle(annotationDealt, porterOfFun, typeParserStore, realClass,
                        paramType, paramName, paramIndex, paramAnnotations);
            }
            return argHandle;
        }

    }


    public DefaultServletArgumentsFactory()
    {
    }

    @Override
    public IArgsHandle newIArgsHandle(@MayNull IConfigData configData, PorterOfFun porterOfFun,
            TypeParserStore typeParserStore) throws Exception
    {
        return new IArgsHandleImpl3(configData, porterOfFun, typeParserStore);
    }

    @Override
    public IArgsHandle newIArgsHandle(@MayNull IConfigData configData, Class realType, Method method, int[] argsIndex,
            TypeParserStore typeParserStore) throws Exception
    {
        return new IArgsHandleImpl3(configData, realType, method, argsIndex, typeParserStore);
    }
}
