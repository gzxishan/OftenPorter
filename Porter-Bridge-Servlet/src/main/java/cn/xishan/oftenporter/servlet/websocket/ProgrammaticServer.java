package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.exception.WCallException;

import javax.servlet.http.HttpSession;
import javax.websocket.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
public class ProgrammaticServer extends Endpoint
{

    private void doInvoke(Session session, WebSocket.Type type, Object value)
    {
        try
        {
            HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
            WObject wObject = (WObject) httpSession.getAttribute(WObject.class.getName());
            PorterOfFun porterOfFun = (PorterOfFun) httpSession.getAttribute(PorterOfFun.class.getName());

            porterOfFun.getMethod().invoke(porterOfFun.getPorter().getObj(), wObject, WS.newWS(type,
                    session, value));
        } catch (Exception e)
        {
            throw new WCallException(e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig)
    {
        doInvoke(session, WebSocket.Type.ON_OPEN, null);

        MessageHandler.Whole<String> messageHandler = new MessageHandler.Whole<String>()
        {
            @Override
            public void onMessage(String message)
            {
                doInvoke(session, WebSocket.Type.ON_MESSAGE, message);
            }
        };
        session.addMessageHandler(messageHandler);

    }

    @Override
    public void onClose(Session session, CloseReason closeReason)
    {
        doInvoke(session, WebSocket.Type.ON_CLOSE, closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr)
    {
        doInvoke(session, WebSocket.Type.ON_ERROR, thr);
    }

}
