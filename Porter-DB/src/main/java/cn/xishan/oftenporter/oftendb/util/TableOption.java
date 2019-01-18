package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.annotation.TableOptionFilter;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.function.Predicate;

/**
 * 支持@{@linkplain TableOptionFilter}
 * Created by chenyg on 2017-06-01.
 */
@BindEntityDealt(handle = TableOptionDealt.class)
public class TableOption
{
    public interface IHandle
    {
        void handle(OftenObject oftenObject, TableOption tableOption) throws Exception;
    }

    @Unece
    public JSONObject query;

    /**
     * 有顺序的查询条件[{key:"",value:""},{},...]
     */
    @Unece
    public JSONArray queryArray;

    /**
     * 格式:{limit:int,skip:int,order:[key,intOrder,...]或{key:intOrder,...}}
     */
    @Unece
    public JSONObject settings;

    private static IHandle iHandle;

    public static IHandle getDefaultHandle()
    {
        return iHandle;
    }

    public static void setDefaultHandle(IHandle iHandle)
    {
        TableOption.iHandle = iHandle;
    }

    public TableOption()
    {
    }


    public TableOption(JSONArray queryArray, JSONObject settings)
    {
        this.queryArray = queryArray;
        this.settings = settings;
    }

    public TableOption(JSONObject query, JSONObject settings)
    {
        this.query = query;
        this.settings = settings;
    }


    public TableOption setSkip(int skip)
    {
        if (settings == null)
        {
            settings = new JSONObject();
        }
        settings.put("skip", skip);
        return this;
    }

    public TableOption setLimit(int limit)
    {
        if (settings == null)
        {
            settings = new JSONObject();
        }
        settings.put("limit", limit);
        return this;
    }

    public JSONArray getOrder()
    {
        if (settings == null)
        {
            return null;
        } else
        {
            return settings.getJSONArray("order");
        }
    }

    public TableOption addOrder(String key, int order)
    {
        if (settings == null)
        {
            settings = new JSONObject();
        }

        Object object = settings.get("order");
        if (object == null)
        {
            object = new JSONArray();
            settings.put("order", object);
        }

        if (object instanceof JSONObject)
        {
            JSONObject orderJson = (JSONObject) object;
            orderJson.put(key, object);
        } else
        {
            JSONArray orderArray = (JSONArray) object;
            orderArray.add(key);
            orderArray.add(order);
        }
        return this;
    }

    /**
     * 另见{@linkplain SimpleSqlUtil#toQueryArray(Object...)}.
     *
     * @param nameValues
     * @return
     */
    public TableOption add2QueryArray(Object... nameValues)
    {
        JSONArray array = SimpleSqlUtil.getInstance().toQueryArray(nameValues);
        if (queryArray == null)
        {
            queryArray = new JSONArray();
        }
        queryArray.addAll(array);
        return this;
    }

    public TableOption add2QueryArray(String key, Object value)
    {
        JSONObject json = new JSONObject(2);
        json.put("key", key);
        json.put("value", value);
        if (queryArray == null)
        {
            queryArray = new JSONArray();
        }
        queryArray.add(json);
        return this;
    }


    public String getQueryValue(String key)
    {
        return getQueryValue(key, null);
    }

    public String getQueryValue(String key, String defaultValue)
    {
        String value = null;
        if (query != null && query.containsKey(key))
        {
            value = query.getString(key);
        } else if (queryArray != null)
        {
            for (int i = 0; i < queryArray.size(); i++)
            {
                JSONObject json = queryArray.getJSONObject(i);
                if (key.equals(json.getString("key")))
                {
                    value = json.getString("value");
                    break;
                }
            }
        }
        if (OftenTool.isEmpty(value))
        {
            value = defaultValue;
        }
        return value;
    }

    /**
     * 替换所有指定key的查询条件，如果不存在、最好会添加
     *
     * @param key
     * @param value
     * @return
     */
    public TableOption replaceQueryValue(String key, Object value)
    {
        boolean replaced = false;
        if (query != null && query.containsKey(key))
        {
            query.put(key, value);
            replaced = true;
        }
        if (queryArray != null)
        {
            for (int i = 0; i < queryArray.size(); i++)
            {
                JSONObject json = queryArray.getJSONObject(i);
                if (key.equals(json.getString("key")))
                {
                    json.put("value", value);
                    replaced = true;
                }
            }
        }
        if (!replaced)
        {
            add2QueryArray(key, value);
        }
        return this;
    }

    /**
     * 移除指定key的查询条件
     *
     * @param key
     */
    public TableOption removeQuery(String key)
    {
        if (query != null)
        {
            query.remove(key);
        }

        if (queryArray != null)
        {
            queryArray.removeIf(o -> {
                JSONObject json = (JSONObject) o;
                return key.equals(json.getString("key"));
            });
        }
        return this;
    }

    public JSONArray getQueryArray()
    {
        return queryArray;
    }

    public JSONObject getQuery()
    {
        return query;
    }

    public void setQuery(JSONObject query)
    {
        this.query = query;
    }

    public JSONObject getSettings()
    {
        return settings;
    }

    public void setSettings(JSONObject settings)
    {
        this.settings = settings;
    }

    /**
     * 返回结果例子：
     * <pre>
     * {
     *     code:0,
     *     rs:{
     *         total:1000,
     *         skip:0,
     *         limit:30,
     *         data:[{},{},{}]
     *     }
     * }
     * </pre>
     *
     * @param jResponse 结果为数组或list
     * @param total     数据总条数
     * @param skip      跳过的条数
     * @param limit     当前页最大数
     * @return
     */
    public static JResponse toTableData(JResponse jResponse, Long total, int skip, int limit)
    {
        if (total != null && jResponse.isSuccess())
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("total", total);
            jsonObject.put("skip", skip);
            jsonObject.put("limit", limit);
            jsonObject.put("data", jResponse.getResult());
            jResponse.setResult(jsonObject);
        }
        return jResponse;
    }

    /**
     * 见{@linkplain #toTableData(JResponse, Long, int, int)}
     *
     * @param data  数据
     * @param total 总条数
     * @param skip  跳过的条数
     * @param limit 当前页最大条数
     * @return
     */
    public static JResponse toTableData(List data, Long total, int skip, int limit)
    {
        JResponse jResponse = new JResponse(ResultCode.SUCCESS);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", total);
        jsonObject.put("skip", skip);
        jsonObject.put("limit", limit);
        jsonObject.put("data", data);
        jResponse.setResult(jsonObject);

        return jResponse;
    }
}
