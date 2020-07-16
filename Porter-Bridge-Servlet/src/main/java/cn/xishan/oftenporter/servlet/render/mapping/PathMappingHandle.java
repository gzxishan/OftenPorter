package cn.xishan.oftenporter.servlet.render.mapping;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.StartupServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2020/5/16.
 */
public class PathMappingHandle extends AspectOperationOfPortIn.HandleAdapter<PathMapping> implements Comparable<PathMappingHandle>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PathMappingHandle.class);
    private static final String HANDLE_KEY = PathMappingHandle.class.getName() + "@-pathmapping-handle-key-";

    private int order;
    private String[] path;
    private String oftenPath;
    private Set<PortMethod> methods;

    @AutoSet
    private ServletContext servletContext;

    @Override
    public int compareTo(PathMappingHandle pathMappingHandle)
    {
        if (this.order > pathMappingHandle.order)
        {
            return 1;
        } else if (this.order < pathMappingHandle.order)
        {
            return -1;
        } else
        {
            return 0;
        }
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }

    @Override
    public boolean needInvoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn)
    {
        return true;
    }

    @Override
    public boolean init(PathMapping current, IConfigData configData, PorterOfFun porterOfFun)
    {
        if (current.enable())
        {
            this.order = current.order();
            Set<String> stringSet = new HashSet<>();
            OftenTool.addAll(stringSet, current.path());
            this.path = stringSet.toArray(new String[0]);
            this.oftenPath = porterOfFun.getPath();
            this.methods = new HashSet<>();
            OftenTool.addAll(this.methods, porterOfFun.getMethodPortIn().getMethods());

            List<PathMappingHandle> handleList = configData.get(HANDLE_KEY);
            if (handleList == null)
            {
                handleList = new ArrayList<>();
                configData.set(HANDLE_KEY, handleList);
            }
            handleList.add(this);
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public void onStart(OftenObject oftenObject)
    {
        IConfigData configData = oftenObject.getConfigData();
        List<PathMappingHandle> handleList = configData.get(HANDLE_KEY);
        if (handleList != null)
        {
            configData.remove(HANDLE_KEY);
            StartupServlet startupServlet = oftenObject.getContextSet(StartupServlet.class);
            PathMappingHandle[] handles = handleList.toArray(new PathMappingHandle[0]);
            Arrays.sort(handles);


            for (PathMappingHandle handle : handles)
            {
                String oftenPath = handle.oftenPath;
                String[] path = handle.path;

                FilterRegistration.Dynamic dynamic = servletContext
                        .addFilter("@pathmapping:" + oftenPath,
                                new PathMappingFilter(startupServlet, oftenPath, methods));
                dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),
                        false, path);
                dynamic.setAsyncSupported(true);
            }
        }
    }

    @Override
    public Object invoke(OftenObject oftenObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        HttpServletResponse response = oftenObject.getRequest().getOriginalResponse();
        try
        {
            Object rt = porterOfFun.invokeByHandleArgs(oftenObject);

            if (rt instanceof Boolean)
            {
                if (!(Boolean) rt)
                {//返回false，则执行后续过滤器
                    HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
                    FilterChain chain = (FilterChain) request.getAttribute(PathMappingFilter.FILTER_NAME);
                    chain.doFilter(request, response);
                }
            } else if (rt != null)
            {
                LOGGER.warn("return object is ignored:often path={},srvlet path={},object={}", oftenPath, path, rt);
            }
            return null;
        } catch (Throwable e)
        {
            LOGGER.warn(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

}
