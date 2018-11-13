package cn.xishan.oftenporter.oftendb.util;

import cn.xishan.oftenporter.oftendb.annotation.TableOptionFilter;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 支持@{@linkplain TableOptionFilter}
 * Created by chenyg on 2017-06-01.
 */
@BindEntityDealt(handle = TableOptionDealt.class)
public class TableOption
{
    public interface IHandle
    {
        void handle(OftenObject oftenObject, TableOption tableOption)throws Exception;
    }

    @Nece
    public JSONObject query;

    /**
     * 有顺序的查询条件[{key:"",value:""},{},...]
     */
    @Unece
    public JSONArray queryArray;

    /**
     * 格式:{limit:int,skip:int,order:[key,intOrder,...]或{key:intOrder,...}}
     */
    @Nece
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
