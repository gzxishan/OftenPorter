package cn.xishan.oftenporter.bridge.http.websocket;

import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public abstract class WSClientConfig
{
    public int initDelay = 0;

    public int retryDelay = 5000;

    public int retryTimes = 20;

    public int connectTimeout = 10 * 1000;

    public int heartDelay = 60 * 1000;

    public int heartTimeout = 15 * 1000;


    public abstract String getWSUrl();

    public abstract Map<String, String> getConnectHeaders();

    public WSClientConfig()
    {
    }
}
