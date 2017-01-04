package cn.xishan.oftenporter.oftendb.db.mongodb.advanced;


import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoAdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoHandle;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


/**
 * 结果返回的是影响的条数
 *
 * @author ZhuiFeng
 */
public class UpdateAdvancedExecutor extends MongoAdvancedExecutor
{
    private DBObject query, update;
    private boolean upsert, multi;

    @Override
    public Object toFinalObject()
    {
        return null;
    }

    public void update(DBObject query, DBObject update, boolean upsert, boolean multi)
    {
        this.query = query;
        this.update = update;
        this.upsert = upsert;
        this.multi = multi;
    }

    @Override
    protected Integer execute(DBCollection collection, MongoHandle mongoHandle) throws DBException
    {
        int n = 0;
        try
        {
            n = collection.update(query, update, upsert, multi).getN();
        } catch (Exception e)
        {
            throw new DBException(e);
        }
        return n;
    }

}
