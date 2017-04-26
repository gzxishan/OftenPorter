package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-26.
 */
public interface AutoSetDealt
{
    Object deal(Object object,Field field,@MayNull Object fieldValue,String option);
}
