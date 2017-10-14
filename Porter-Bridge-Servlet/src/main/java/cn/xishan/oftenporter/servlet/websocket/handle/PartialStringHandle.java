package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class PartialStringHandle extends BaseHandle implements MessageHandler.Partial<String>
{


    public PartialStringHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(String partialMessage, boolean last)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_MESSAGE, last, partialMessage);
    }
}
