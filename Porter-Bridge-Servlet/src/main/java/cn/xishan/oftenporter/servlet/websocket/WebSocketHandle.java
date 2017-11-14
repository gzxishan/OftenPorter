package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class WebSocketHandle implements AspectFunOperation.Handle<WebSocket>
{
    private PorterOfFun porterOfFun;
    private WebSocket webSocket;

    @Override
    public boolean init(WebSocket webSocket, PorterOfFun porterOfFun)
    {
        this.webSocket = webSocket;
        this.porterOfFun = porterOfFun;
        return true;
    }

    @Override
    public boolean init(WebSocket current, Porter porter)
    {
        return false;
    }

    @Override
    public Object invokeMethod(WObject wObject, PorterOfFun porterOfFun, Object lastReturn) throws Exception
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(XSServletWSConfig.WS_PATH);
        HttpSession session = request.getSession();

        session.setAttribute(WObject.class.getName(), wObject);
        session.setAttribute(PorterOfFun.class.getName(), porterOfFun);
        session.setAttribute(WebSocket.class.getName(), webSocket);

        requestDispatcher.forward(request, response);
        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }
}
