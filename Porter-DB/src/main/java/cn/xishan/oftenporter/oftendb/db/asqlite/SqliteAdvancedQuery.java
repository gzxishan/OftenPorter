package cn.xishan.oftenporter.oftendb.db.asqlite;


import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.mysql.SqlUtil;

/**
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class SqliteAdvancedQuery extends AdvancedQuery
{

    SqlUtil.WhereSQL whereSQL;
    String[] keys;

    /**
     * @param whereSQL
     * @param keys     大小为0表示选择全部。
     */
    public SqliteAdvancedQuery(SqlUtil.WhereSQL whereSQL, String... keys)
    {
        this.whereSQL = whereSQL;
        this.keys = keys;
    }

    @Override
    public Object toFinalObject()
    {
        throw new RuntimeException("can not be invoked!");
    }
}
