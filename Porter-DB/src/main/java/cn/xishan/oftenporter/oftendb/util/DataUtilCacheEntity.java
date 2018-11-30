package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.db.DBNameValues;
import cn.xishan.oftenporter.porter.core.base.FilterEmpty;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
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

        public void append(int deep, DBNameValues nameValues, Object obj) throws Throwable
        {
            nameValues.append(name, field.get(obj));
        }

        public void put(int deep, boolean filterEmpty, JSONObject json, Object obj) throws Throwable
        {
            Object value = field.get(obj);
            if (filterEmpty && OftenTool.isEmpty(value))
            {
                return;
            }
            json.put(name, value);
        }
    }

    static class RecursiveEField extends EField
    {
        DataUtilCacheEntity recursive;

        public RecursiveEField(Field field, String name, FilterEmpty filterEmpty,
                DataUtilCacheEntity recursive)
        {
            super(field, name, filterEmpty);
            this.recursive = recursive;
        }

        @Override
        public void append(int deep, DBNameValues nameValues, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            JSONObject json = null;
            if (fieldObj != null)
            {
                json = recursive._toJSON(deep + 1, fieldObj, nameValues.isFilterNullAndEmpty(), true);
            }
            nameValues.append(name, json);
        }

        @Override
        public void put(int deep, boolean filterEmpty, JSONObject json, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            JSONObject _json = null;
            if (fieldObj != null)
            {
                _json = recursive._toJSON(deep + 1, fieldObj, filterEmpty, true);
            }
            if (filterEmpty && OftenTool.isEmpty(_json))
            {
                return;
            }
            json.put(name, _json);
        }
    }

    static class JsonEField extends EField
    {

        public JsonEField(Field field, String name, FilterEmpty filterEmpty)
        {
            super(field, name, filterEmpty);
        }

        @Override
        public void append(int deep, DBNameValues nameValues, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            Object json = null;
            if (fieldObj != null)
            {
                json = JSON.toJSON(fieldObj);
            }
            nameValues.append(name, json);
        }

        @Override
        public void put(int deep, boolean filterEmpty, JSONObject json, Object obj) throws Throwable
        {
            Object fieldObj = field.get(obj);
            Object _json = null;
            if (fieldObj != null)
            {
                _json = JSON.toJSON(fieldObj);
            }
            if (filterEmpty && OftenTool.isEmpty(_json))
            {
                return;
            }
            json.put(name, _json);
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


    public JSONObject toJSON(Object object, boolean parentToEmpty, boolean isExcept,
            String... keyNames) throws Throwable
    {
        return _toJSON(0, object, parentToEmpty, isExcept, keyNames);
    }

    private JSONObject _toJSON(int deep, Object object, boolean parentToEmpty, boolean isExcept,
            String... keyNames) throws Throwable
    {
        if (deep > 64)
        {
            throw new RuntimeException("too deep invoke,may loop ref:"+object);
        }
        boolean isFilterEmpty = !(filterEmpty == FilterEmpty.NO || !parentToEmpty);
        JSONObject jsonObject = new JSONObject(5);
        if (keyNames.length > 0)
        {
            Arrays.sort(keyNames);
            if (isExcept)
            {
                for (EField field : fieldList)
                {
                    if (Arrays.binarySearch(keyNames, field.name) >= 0)
                    {
                        continue;
                    }
                    field.put(deep, isFilterEmpty, jsonObject, object);
                }
            } else
            {
                for (EField field : fieldList)
                {
                    if (Arrays.binarySearch(keyNames, field.name) >= 0)
                    {
                        field.put(deep, isFilterEmpty, jsonObject, object);
                    }
                }
            }
        } else
        {
            for (EField field : fieldList)
            {
                field.put(deep, isFilterEmpty, jsonObject, object);
            }
        }
        return jsonObject;
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
                    field.append(0, nameValues, object);
                }
            } else
            {
                for (EField field : fieldList)
                {
                    if (Arrays.binarySearch(keyNames, field.name) >= 0)
                    {
                        field.append(0, nameValues, object);
                    }
                }
            }
        } else
        {
            for (EField field : fieldList)
            {
                field.append(0, nameValues, object);
            }
        }
        return nameValues;
    }

}
