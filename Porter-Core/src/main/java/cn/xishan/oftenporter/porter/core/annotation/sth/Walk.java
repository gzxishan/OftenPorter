package cn.xishan.oftenporter.porter.core.annotation.sth;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
class Walk<T>
{
    T t1, t2;

    public Walk(T t1, T t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public int hashCode()
    {
        return t1.hashCode() + t2.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Walk))
        {
            return false;
        }
        Walk walk = (Walk) obj;

        return walk.t1.equals(t1) && walk.t2.equals(t2);
    }
}
