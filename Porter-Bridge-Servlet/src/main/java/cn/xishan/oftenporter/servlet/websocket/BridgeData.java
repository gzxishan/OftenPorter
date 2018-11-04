package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.io.Serializable;

/**
 * @author Created by https://github.com/CLovinr on 2018-10-25.
 */
class BridgeData implements Serializable
{

    transient WObject wObject;
    transient PorterOfFun porterOfFun;
    transient WebSocket webSocket;
    transient WSConfig wsConfig;

    public BridgeData(WObject wObject, PorterOfFun porterOfFun, WebSocket webSocket,WSConfig wsConfig)
    {
        this.wObject = wObject;
        this.porterOfFun = porterOfFun;
        this.webSocket = webSocket;
        this.wsConfig=wsConfig;
    }

}
