package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
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


    static class RequestArgDealt implements ArgDealt
    {
        @Override
        public final HttpServletRequest getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject.getRequest().getOriginalRequest();
        }
    }

    static class ResponseArgDealt implements ArgDealt
    {
        @Override
        public final HttpServletResponse getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            return wObject.getRequest().getOriginalResponse();
        }
    }

    static class ContextArgDealt implements ArgDealt
    {
        @Override
        public final ServletContext getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            HttpServletRequest request = wObject.getRequest().getOriginalRequest();
            return request.getServletContext();
        }
    }


    static class SessionArgDealt implements ArgDealt
    {
        @Override
        public final HttpSession getArg(WObject wObject, Method method, Map<String, Object> optionArgMap)
        {
            HttpServletRequest request = wObject.getRequest().getOriginalRequest();
            return request.getSession();
        }
    }


    static class IArgsHandleImpl3 extends IArgsHandleImpl2
    {
        public IArgsHandleImpl3(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
        {
            super(porterOfFun, typeParserStore);
        }

        @Override
        public ArgDealt newHandle(AnnotationDealt annotationDealt, PorterOfFun porterOfFun,
                TypeParserStore typeParserStore, Class<?> paramType, String paramName,
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
                argHandle = super.newHandle(annotationDealt, porterOfFun, typeParserStore, paramType, paramName,
                                paramAnnotations);
            }
            return argHandle;
        }

    }


    public DefaultServletArgumentsFactory()
    {
    }

    @Override
    public IArgsHandleImpl newIArgsHandle(PorterOfFun porterOfFun, TypeParserStore typeParserStore) throws Exception
    {
        return new IArgsHandleImpl3(porterOfFun, typeParserStore);
    }

}
