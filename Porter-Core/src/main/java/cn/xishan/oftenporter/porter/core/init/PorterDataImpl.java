package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.PortExecutor;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/5.
 */
class PorterDataImpl implements PorterData
{
    private PortExecutor executor;

    public PorterDataImpl(PortExecutor executor)
    {
        this.executor = executor;
    }

    @Override
    public Enumeration<String> ofContexts()
    {
        return new EnumerationImpl<String>(executor.contextNameIterator());
    }

    @Override
    public Enumeration<Porter> ofContextPorters(String contextName)
    {
        Context context = executor.getContext(contextName);
        return new EnumerationImpl<Porter>(context.contextPorter.getPortMap().values().iterator());
    }
}
