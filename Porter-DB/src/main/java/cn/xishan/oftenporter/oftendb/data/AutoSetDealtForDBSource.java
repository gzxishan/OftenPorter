package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.annotation.CustomerToDo;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.lang.reflect.Field;

/**
 * Created by chenyg on 2017-04-26.
 */
public class AutoSetDealtForDBSource implements AutoSetDealt
{

    @Override
    public Object deal(Object finalObject, Class<?> currentObjectClass, Object currentObject, Field field,
            Object fieldValue, String option) throws Exception
    {
        if (fieldValue == null)
        {
            return null;
        }
        DBSource dbSource = (DBSource) fieldValue;


        CustomerToDo customerToDo = AnnoUtil.getAnnotation(currentObjectClass, CustomerToDo.class);
        if (customerToDo != null && (WPTool.notNullAndEmpty(customerToDo.tableName()) || !ConfigToDo.class
                .equals(customerToDo.todo())))
        {
            if (!customerToDo.todo().equals(ConfigToDo.class))
            {
                ConfigToDo configToDo = WPTool.newObject(customerToDo.todo());
                dbSource = dbSource.newInstance(configToDo);
            } else if (WPTool.notNullAndEmpty(customerToDo.tableName()))
            {
                dbSource = dbSource.newInstance((wObject, configed, configing) -> {
                    configing.setCollectionName(customerToDo.tableName());
                });
            }
        } else
        {
            dbSource = dbSource.newInstance();
        }
        setUnit(finalObject, dbSource);
        return dbSource;
    }

    public static void setUnit(Object object, DBSource dbSource)
    {
        dbSource.getConfiged().setUnit(object);
    }
}
