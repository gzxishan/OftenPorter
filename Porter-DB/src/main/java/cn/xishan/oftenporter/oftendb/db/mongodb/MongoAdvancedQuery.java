package cn.xishan.oftenporter.oftendb.db.mongodb;

import cn.xishan.oftenporter.oftendb.db.AdvancedQuery;
import cn.xishan.oftenporter.oftendb.db.DBException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBCollection;

import java.util.List;

/**
 * Created by 宇宙之灵 on 2015/9/23.
 */
public abstract class MongoAdvancedQuery extends AdvancedQuery
{
    protected abstract JSONArray execute(DBCollection collection, MongoHandle mongoHandle) throws DBException;

}
