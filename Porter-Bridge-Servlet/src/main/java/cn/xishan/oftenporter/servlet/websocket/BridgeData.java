package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2018-10-25.
 */
class BridgeData implements Serializable
{

    private static final long serialVersionUID = -5131511912361825548L;
    private String id;
    transient OftenObject oftenObject;
    transient PorterOfFun porterOfFun;
    transient WebSocket webSocket;
    transient WSConfig wsConfig;

    private static final Map<String, WeakReference<BridgeData>> BRIDGE_DATA_MAP = new HashMap<>();
    private static long lastCheck;

    public BridgeData(OftenObject oftenObject, PorterOfFun porterOfFun, WebSocket webSocket,WSConfig wsConfig)
    {
        this.oftenObject = oftenObject;
        this.porterOfFun = porterOfFun;
        this.webSocket = webSocket;
        this.wsConfig=wsConfig;
        this.id = OftenKeyUtil.randomUUID();
        synchronized (BRIDGE_DATA_MAP)
        {
            check();
            BRIDGE_DATA_MAP.put(id, new WeakReference<>(this));
        }
    }

    private static void check()
    {
        synchronized (BRIDGE_DATA_MAP)
        {
            if (System.currentTimeMillis() - lastCheck > 20 * 1000)
            {
                lastCheck = System.currentTimeMillis();
                Iterator<Map.Entry<String, WeakReference<BridgeData>>> it = BRIDGE_DATA_MAP.entrySet().iterator();
                while (it.hasNext())
                {
                    Map.Entry<String, WeakReference<BridgeData>> entry = it.next();
                    if (entry.getValue().get() == null)
                    {
                        it.remove();
                    }
                }
            }
        }
    }

    public BridgeData gotData()
    {
        synchronized (BRIDGE_DATA_MAP)
        {
            WeakReference<BridgeData> reference = BRIDGE_DATA_MAP.remove(id);
            if (reference == null || reference.get() == null)
            {
                throw new RuntimeException("WebSocket BridgeData is null:id=" + id);
            }
            BridgeData bridgeData = reference.get();
            return bridgeData;
        }
    }

}
