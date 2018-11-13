package cn.xishan.oftenporter.servlet.websocket;

/**
 * @author Created by https://github.com/CLovinr on 2018/4/28.
 */
public class WSConfig
{
    private int maxBinaryBuffer = -1;
    private int maxTextBuffer = -1;
    private long maxIdleTime = -1;
    private boolean isPartial = false;

    private WebSocketOption webSocketOption;


    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("maxBinaryBuffer=").append(maxBinaryBuffer)
                .append(",maxTextBuffer=").append(maxTextBuffer)
                .append(",maxIdleTime=").append(maxIdleTime)
                .append(",isPartial=").append(isPartial);
        return stringBuilder.toString();
    }

    public boolean isPartial()
    {
        return isPartial;
    }

    public void setPartial(boolean partial)
    {
        isPartial = partial;
    }

    public int getMaxBinaryBuffer()
    {
        return maxBinaryBuffer;
    }

    public void setMaxBinaryBuffer(int maxBinaryBuffer)
    {
        this.maxBinaryBuffer = maxBinaryBuffer;
    }

    public int getMaxTextBuffer()
    {
        return maxTextBuffer;
    }

    public void setMaxTextBuffer(int maxTextBuffer)
    {
        this.maxTextBuffer = maxTextBuffer;
    }

    public long getMaxIdleTime()
    {
        return maxIdleTime;
    }

    /**
     * 单位毫秒
     */
    public void setMaxIdleTime(long maxIdleTime)
    {
        this.maxIdleTime = maxIdleTime;
    }

    public WebSocketOption getWebSocketOption()
    {
        return webSocketOption;
    }

    public void setWebSocketOption(WebSocketOption webSocketOption)
    {
        this.webSocketOption = webSocketOption;
    }
}
