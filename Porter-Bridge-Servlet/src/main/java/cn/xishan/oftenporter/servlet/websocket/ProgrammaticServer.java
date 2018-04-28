package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.servlet.websocket.handle.*;

import javax.servlet.http.HttpSession;
import javax.websocket.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class ProgrammaticServer extends Endpoint
{

    public void doInvoke(Session session, WebSocket.Type type, boolean isLast, Object value)
    {
        try
        {
            HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
            WObject wObject = (WObject) httpSession.getAttribute(WObject.class.getName());
            PorterOfFun porterOfFun = (PorterOfFun) httpSession.getAttribute(PorterOfFun.class.getName());

            porterOfFun.invoke(wObject, new Object[]{wObject, WS.newWS(type, session, isLast, value)});
        } catch (Exception e)
        {
            throw new WCallException(e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig)
    {
        WSConfig wsConfig = new WSConfig();
        doInvoke(session, WebSocket.Type.ON_OPEN, true, wsConfig);
        HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());

        WebSocket webSocket = (WebSocket) httpSession.getAttribute(WebSocket.class.getName());

        int maxBinaryBuffer = wsConfig.getMaxBinaryBuffer() > 0 ? wsConfig.getMaxBinaryBuffer() : webSocket
                .maxBinaryBuffer();
        if (maxBinaryBuffer > 0)
        {
            session.setMaxBinaryMessageBufferSize(maxBinaryBuffer);
        }
        int maxTextBuffer = wsConfig.getMaxTextBuffer() > 0 ? wsConfig.getMaxTextBuffer() : webSocket.maxTextBuffer();
        if (maxTextBuffer > 0)
        {
            session.setMaxTextMessageBufferSize(maxTextBuffer);
        }
        long maxIdleTime = wsConfig.getMaxIdleTime() > 0 ? wsConfig.getMaxIdleTime() : webSocket.maxIdleTime();
        if (maxIdleTime > 0)
        {
            session.setMaxIdleTimeout(maxIdleTime);
        }

        if (wsConfig.isPartial() || webSocket.isPartial())
        {
            switch (webSocket.stringType())
            {
                case STRING:
                    session.addMessageHandler(new PartialStringHandle(this, session));
                    break;
                default:
                    throw new InitException("illegal StringType for part handle:" + webSocket.stringType());
            }

            switch (webSocket.binaryType())
            {
                case BYTE_BUFFER:
                    session.addMessageHandler(new PartialByteBufferHandle(this, session));
                    break;
                case BYTE_ARRAY:
                    session.addMessageHandler(new PartialByteArrayHandle(this, session));
                    break;
                default:
                    throw new InitException("illegal BinaryType for part handle:" + webSocket.binaryType());
            }
        } else
        {
            switch (webSocket.stringType())
            {
                case STRING:
                    session.addMessageHandler(new WholeStringHandle(this, session));
                    break;
                case READER:
                    session.addMessageHandler(new WholeReaderHandle(this, session));
                    break;
            }

            switch (webSocket.binaryType())
            {
                case BYTE_BUFFER:
                    session.addMessageHandler(new WholeByteBufferHandle(this, session));
                    break;
                case BYTE_ARRAY:
                    session.addMessageHandler(new WholeByteArrayHandle(this, session));
                    break;
                case INPUT_STREAM:
                    session.addMessageHandler(new WholeInputStreamHandle(this, session));
                    break;
            }
        }

        session.addMessageHandler(new WholePongHandle(this, session));

    }

    @Override
    public void onClose(Session session, CloseReason closeReason)
    {
        doInvoke(session, WebSocket.Type.ON_CLOSE, true, closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr)
    {
        doInvoke(session, WebSocket.Type.ON_ERROR, true, thr);
    }

}
