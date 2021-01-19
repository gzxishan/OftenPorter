package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.servlet.websocket.handle.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class ProgrammaticServer extends Endpoint
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgrammaticServer.class);
    private State state = new State();

    public ProgrammaticServer()
    {
    }

    public void doInvoke(Session session, WebSocket.Type type, boolean isLast, Object value)
    {
        OftenObject oftenObject = null;
        try
        {
            BridgeData bridgeData = (BridgeData) session.getUserProperties().get(BridgeData.class.getName());
            oftenObject = bridgeData.oftenObject;
            PorterOfFun porterOfFun = bridgeData.porterOfFun;
            porterOfFun.invokeByHandleArgs(oftenObject, WS.newWS(state, type, session, isLast, value));
        } catch (SessionException e)
        {
            throw new OftenCallException(e);
        } catch (Throwable e)
        {
            LOGGER.error(e.getMessage(), e);
            try
            {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.getMessage()));
            } catch (IOException ex)
            {
                throw new OftenCallException(ex);
            }
        } finally
        {
            if (oftenObject != null)
            {
                oftenObject.release();
            }
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig)
    {
        state.setConnected(true);
        BridgeData bridgeData = (BridgeData) session.getUserProperties().get(BridgeData.class.getName());
        WebSocket webSocket = bridgeData.webSocket;

        WSConfig wsConfig = bridgeData.wsConfig;
        doInvoke(session, WebSocket.Type.ON_OPEN, true, endpointConfig);

        int maxBinaryBuffer = wsConfig.getMaxBinaryBuffer();
        if (maxBinaryBuffer > 0)
        {
            session.setMaxBinaryMessageBufferSize(maxBinaryBuffer);
        }
        int maxTextBuffer = wsConfig.getMaxTextBuffer();
        if (maxTextBuffer > 0)
        {
            session.setMaxTextMessageBufferSize(maxTextBuffer);
        }
        long maxIdleTime = wsConfig.getMaxIdleTime();
        if (maxIdleTime > 0)
        {
            session.setMaxIdleTimeout(maxIdleTime);
        }

        if (wsConfig.isPartial())
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
        state.setConnected(false);
        doInvoke(session, WebSocket.Type.ON_CLOSE, true, closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr)
    {
        doInvoke(session, WebSocket.Type.ON_ERROR, true, thr);
    }

}
