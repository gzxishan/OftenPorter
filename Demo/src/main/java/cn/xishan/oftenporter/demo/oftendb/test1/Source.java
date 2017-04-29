package cn.xishan.oftenporter.demo.oftendb.test1;

import cn.xishan.oftenporter.demo.oftendb.base.ParamsGetterImpl;
import cn.xishan.oftenporter.demo.oftendb.base.SqlDBSource;
import cn.xishan.oftenporter.oftendb.data.impl.DBSourceImpl;

/**
 * Created by chenyg on 2017-04-29.
 */
public class Source extends DBSourceImpl
{

    public Source()
    {
        super(new ParamsGetterImpl().getParams(), new SqlDBSource());
    }
}