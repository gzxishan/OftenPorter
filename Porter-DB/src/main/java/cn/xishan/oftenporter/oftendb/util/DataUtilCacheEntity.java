package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.db.DBNameValues;
import cn.xishan.oftenporter.porter.core.base.FilterEmpty;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-30.
 */
class DataUtilCacheEntity
{
    static class EField
    {
        Field field;
        String name;
        FilterEmpty filterEmpty;


        public EField(Field field, String name, FilterEmpty filterEmpty)
        {
            field.setAccessible(true);
            this.field = field;
            this.name = name;
            this.filterEmpty = filterEmpty;
        }

        public void append(DBNameValues nameValues, Object obj) throws Throwable
        {
            nameValues.append(name, field.get(obj));
        }
    }

    static class RecursiveEField extends EField{
        DataUtilCacheEntity recursive;

        public RecursiveEField(Field field, String name, FilterEmpty filterEmpty,
                DataUtilCacheEntity recursive)
        {
            super(field, name, filterEmpty);
            this.recursive = recursive;
        }

        @Override
        public void append(DBNameValues nameValues, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            JSONObject json = null;
            if (fieldObj != null)
            {
                DBNameValues nvs = recursive.toDBNameValues(fieldObj, nameValues.isFilterNullAndEmpty(), true);
                json = nvs.toJSON();
            }
            nameValues.append(name, json);
        }
    }

    static class JsonEField extends EField
    {

        public JsonEField(Field field, String name, FilterEmpty filterEmpty)
        {
            super(field, name, filterEmpty);
        }

        @Override
        public void append(DBNameValues nameValues, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            JSONObject json = null;
            if (fieldObj != null)
            {
                JSON.toJSON(fieldObj);
            }
            nameValues.append(name, json);
        }
    }

    private List<EField> fieldList;
    private FilterEmpty filterEmpty;
    private boolean isReady = false;

    public DataUtilCacheEntity(FilterEmpty filterEmpty)
    {
        this.filterEmpty = filterEmpty;
        fieldList = new ArrayList<>();
    }

    public boolean isReady()
    {
        return isReady;
    }

    public void setReady(boolean ready)
    {
        isReady = ready;
    }

    public void addField(EField field)
    {
        fieldList.add(field);
    }

    public DBNameValues toDBNameValues(Object object, boolean parentToEmpty, boolean isExcept,
            String... keyNames) throws Throwable
    {
        boolean isFilterEmpty = !(filterEmpty == FilterEmpty.NO || !parentToEmpty);
        DBNameValues nameValues = new DBNameValues(fieldList.size() / 2 + 1);
        nameValues.filterNullAndEmpty(isFilterEmpty);
        Arrays.sort(keyNames);
        if (keyNames.length > 0)
        {
            if (isExcept)
            {
                for (EField field : fieldList)
                {
                    if (Arrays.binarySearch(keyNames, field.name) >= 0)
                    {
                        continue;
                    }
                    field.append(nameValues, object);
                }
            } else
            {
                for (EField field : fieldList)
                {
                    if (Arrays.binarySearch(keyNames, field.name) >= 0)
                    {
                        field.append(nameValues, object);
                    }
                }
            }
        } else
        {
            for (EField field : fieldList)
            {
                field.append(nameValues, object);
            }
        }
        return nameValues;
    }

}
