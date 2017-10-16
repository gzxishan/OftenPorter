package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class PartialByteBufferHandle extends BaseHandle implements MessageHandler.Partial<ByteBuffer>
{


    public PartialByteBufferHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(ByteBuffer partialMessage, boolean last)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_BINARY_BYTE_BUFFER, last, partialMessage);
    }
}
