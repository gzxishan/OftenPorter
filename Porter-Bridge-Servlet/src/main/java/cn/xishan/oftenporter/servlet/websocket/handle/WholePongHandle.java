package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;
import cn.xishan.oftenporter.servlet.websocket.WebSocket;

import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public class WholePongHandle extends BaseHandle implements MessageHandler.Whole<PongMessage>
{
    public WholePongHandle(ProgrammaticServer programmaticServer,
            Session session)
    {
        super(programmaticServer, session);
    }

    @Override
    public void onMessage(PongMessage message)
    {
        programmaticServer.doInvoke(session, WebSocket.Type.ON_PONG,true,message,true);
    }
}
