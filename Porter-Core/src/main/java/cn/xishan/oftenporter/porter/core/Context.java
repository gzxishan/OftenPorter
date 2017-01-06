package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;

/**
 * @author Created by https://github.com/CLovinr on 2016/10/3.
 */
public class Context {
    public final PortContext portContext;
    CheckPassable[] contextChecks;
    InnerContextBridge innerContextBridge;
    DeliveryBuilder deliveryBuilder;
    ParamSourceHandleManager paramSourceHandleManager;
    public final StateListener stateListenerForAll;

    boolean isEnable = true;
    String name, contentEncoding;
    CheckPassable[] forAllCheckPassables;

    public Context(DeliveryBuilder deliveryBuilder, PortContext portContext, CheckPassable[] contextChecks,
                   ParamSourceHandleManager paramSourceHandleManager,
                   StateListener stateListenerForAll, InnerContextBridge innerContextBridge, CheckPassable[] forAllCheckPassables) {
        this.deliveryBuilder = deliveryBuilder;
        this.portContext = portContext;
        this.contextChecks = contextChecks;
        this.paramSourceHandleManager = paramSourceHandleManager;
        this.stateListenerForAll = stateListenerForAll;
        this.innerContextBridge = innerContextBridge;
        this.forAllCheckPassables = forAllCheckPassables != null && forAllCheckPassables.length > 0 ? forAllCheckPassables : null;
        setEnable(true);
    }


    public String getContentEncoding() {
        return contentEncoding;
    }

    public String getName() {
        return name;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isEnable() {
        return isEnable;
    }
}