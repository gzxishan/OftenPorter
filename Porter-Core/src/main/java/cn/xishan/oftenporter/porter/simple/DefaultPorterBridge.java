package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.init.PorterBridge;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
public class DefaultPorterBridge
{
    public static PorterBridge defaultBridge(final PorterConf porterConf)
    {
        PorterBridge bridge = new PorterBridge()
        {

            @Override
            public String contextName()
            {
                return porterConf.getContextName();
            }

            @Override
            public ParamDealt paramDealt()
            {
                return new DefaultParamDealt();
            }

            @Override
            public PorterConf porterConf()
            {
                return porterConf;
            }

            @Override
            public ParamSourceHandleManager paramSourceHandleManager()
            {
                return porterConf.getParamSourceHandleManager();
            }
        };
        return bridge;
    }
}
