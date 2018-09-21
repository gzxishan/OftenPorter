package cn.xishan.oftenporter.porter.core.annotation.sth;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/22.
 */
class ObjectGetterImpl implements ObjectGetter
{
    private Porter porter;

    public ObjectGetterImpl(Porter porter)
    {
        this.porter = porter;
    }

    @Override
    public Object getObject()
    {
        return porter.getObj();
    }
}
