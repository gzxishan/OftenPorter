package cn.xishan.oftenporter.bridge.http.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.PongFrame;

import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
class SessionImpl implements Session
{
    WebSocketClient webSocketClient;
    private OnClose onClose;

    interface OnClose
    {
        void onClosed();
    }

    public SessionImpl(WebSocketClient webSocketClient, OnClose onClose)
    {
        this.webSocketClient = webSocketClient;
        this.onClose = onClose;
    }

    @Override
    public void close()
    {
        webSocketClient.close();
        onClose.onClosed();
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
        onClose.onClosed();
    }

    @Override
    public void sendPing()
    {
        webSocketClient.sendPing();
    }

    @Override
    public void sendPong(ByteBuffer data)
    {
        PongFrame pongFrame = new PongFrame();
        if(data!=null){
            pongFrame.setPayload(data);
        }
        webSocketClient.sendFrame(pongFrame);
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
