package cn.xishan.oftenporter.porter.core.annotation.sth;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
class Walk
{
    Class<?> clazz1, clazz2;

    public Walk(Class<?> clazz1, Class<?> clazz2)
    {
        this.clazz1 = clazz1;
        this.clazz2 = clazz2;
    }

    @Override
    public int hashCode()
    {
        return clazz1.hashCode() + clazz2.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Walk))
        {
            return false;
        }
        Walk walk = (Walk) obj;

        return walk.clazz1.equals(clazz1) && walk.clazz2.equals(clazz2);
    }
}
