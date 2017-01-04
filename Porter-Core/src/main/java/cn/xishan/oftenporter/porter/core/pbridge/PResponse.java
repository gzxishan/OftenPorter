package cn.xishan.oftenporter.porter.core.pbridge;


/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class PResponse
{
    private Object object;

    protected PResponse(Object object)
    {
        this.object = object;
    }

    public Object getResponse()
    {
        return object;
    }


    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ":" + String.valueOf(object);
    }
}
