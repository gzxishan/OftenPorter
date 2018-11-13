package cn.xishan.oftenporter.porter.core.bridge;


/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class BridgeResponse
{
    private Object object;
    private boolean isOk;

    protected BridgeResponse(boolean isOk,Object object)
    {
        this.isOk=isOk;
        this.object = object;
    }

    public boolean isOk()
    {
        return isOk;
    }

    public <T> T getResponse()
    {
        return (T)object;
    }


    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ":" + String.valueOf(object);
    }
}
