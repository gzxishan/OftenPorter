package cn.xishan.oftenporter.oftendb.db;


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
    public QuerySettings putOrder(String name, int n)
    {
        orders.add(new Order(name, n));
        return this;
    }

}
