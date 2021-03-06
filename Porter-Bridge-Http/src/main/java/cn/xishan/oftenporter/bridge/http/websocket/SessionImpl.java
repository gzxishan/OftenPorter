package cn.xishan.oftenporter.bridge.http.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.PongFrame;

import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
class SessionImpl implements Session
{
    WebSocketClient webSocketClient;
    private OnClose onClose;
    private String id;
    private Map<String, Object> attrMap;

    interface OnClose
    {
        void onClosed();
    }

    public SessionImpl(WebSocketClient webSocketClient, OnClose onClose, String id)
    {
        this.webSocketClient = webSocketClient;
        this.onClose = onClose;
        this.id = id;
        this.attrMap = new Hashtable<>();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public Map<String, Object> getUserProperties()
    {
        return attrMap;
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
    public void sendPing(ByteBuffer applicationData)
    {
        PingFrame pingFrame = new PingFrame();
        if (applicationData != null)
        {
            pingFrame.setPayload(applicationData);
        }
        webSocketClient.sendFrame(pingFrame);
    }

    @Override
    public void sendPong(ByteBuffer applicationData)
    {
        PongFrame pongFrame = new PongFrame();
        if (applicationData != null)
        {
            pongFrame.setPayload(applicationData);
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
