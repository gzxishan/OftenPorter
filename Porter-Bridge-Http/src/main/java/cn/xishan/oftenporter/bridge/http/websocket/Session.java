package cn.xishan.oftenporter.bridge.http.websocket;

import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public interface Session
{
    public void close();

    public boolean isClosed();

    public void close(int code, String reason);

    public void sendPing();

    public void send(String text);

    public void send(ByteBuffer byteBuffer);

    public void send(byte[] bs);
}
