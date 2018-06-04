package cn.xishan.oftenporter.bridge.http.websocket;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public abstract class WSClientConfig
{
    /**
     * 单位毫秒
     */
    public int initDelay = 0;

    /**
     * 单位毫秒
     */
    public int retryDelay = 20000;

    /**
     * 小于0表示无限重试
     */
    public int retryTimes = -1;

    /**
     * 单位毫秒
     */
    public int connectTimeout = 10 * 1000;

    public Integer connectionLostTimeoutSecond = null;


    public abstract String getWSUrl();

    public abstract Map<String, String> getConnectHeaders();

    public WSClientConfig()
    {
    }
}
