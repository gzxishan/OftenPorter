package cn.xishan.oftenporter.oftendb.db.mongodb;

import cn.xishan.oftenporter.oftendb.db.AdvancedExecutor;
import cn.xishan.oftenporter.oftendb.db.DBException;
import com.mongodb.DBCollection;

public abstract class MongoAdvancedExecutor extends AdvancedExecutor
{

    protected abstract Object execute(DBCollection collection,MongoHandle mongoHandle) throws DBException;

}
