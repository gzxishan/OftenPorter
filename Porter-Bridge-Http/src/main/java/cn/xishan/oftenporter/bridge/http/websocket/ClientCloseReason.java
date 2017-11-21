package cn.xishan.oftenporter.bridge.http.websocket;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public class ClientCloseReason
{
    public int code;
    public String reason;

    public ClientCloseReason(int code, String reason)
    {
        this.code = code;
        this.reason = reason;
    }
}
