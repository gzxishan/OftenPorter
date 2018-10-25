package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2018-10-25.
 */
class BridgeData implements Serializable
{
    private String id;

    transient WObject wObject;
    transient PorterOfFun porterOfFun;
    transient WebSocket webSocket;
    private static final Map<String, WeakReference<BridgeData>> BRIDGE_DATA_MAP = new HashMap<>();
    private static long lastCheck;

    public BridgeData(WObject wObject, PorterOfFun porterOfFun, WebSocket webSocket)
    {
        this.id = KeyUtil.randomUUID();
        this.wObject = wObject;
        this.porterOfFun = porterOfFun;
        this.webSocket = webSocket;
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

    public BridgeData getInstance()
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

    public String getId()
    {
        return id;
    }
}
