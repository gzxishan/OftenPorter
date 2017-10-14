package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.base.WObject;

import javax.websocket.MessageHandler;
import java.lang.annotation.*;

/**
 * <pre>
 * 1.被注解的函数形参必须是:({@linkplain WObject},{@linkplain WS})
 * 2.不具有继承性
 * 3.对于ON_MESSAGE*类型与是否是部分传输方式的对应关系见{@linkplain MessageHandler.Whole}与{@linkplain MessageHandler.Partial}
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectFunOperation(handle = WebSocketHandle.class)
public @interface WebSocket
{
    public enum Type
    {
        ON_OPEN, ON_MESSAGE, ON_PONG, ON_ERROR, ON_CLOSE,
        ON_BINARY_BYTE_BUFFER, ON_BINARY_BYTE_ARRAY, ON_BINARY_INPUT_STREAM,
        ON_MESSAGE_READER
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
}
