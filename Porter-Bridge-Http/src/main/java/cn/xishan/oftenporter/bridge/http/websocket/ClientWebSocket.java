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
     * 支持返回消息的有：{@linkplain #ON_OPEN},{@linkplain #ON_MESSAGE},{@linkplain #ON_PING},{@linkplain #ON_PONG},
     * {@linkplain #ON_BINARY_BYTE_BUFFER}
     */
    public enum Type
    {
        /**
         * 需要返回{@linkplain WSClientConfig}配置。
         */
        ON_CONFIG,
        ON_OPEN, ON_MESSAGE,
        ON_PONG, ON_PING,
        ON_ERROR, ON_CLOSE,
        ON_BINARY_BYTE_BUFFER
    }

    boolean autoStart() default true;

    /**
     * 自动开始的次数
     *
     * @return
     */
    int startCount() default 1;
}
