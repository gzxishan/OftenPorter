package cn.xishan.oftenporter.servlet.websocket;

/**
 * @author Created by https://github.com/CLovinr on 2019-09-20.
 */
class State
{
    static final State DEFAULT = new State();

    private boolean isConnected = false;

    public boolean isConnected()
    {
        return isConnected;
    }

    public void setConnected(boolean connected)
    {
        isConnected = connected;
    }
}
