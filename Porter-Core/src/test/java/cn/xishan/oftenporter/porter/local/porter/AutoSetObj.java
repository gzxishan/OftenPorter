package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class AutoSetObj
{
    private static AtomicInteger count = new AtomicInteger(0);
    private int id;

    public AutoSetObj()
    {
        id = count.getAndIncrement();
    }

    public int getId()
    {
        return id;
    }

    @AutoSet.SetOk
    public void setOk(WObject wObject){
        LogUtil.printErrPos(wObject);
    }

    @AutoSet.SetOk
    public static void setOkStatic(){
        LogUtil.printErrPos();
    }

    @Override
    public String toString()
    {
        return "id=" + id+","+super.toString();
    }
}
