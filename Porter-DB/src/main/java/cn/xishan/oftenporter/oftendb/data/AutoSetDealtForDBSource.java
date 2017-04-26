package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;

import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-26.
 */
class AutoSetDealtForDBSource implements AutoSetDealt
{
    @Override
    public Object deal(Object object, Field field, @MayNull Object fieldValue, String option)
    {
        if (fieldValue == null)
        {
            return null;
        }
        DBSource dbSource = (DBSource) fieldValue;
        dbSource = dbSource.newInstance();
        if (object instanceof DataAble)
        {
            dbSource.getParams().set((DataAble) object);
        } else
        {
            dbSource.getParams().setUnit(object);
        }
        return dbSource;
    }
}
