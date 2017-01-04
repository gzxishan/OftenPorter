package cn.xishan.oftenporter.oftendb.db.mongodb.advanced;

import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoAdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.mongodb.MongoHandle;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Created by 宇宙之灵 on 2016/5/10.
 */
public class AddAdvancedExecutor extends MongoAdvancedExecutor
{
    private DBObject[] dbObjects;

    public void insert(DBObject... dbObjects)
    {
        this.dbObjects = dbObjects;
    }

    @Override
    protected Object execute(DBCollection collection, MongoHandle mongoHandle) throws DBException
    {
        try
        {
            collection.insert(dbObjects);
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public Object toFinalObject()
    {
        return null;
    }
}
