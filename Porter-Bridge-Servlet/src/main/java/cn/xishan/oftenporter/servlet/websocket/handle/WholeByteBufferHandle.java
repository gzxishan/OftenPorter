package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class WholeByteBufferHandle extends BaseHandle implements MessageHandler.Whole<ByteBuffer>
{
    public WholeByteBufferHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(ByteBuffer message)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_BINARY_BYTE_BUFFER, true, message);
    }
}
