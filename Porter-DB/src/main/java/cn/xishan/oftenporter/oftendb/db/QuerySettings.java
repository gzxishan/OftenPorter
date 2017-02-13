package cn.xishan.oftenporter.oftendb.db;



public abstract class QuerySettings implements ToFinal
{
    protected Integer skip, limit;

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
     * 
     * @param name
     * @param n 其中1为升序排列，而-1是用于降序排列
     * @return
     */
    public abstract QuerySettings putOrder(String name, int n);

    /**
     * 处理字段(含有@Key注解的).此方法使用者无需调用.
     * 
     * @param c 用于Field查找的类
     */
    public abstract void _dealNames(Class<?> c);

    

    /**
     * 转换为最终的对象
     * 
     * @return
     */
    public abstract Object toFinalObject();
}
