package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;

/**
 */
public interface PorterBridge
{
    String oftenContextName();

    ParamDealt paramDealt();

    PorterConf porterConf();

    ParamSourceHandleManager paramSourceHandleManager();
}
