package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.Reader;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class WholeReaderHandle extends BaseHandle implements MessageHandler.Whole<Reader>
{
    public WholeReaderHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(Reader message)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_MESSAGE_READER,true,message);
    }
}
