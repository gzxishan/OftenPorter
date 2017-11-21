package cn.xishan.oftenporter.bridge.http.websocket;

import org.java_websocket.client.WebSocketClient;

import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public class SessionImpl implements Session
{
    WebSocketClient webSocketClient;

    public SessionImpl(WebSocketClient webSocketClient)
    {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void close()
    {
        webSocketClient.close();
    }

    @Override
    public boolean isClosed()
    {
        return webSocketClient.isClosed();
    }

    @Override
    public void close(int code, String reason)
    {
        webSocketClient.close(code, reason);
    }

    @Override
    public void sendPing()
    {
        webSocketClient.sendPing();
    }

    @Override
    public void send(String text)
    {
        webSocketClient.send(text);
    }

    @Override
    public void send(ByteBuffer byteBuffer)
    {
        webSocketClient.send(byteBuffer);
    }

    @Override
    public void send(byte[] bs)
    {
        webSocketClient.send(bs);
    }
}
