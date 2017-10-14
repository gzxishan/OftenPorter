package cn.xishan.oftenporter.servlet.websocket.handle;

import cn.xishan.oftenporter.servlet.websocket.ProgrammaticServer;

import javax.websocket.Session;

/**
 * @author Created by https://github.com/CLovinr on 2017/10/14.
 */
public abstract class BaseHandle
{
    protected Session session;
    protected ProgrammaticServer programmaticServer;

    public BaseHandle(ProgrammaticServer programmaticServer, Session session)
    {
        this.programmaticServer = programmaticServer;
        this.session = session;
    }
}
