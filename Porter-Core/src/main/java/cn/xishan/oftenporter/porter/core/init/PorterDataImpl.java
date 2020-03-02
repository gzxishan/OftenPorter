package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.PortExecutor;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.OftenTool;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
        return new EnumerationImpl<>(executor.contextNameIterator());
    }

    @Override
    public Enumeration<Porter> ofContextPorters(String contextName)
    {
        if (OftenTool.isEmpty(contextName))
        {
            throw new NullPointerException("context name is null");
        }
        Context context = executor.getContext(contextName);
        return new EnumerationImpl<>(context.contextPorter.getPortMap().values().iterator());
    }

    @Override
    public Enumeration<CheckPassable> ofAllCheckPassables(String contextName)
    {
        return executor.ofAllCheckPassables(contextName);
    }
}
