package cn.xishan.oftenporter.demo.oftendb.base;

import cn.xishan.oftenporter.oftendb.data.DataDynamic;
import cn.xishan.oftenporter.oftendb.data.ParamsGetter;

/**
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
public class ParamsGetterImpl implements ParamsGetter
{
    private Params params;

    public ParamsGetterImpl()
    {
        params = new Params(new DataDynamic(), (wObject, dataAble,unit) -> dataAble.setCollectionName("test1"));
    }

    @Override
    public Params getParams()
    {
        return params;
    }
}
