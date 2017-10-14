package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class WholeInputStreamHandle extends BaseHandle implements MessageHandler.Whole<InputStream>
{
    public WholeInputStreamHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(InputStream message)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_BINARY_INPUT_STREAM, true, message);
    }
}
