package cn.xishan.oftenporter.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

/**
 * 用于servlet,请求地址格式为:http://host[:port]/ServletContextPath[/=pname]/contextName/ClassTied/[funTied|restValue
 * ][=*=][?name1
 * =value1
 * &name2=value2...]
 * <pre>
 *     <strong>注意：</strong>url-pattern必须是"xxx/*"(xxx不含统配符,x可含"/")的形式,如"/op-porter/*"
 *     初始参数有：
 *     pname:框架实例名称，默认为"当前类名".
 *     responseWhenException:默认为true。
 * </pre>
 * <p>
 * 自定义链接映射({@linkplain CustomServletPath#CustomServletPath(String, String, Class) CustomServletPath})
 * ：重写{@linkplain #getCustomServletPaths()}
 * </p>
 * <p>
 * <strong>另见</strong>{@linkplain OPServletInitializer}
 * </p>
 *
 * <p>
 * 默认支持的形式参数：HttpServletRequest,HttpServletResponse,HttpSession,ServletContext。见{@linkplain DefaultServletArgumentsFactory}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/23.
 */
public abstract class StartupServlet extends OPServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServlet.class);

    boolean isStarted = false;

    public StartupServlet()
    {
    }

    public StartupServlet(MultiPartOption multiPartOption)
    {
        super(multiPartOption);
    }

    public StartupServlet(String pname, boolean responseWhenException)
    {
        super(pname, responseWhenException);
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
            LOGGER.error(e.getMessage(),e);
            throw new ServletException(e);
        }
        isStarted = true;
    }

    public abstract void onStart()throws Throwable;
}
