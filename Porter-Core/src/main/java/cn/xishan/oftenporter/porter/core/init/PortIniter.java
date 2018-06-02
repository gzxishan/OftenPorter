package cn.xishan.oftenporter.porter.core.init;


import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;

/**
 * Created by chenyg on 2018-03-02.
 */
public abstract class PortIniter implements Comparable<PortIniter>
{
    private int order;
    private String key;

    public PortIniter(String key, int order)
    {
        this.key = key;
        this.order = order;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof PortIniter))
        {
            return false;
        }
        return this.key.equals(((PortIniter) obj).key);
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public int compareTo(PortIniter o)
    {
        return this.order - o.order;
    }

    public abstract void init(Delivery delivery) throws InitException;

}
