package cn.xishan.oftenporter.bridge.http.websocket;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
public interface Session
{
    /**
     * 调用后将不会连接。
     */
    void close(int code, String reason);

    /**
     * 调用后将不会连接。
     */
    void close();

    boolean isClosed();

    void sendPing(ByteBuffer applicationData);

    void sendPong(ByteBuffer applicationData);

    void send(String text);

    void send(ByteBuffer byteBuffer);

    void send(byte[] bs);

    String getId();

    Map<String, Object> getUserProperties();
}
