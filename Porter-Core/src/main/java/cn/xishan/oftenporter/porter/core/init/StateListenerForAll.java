package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.StateListener;

import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/16.
 */
class StateListenerForAll implements StateListener
{
    private List<StateListener> stateListenerList;

    public StateListenerForAll(List<StateListener> stateListenerList)
    {
        this.stateListenerList = stateListenerList;
    }

    @Override
    public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
            ParamSourceHandleManager paramSourceHandleManager)
    {
        for (StateListener stateListener : stateListenerList)
        {
            stateListener.beforeSeek(initParamSource, porterConf, paramSourceHandleManager);
        }
    }

    @Override
    public void afterSeek(InitParamSource initParamSource, ParamSourceHandleManager paramSourceHandleManager)
    {
        for (StateListener stateListener : stateListenerList)
        {
            stateListener.afterSeek(initParamSource, paramSourceHandleManager);
        }
    }

    @Override
    public void beforeSetOk(OftenObject oftenObject, InitParamSource initParamSource)
    {
        for (int i = stateListenerList.size() - 1; i >= 0; i--)
        {
            stateListenerList.get(i).beforeSetOk(oftenObject, initParamSource);
        }
    }

    @Override
    public void afterSetOk(OftenObject oftenObject, InitParamSource initParamSource)
    {
        for (int i = stateListenerList.size() - 1; i >= 0; i--)
        {
            stateListenerList.get(i).afterSetOk(oftenObject, initParamSource);
        }
    }

    @Override
    public void beforeStart(OftenObject oftenObject, InitParamSource initParamSource)
    {
        for (int i = stateListenerList.size() - 1; i >= 0; i--)
        {
            stateListenerList.get(i).beforeStart(oftenObject, initParamSource);
        }
    }

    @Override
    public void afterStart(OftenObject oftenObject, InitParamSource initParamSource)
    {
        for (int i = stateListenerList.size() - 1; i >= 0; i--)
        {
            stateListenerList.get(i).afterStart(oftenObject, initParamSource);
        }
    }

    @Override
    public void beforeDestroy()
    {
        for (StateListener stateListener : stateListenerList)
        {
            stateListener.beforeDestroy();
        }
    }

    @Override
    public void afterDestroy()
    {
        for (int i = stateListenerList.size() - 1; i >= 0; i--)
        {
            stateListenerList.get(i).afterDestroy();
        }
    }
}
