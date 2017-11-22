package cn.xishan.oftenporter.bridge.http.websocket;


import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectFunOperation(handle = WSClientHandle.class)
public @interface ClientWebSocket
{
    /**
     * 支持返回消息的有：{@linkplain #ON_OPEN},{@linkplain #ON_MESSAGE},{@linkplain #ON_PONG},{@linkplain #ON_BINARY_BYTE_BUFFER}
     */
    public enum Type
    {
        ON_CONFIG,
        ON_OPEN, ON_MESSAGE, ON_PONG, ON_ERROR, ON_CLOSE,
        ON_BINARY_BYTE_BUFFER
    }

    public enum StringType
    {
        STRING, READER
    }

    public enum BinaryType
    {
        /**
         * {@linkplain java.nio.ByteBuffer}
         */
        BYTE_BUFFER,
        /**
         * byte[]
         */
        BYTE_ARRAY,
        /**
         * {@linkplain java.io.InputStream}
         */
        INPUT_STREAM
    }

    /**
     * 是否是部分消息模式，默认false。
     *
     * @return
     */
    boolean isPartial() default false;

    /**
     * 二进制的类型，默认{@linkplain BinaryType#BYTE_BUFFER}
     *
     * @return
     */
    BinaryType binaryType() default BinaryType.BYTE_BUFFER;

    /**
     * 字符串类型，默认{@linkplain StringType#STRING}
     *
     * @return
     */
    StringType stringType() default StringType.STRING;

    boolean autoStart() default true;
}
