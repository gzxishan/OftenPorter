package cn.xishan.oftenporter.porter.local.porter;

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

    @Override
    public String toString()
    {
        return "id=" + id+","+super.toString();
    }
}
