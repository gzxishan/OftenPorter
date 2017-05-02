package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;

import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-26.
 */
public class AutoSetDealtForDBSource implements AutoSetDealt
{
    @Override
    public Object deal(Object finalObject,Object object, Field field, @MayNull Object fieldValue, String option)
    {
        if (fieldValue == null)
        {
            return null;
        }
        DBSource dbSource = (DBSource) fieldValue;
        dbSource = dbSource.newInstance();
        setUnit(finalObject,dbSource);
        return dbSource;
    }

    public static void setUnit(Object object,DBSource dbSource){
        if (object instanceof DataAble)
        {
            dbSource.getParams().set((DataAble) object);
        } else
        {
            dbSource.getParams().setUnit(object);
        }
    }
}
