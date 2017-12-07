package cn.xishan.oftenporter.bridge.http.websocket;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public class ClientCloseReason
{
    private int code;
    private String reason;

    public ClientCloseReason(int code, String reason)
    {
        this.code = code;
        this.reason = reason;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
