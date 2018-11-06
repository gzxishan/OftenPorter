package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.util.Arrays;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _Nece extends _NeceUnece
{

    PortMethod[] forMethods;
    String[] forClassTieds;
    String[] forFunTieds;
    boolean toUnece;

    public _Nece()
    {
    }

    public boolean isNece(WObject wObject)
    {
        if(toUnece&&wObject!=null&&isIn(wObject)){
            return false;
        }else{
            return true;
        }
    }

    private boolean isIn(WObject wObject)
    {
        if (Arrays.binarySearch(forMethods, wObject.getRequest().getMethod()) >= 0)
        {
            return true;
        } else if (Arrays.binarySearch(forClassTieds, wObject.url().classTied()) >= 0)
        {
            return true;
        } else if (Arrays.binarySearch(forFunTieds, wObject.url().funTied()) >= 0)
        {
            return true;
        } else
        {
            return false;
        }

    }

    public boolean isToUnece()
    {
        return toUnece;
    }

    public PortMethod[] forMethods()
    {
        return forMethods;
    }

    public String[] forClassTieds()
    {
        return forClassTieds;
    }

    public String[] forFunTieds()
    {
        return forFunTieds;
    }

    void init()
    {
        Arrays.sort(forMethods);
        Arrays.sort(forClassTieds);
        Arrays.sort(forFunTieds);
    }
}
