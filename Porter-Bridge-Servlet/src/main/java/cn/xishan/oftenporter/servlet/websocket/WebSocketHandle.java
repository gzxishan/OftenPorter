package cn.xishan.oftenporter.servlet.websocket;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/12.
 */
class WebSocketHandle implements AspectFunOperation.Handle
{
    private PorterOfFun porterOfFun;

    @Override
    public boolean initWith(PorterOfFun porterOfFun)
    {
        this.porterOfFun = porterOfFun;
        return true;
    }

    @Override
    public Object invoke(WObject wObject,Object lastReturn)throws Exception
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        HttpServletResponse response = wObject.getRequest().getOriginalResponse();
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(XSServletWSConfig.WS_PATH);
        request.getSession().setAttribute(WObject.class.getName(), wObject);
        request.getSession().setAttribute(PorterOfFun.class.getName(), porterOfFun);
        requestDispatcher.forward(request, response);
        return null;
    }

    @Override
    public OutType getOutType()
    {
        return OutType.NO_RESPONSE;
    }
}
