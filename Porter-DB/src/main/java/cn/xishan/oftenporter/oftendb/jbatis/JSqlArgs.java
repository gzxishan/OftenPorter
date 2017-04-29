package cn.xishan.oftenporter.oftendb.jbatis;

/**
 * Created by chenyg on 2017-04-29.
 */
public class JSqlArgs
{
    public String sql;
    public Object[] args;

    public JSqlArgs(String sql, Object[] args)
    {
        this.sql = sql;
        this.args = args;
    }
}
