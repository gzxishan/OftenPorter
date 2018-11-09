package cn.xishan.oftenporter.oftendb.db;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuerySettings
{
    protected Integer skip, limit;

    public static class Order
    {
        public String name;
        public int n;

        public Order(String name, int n)
        {
            this.name = name;
            this.n = n;
        }
    }

    public QuerySettings()
    {
    }

    /**
     * settings格式：
     * settings.skip:int
     * settings.limit:int
     * settings.order:[name,-1|0|1,...]
     *
     * @param settings
     */
    public QuerySettings(JSONObject settings)
    {
        this.skip = settings.getInteger("skip");
        this.limit = settings.getInteger("limit");
        JSONArray order = settings.getJSONArray("order");
        if (order != null)
        {
            for (int i = 0; i < order.size(); i += 2)
            {
                String name = order.getString(i);
                int n = order.getIntValue(i + 1);
                if (i == 1 || i == -1)
                {
                    appendOrder(name, n);
                }
            }
        }
    }

    private List<Order> orders = new ArrayList<>();

    public List<Order> getOrders()
    {
        return orders;
    }

    public final QuerySettings setSkip(int skip)
    {
        this.skip = skip;
        return this;
    }

    public final QuerySettings setLimit(int limit)
    {
        this.limit = limit;
        return this;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public Integer getSkip()
    {
        return skip;
    }

    /**
     * @param name
     * @param n    其中1为升序排列，而-1是用于降序排列
     * @return
     */
    public QuerySettings appendOrder(String name, int n)
    {
        orders.add(new Order(name, n));
        return this;
    }

}
