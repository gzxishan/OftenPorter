package cn.xishan.oftenporter.porter.core.pbridge;


/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class PResponse
{
    private Object object;
    private boolean isOk;

    protected PResponse(boolean isOk,Object object)
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
