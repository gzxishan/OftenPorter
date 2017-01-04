package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.base.StateListener;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
class StateListenerForAll implements StateListener
{
    private Set<StateListener> set;

    public StateListenerForAll(Set<StateListener> set)
    {
        this.set = set;
    }

    @Override
    public void beforeSeek(InitParamSource initParamSource,PorterConf porterConf, ParamSourceHandleManager paramSourceHandleManager)
    {
        Iterator<StateListener> stateListenerIterator = set.iterator();
        while (stateListenerIterator.hasNext())
        {
            stateListenerIterator.next().beforeSeek(initParamSource,porterConf, paramSourceHandleManager);
        }
    }

    @Override
    public void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager)
    {
        Iterator<StateListener> stateListenerIterator = set.iterator();
        while (stateListenerIterator.hasNext())
        {
            stateListenerIterator.next().afterSeek(initParamSource, paramSourceHandleManager);
        }
    }

    @Override
    public void afterStart(InitParamSource initParamSource)
    {
        Iterator<StateListener> stateListenerIterator = set.iterator();
        while (stateListenerIterator.hasNext())
        {
            stateListenerIterator.next().afterStart(initParamSource);
        }
    }

    @Override
    public void beforeDestroy()
    {
        Iterator<StateListener> stateListenerIterator = set.iterator();
        while (stateListenerIterator.hasNext())
        {
            stateListenerIterator.next().beforeDestroy();
        }
    }

    @Override
    public void afterDestroy()
    {
        Iterator<StateListener> stateListenerIterator = set.iterator();
        while (stateListenerIterator.hasNext())
        {
            stateListenerIterator.next().afterDestroy();
        }
    }
}
