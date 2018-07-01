package cn.xishan.oftenporter.bridge.http.server;

import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.servlet.OPServlet;

import javax.servlet.ServletException;

/**
 * <pre>
 *     初始参数还有：
 *     contextName
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2016/10/7.
 */
public abstract class HServerServlet extends OPServlet
{
    private String contextName;

    public HServerServlet()
    {
        super();
    }

    public HServerServlet(String pname, String contextName, boolean responseWhenException)
    {
        super(pname, responseWhenException);
        this.contextName = contextName;
    }

    public abstract Delivery getBridgeDelivery();

    @Override
    public final void init() throws ServletException
    {
        super.init();
        if (contextName == null)
        {
            contextName = getInitParameter("contextName");
        }
        PorterConf porterConf = newPorterConf();
        porterConf.setContextName(contextName);
        porterConf.addContextAutoSet("hServerDelivery", getBridgeDelivery());
        porterConf.getSeekPackages().addObjectPorter(new HServerPorter()
        {
        });
        startOne(porterConf);
    }
}
