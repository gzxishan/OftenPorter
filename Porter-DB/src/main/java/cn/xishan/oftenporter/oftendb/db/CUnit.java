package cn.xishan.oftenporter.oftendb.db;

public class CUnit
{
    protected String param1;
    protected Object param2;
    /**
     * 是否为值。有特殊用途，默认为false。若为true，则进行处理，否则直接添加到相关语句.
     */
    private boolean isParam1Value = false;
    /**
     * 是否为值。有特殊用途，默认为true。若为true，则进行处理，否则直接添加到相关语句.
     */
    private boolean isParam2Value = true;

    public CUnit(String param1, Object param2)
    {
        this.param1 = param1;
        this.param2 = param2;
    }


    /**
     * 设置参数1是否为值：true为值，false为数据库键（列）名称。
     *
     * @param isParam1Value
     */
    public void setParam1Value(boolean isParam1Value)
    {
        this.isParam1Value = isParam1Value;
    }

    /**
     * 设置参数2是否为值：true为值，false为数据库键（列）名称。
     *
     * @param isParam2Value
     */
    public void setParam2Value(boolean isParam2Value)
    {
        this.isParam2Value = isParam2Value;
    }

    public boolean isParam1Value()
    {
        return isParam1Value;
    }

    public boolean isParam2Value()
    {
        return isParam2Value;
    }

    public void setParam1(String param1)
    {
        this.param1 = param1;
    }


    public void setParam2(Object param2)
    {
        this.param2 = param2;
    }

    public String getParam1()
    {
        return param1;
    }

    public Object getParam2()
    {
        return param2;
    }
}
