package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.advanced.ResponseHandle;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.advanced.DefaultReturnFactory;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class Context
{
    public final ContextPorter contextPorter;
    InnerContextBridge innerContextBridge;
    DeliveryBuilder deliveryBuilder;
    ParamSourceHandleManager paramSourceHandleManager;
    public final StateListener stateListenerForAll;
    boolean isEnable = true;

    String name, contentEncoding;

    CheckPassable[] contextChecks;
    CheckPassable[] porterCheckPassables;
    DefaultReturnFactory defaultReturnFactory;
    Map<Class, ResponseHandle> responseHandles;
    ResponseHandle defaultResponseHandle;

    public Context(DeliveryBuilder deliveryBuilder, ContextPorter contextPorter, CheckPassable[] contextChecks,
            ParamSourceHandleManager paramSourceHandleManager,
            StateListener stateListenerForAll, InnerContextBridge innerContextBridge,
            CheckPassable[] porterCheckPassables, DefaultReturnFactory defaultReturnFactory,
            Map<Class, ResponseHandle> responseHandles,ResponseHandle defaultResponseHandle)
    {

        this.deliveryBuilder = deliveryBuilder;
        this.contextPorter = contextPorter;
        this.contextChecks = contextChecks;
        this.paramSourceHandleManager = paramSourceHandleManager;
        this.stateListenerForAll = stateListenerForAll;
        this.innerContextBridge = innerContextBridge;
        this.porterCheckPassables = porterCheckPassables != null && porterCheckPassables.length > 0 ?
                porterCheckPassables : null;
        this.defaultReturnFactory = defaultReturnFactory;
        this.responseHandles = responseHandles;
        this.defaultResponseHandle=defaultResponseHandle;
        setEnable(true);
    }


    public String getContentEncoding()
    {
        return contentEncoding;
    }

    public String getName()
    {
        return name;
    }

    public void setEnable(boolean enable)
    {
        isEnable = enable;
    }

    public boolean isEnable()
    {
        return isEnable;
    }
}