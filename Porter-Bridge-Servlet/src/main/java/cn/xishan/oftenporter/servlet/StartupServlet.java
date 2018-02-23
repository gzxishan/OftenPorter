package cn.xishan.oftenporter.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * 用于servlet,请求地址格式为:http://host[:port]/ServletContextPath[/=pname]/contextName/ClassTied/[funTied|restValue][?name1
 * =value1
 * &name2=value2...]
 * <pre>
 *     <strong>注意：</strong>url-pattern必须是"xxx/*"(xxx不含统配符,x可含"/")的形式,如"/op-porter/*"
 *     初始参数有：
 *     pname:框架实例名称，默认为"当前类名".
 *     responseWhenException:默认为true。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/23.
 */
public abstract class StartupServlet extends WMainServlet
{

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

    @Override
    public final void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    @Override
    public final void init() throws ServletException
    {
        super.init();
        onStart();
    }
    public abstract void onStart();
}
