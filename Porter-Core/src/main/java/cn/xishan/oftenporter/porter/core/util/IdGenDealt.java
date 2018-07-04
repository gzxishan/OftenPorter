package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
class IdGenDealt implements AutoSetGen
{
    private static final long FROM_TIME_MILLIS;

    static
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 2 - 1, 16, 00, 00, 00);
        FROM_TIME_MILLIS = calendar.getTimeInMillis();
    }

    @Override
    public Object genObject(Class<?> currentObjectClass, Object currentObject, Field field, Class<?> realFieldType,
            String option)
    {
        return IdGen.getDefault(FROM_TIME_MILLIS);
    }

}
