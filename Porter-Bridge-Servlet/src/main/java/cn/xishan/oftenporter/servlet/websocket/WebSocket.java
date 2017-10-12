package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;

/**
 * <pre>
 * 1.被注解的函数形参必须是:({@linkplain WObject},{@linkplain WS})
 * 2.不具有继承性
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
        ON_OPEN, ON_MESSAGE, ON_ERROR, ON_CLOSE
    }
}
