package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.PortExecutor;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.servlet.websocket.WebSocketHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用于servlet,请求地址格式为:http://host[:port]/ServletContextPath[/=bridgeName]/contextName/ClassTied/[funTied|restValue
 * ][=*=][?name1
 * =value1
 * &name2=value2...]
 * <pre>
 *     <strong>注意：</strong>url-pattern必须是"xxx/*"(xxx不含统配符,x可含"/")的形式,如"/op-porter/*"
 *     初始参数有：
 *     bridgeName:框架实例名称，默认为"当前类名".
 *     responseWhenException:默认为true。
 * </pre>
 * <p>
 * 自定义链接映射({@linkplain CustomServletPath#CustomServletPath(String, String, Class) CustomServletPath})
 * ：重写{@linkplain #getCustomServletPaths()}
 * </p>
 * <p>
 * <strong>另见</strong>{@linkplain OftenInitializer}
 * </p>
 *
 * <p>
 * 默认支持的形式参数：HttpServletRequest,HttpServletResponse,HttpSession,
 * ServletContext。见{@linkplain DefaultServletArgumentsFactory}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/23.
 */
public abstract class StartupServlet extends OftenServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServlet.class);

    boolean isStarted = false;
    protected boolean doSysInit = false;

    public StartupServlet()
    {
    }

    public StartupServlet(MultiPartOption multiPartOption)
    {
        super(multiPartOption);
    }

    public StartupServlet(String bridgeName, boolean responseWhenException)
    {
        super(bridgeName, responseWhenException);
    }


    protected CustomServletPath[] getCustomServletPaths()
    {
        return null;
    }

    @Override
    public final void init() throws ServletException
    {
        super.init();

        CustomServletPath[] customServletPaths = getCustomServletPaths();
        if (customServletPaths != null)
        {
            for (CustomServletPath customServletPath : customServletPaths)
            {
                customServletPath.regServlet(this);
            }
        }

        try
        {
            onStart();
        } catch (Throwable e)
        {
            throw new ServletException(OftenTool.getCause(e));
        }
        if(doSysInit){
            doSysInit();
        }
        isStarted = true;
    }

    protected void doSysInit(){
        try
        {
            ServletContext servletContext = getServletContext();
            //初始化websocket
            PortExecutor portExecutor = porterMain.getPortExecutor();
            Iterator<Context> it = portExecutor.contextIterator();
            while (it.hasNext())
            {
                Context context = it.next();
                ServerContainer serverContainer = (ServerContainer) servletContext
                        .getAttribute("javax.websocket.server.ServerContainer");
                WebSocketHandle.initWithServerContainer(context.getConfigData(), serverContainer);

                //Filterer注入处理
                List<Filterer> filterers = (List<Filterer>) servletContext
                        .getAttribute(Filterer.class.getName());
                if (filterers != null)
                {
                    List<Filterer> current = new ArrayList<>();
                    for (Filterer filterer : filterers)
                    {
                        String oc = filterer.oftenContext();
                        if (oc.equals(context.getName()) || oc.equals("*"))
                        {
                            current.add(filterer);
                        }
                    }
                    if (current.size() > 0)
                    {
                        context.contextPorter.doAutoSetForInstance(current.toArray(new Filterer[0]));
                    }
                }

            }
        } catch (Throwable e)
        {
            throw new InitException(e);
        }
    }

    public abstract void onStart() throws Exception;
}
