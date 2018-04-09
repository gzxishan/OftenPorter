package cn.xishan.oftenporter.porter.core.annotation.deal;

/**
 * @author Created by https://github.com/CLovinr on 2018/4/6.
 */
public class _MixinPorter
{
    Class<?> clazz;
    Object object;
    boolean override;

    public _MixinPorter(Class<?> clazz, Object object, boolean override)
    {
        this.clazz = clazz;
        this.object = object;
        this.override = override;
    }

    @Override
    public int hashCode()
    {
        return clazz.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof _MixinPorter))
        {
            return false;
        }
        return this.clazz.equals(((_MixinPorter) obj).clazz);
    }

    public Class<?> getClazz()
    {
        return clazz;
    }

    public Object getObject()
    {
        return object;
    }

    public boolean isOverride()
    {
        return override;
    }
}
