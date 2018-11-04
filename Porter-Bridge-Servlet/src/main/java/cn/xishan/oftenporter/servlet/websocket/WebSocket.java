package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

import javax.servlet.ServletContext;
import javax.websocket.MessageHandler;
import java.lang.annotation.*;

/**
 * <pre>
 * 1.被注解的函数形参是:({@linkplain WObject},{@linkplain WS})
 * 2.不具有继承性
 * 3.对于ON_MESSAGE*类型与是否是部分传输方式的对应关系见{@linkplain MessageHandler.Whole}与{@linkplain MessageHandler.Partial}
 * </pre>
 * <pre>
 *     兼容性：
 *     本注解能够实现的核心部分在{@linkplain WebSocketHandle#doConnect(WObject, PorterOfFun)}(  RequestDispatcher
 *     requestDispatcher = request.getRequestDispatcher(XSServletWSConfig.WS_PATH);...requestDispatcher.forward
 *     (request, response);)
 *     1.tomcat7.0.47+、tomcat8.0.x、8.x(未知)、tomcat9（未知）
 *     2.jetty9.4.8.v20171121、其他未知。见{@linkplain WebSocketHandle#handleWS(ServletContext)}
 * </pre>
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
         * 进行配置，初始化时调用一次
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
