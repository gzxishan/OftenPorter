package cn.xishan.oftenporter.bridge.http.websocket;

import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public interface Session
{
    /**
     * 调用后将不会连接。
     */
    public void close(int code, String reason);

    /**
     * 调用后将不会连接。
     */
    public void close();

    public boolean isClosed();

    public void sendPing(ByteBuffer applicationData);

    public void sendPong(ByteBuffer applicationData);

    public void send(String text);

    public void send(ByteBuffer byteBuffer);

    public void send(byte[] bs);
}
