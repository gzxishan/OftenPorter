package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import javax.servlet.ServletContext;
import javax.websocket.MessageHandler;
import java.lang.annotation.*;

/**
 * <pre>
 * 1.被注解的函数形参是:({@linkplain OftenObject},{@linkplain WS})
 * 2.不具有继承性
 * 3.对于ON_MESSAGE*类型与是否是部分传输方式的对应关系见{@linkplain MessageHandler.Whole}与{@linkplain MessageHandler.Partial}
 * </pre>
 * <pre>
 *     兼容性：基于jsr356。
 * </pre>
 * <p>
 * <strong>注意：</strong>包装后的request和response可能需要实现接口{@linkplain IContainerResource}，用于获取容器本身的对象。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectOperationOfPortIn(handle = WebSocketHandle.class)
public @interface WebSocket
{
    public enum Type
    {
        /**
         * 进行配置，初始化时调用一次。<strong>注意：</strong>是直接调用，即通过{@linkplain OftenObject}获取参数可能会报错。
         */
        ON_CONFIG,
        /**
         * 进行协议升级前。
         */
        ON_CONNECTING,
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

    /**
     * 是否需要{@linkplain Type#ON_CONNECTING}状态，默认false。
     *
     * @return
     */
    boolean needConnectingState() default false;

    int maxBinaryBuffer() default -1;

    int maxTextBuffer() default -1;

    /**
     * 单位毫秒
     */
    long maxIdleTime() default -1;
}
