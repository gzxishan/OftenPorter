package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class WebSocketHandle extends AspectFunOperation.HandleAdapter<WebSocket>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandle.class);
    private WebSocket webSocket;

    @Override
    public boolean init(WebSocket webSocket, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        return true;
    }


    @Override
    public Object invoke(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        if (webSocket.needConnectingState())
        {
            WS ws = WS.newWS(WebSocket.Type.ON_CONNECTING, null, true, (Connecting) will -> {
                try
                {
                    if (will)
                    {
                        doConnect(wObject, porterOfFun);
                    } else
                    {
                        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
                        response.getWriter().close();
                    }
                } catch (Exception e)
                {
                    LOGGER.debug(e.getMessage(), e);
                }

            });
            porterOfFun.invoke(wObject, new Object[]{wObject, ws});
        } else
        {
            doConnect(wObject, porterOfFun);
        }


        return null;
    }

    private void doConnect(WObject wObject, PorterOfFun porterOfFun) throws ServletException, IOException
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(XSServletWSConfig.WS_PATH);
        HttpSession session = request.getSession();

        session.setAttribute(WObject.class.getName(), wObject);
        session.setAttribute(PorterOfFun.class.getName(), porterOfFun);
        session.setAttribute(WebSocket.class.getName(), webSocket);

        requestDispatcher.forward(request, response);
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }
}
