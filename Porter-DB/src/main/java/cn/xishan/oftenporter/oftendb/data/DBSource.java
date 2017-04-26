package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;

/**
 * Created by 刚帅 on 2016/1/19.
 */
@AutoSet.AutoSetDefaultDealt(dealt = AutoSetDealtForDBSource.class)
public interface DBSource extends DBHandleSource, ParamsGetter
{
    DBSource withAnotherData(Class<? extends Data> clazz);
    DBSource newInstance();
}
