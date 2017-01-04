package cn.xishan.oftenporter.porter.core.base;

/**
 * @author Created by https://github.com/CLovinr on 2016/12/27.
 */
public class Aspect
{
    public Object returnObj;
    public Throwable invokeCause;

    public Aspect(Object returnObj)
    {
        this.returnObj = returnObj;
    }

    public Aspect(Throwable invokeCause)
    {
        this.invokeCause = invokeCause;
    }
}
