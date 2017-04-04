package cn.xishan.oftenporter.oftendb.db.mongodb;

import cn.xishan.oftenporter.oftendb.db.*;
import cn.xishan.oftenporter.oftendb.db.mongodb.advanced.QueryAdvanced;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MongoHandle implements DBHandle
{

    private DBCollection collection;

    public MongoHandle(DB db, String collectionName)
    {

        this.collection = db.getCollection(collectionName);
    }


    @Override
    public void setLogger(Logger logger)
    {

    }

    /**
     * @param toFinal
     * @return 若参数为null，则返回null；若参数不为{@linkplain MongoCondition}类型，则抛出异常。
     */
    public static DBObject checkToFinal(ToFinal toFinal)
    {
        if (toFinal == null)
        {
            return null;
        } else if ((toFinal instanceof MongoCondition) || (toFinal instanceof MongoQuerySettings))
        {
            Object object = toFinal.toFinalObject();

            return (DBObject) object;

        } else
        {
            throw new DBException("The current type " + toFinal.getClass()
                    + " is not accept!");
        }
    }

    private static final String MULTI_INSERT_FUN = "function multiInsert(as,coll){" +
            "     var rs=new Array(as.length);" +
            "var one=NumberInt(1),zero=NumberInt(0);" +
            " for(var i=0;i<as.length;i++)" +
            " {" +
            "var x =db[coll].insert(as[i],{w:2,fsyn:true,wtimeout:0});" +
            " if(x.nInserted==1){" +
            "  rs[i]=(one);" +
            "  }else{" +
            " rs[i]=(zero);" +
            " }" +

            " }" +

            "   return rs;" +
            " }";

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException
    {
        try
        {
            BasicDBList dbList = Util.toDBList(multiNameValues);
            dbList = (BasicDBList) MongodbUtil
                    .eval(collection.getDB(), MULTI_INSERT_FUN, true, dbList, collection.getName());
            int[] rs = new int[dbList.size()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = (Integer) dbList.get(i);
            }
            return rs;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public boolean add(NameValues nameValues) throws DBException
    {
        try
        {
            collection.insert(Util.toDbObject(nameValues));
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public boolean replace(Condition query, NameValues nameValues) throws DBException
    {
        try
        {

            int n = _replace(query, nameValues, true);
            return n > 0;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    private int _replace(Condition query, NameValues nameValues, boolean upsert) throws DBException
    {
        try
        {

            int n = collection
                    .update(checkToFinal(query), new BasicDBObject("$set", Util.toDbObject(nameValues)), upsert, false)
                    .getN();
            return n;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public int del(Condition query) throws DBException
    {
        try
        {
            DBObject _query = checkToFinal(query);
            int n = collection.remove(_query).getN();
            return n;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    private DBCursor dealQuerySettings(DBCursor cursor, QuerySettings querySettings)
    {
        if (querySettings != null)
        {
            cursor = cursor.sort(checkToFinal(querySettings));
            if (querySettings.getSkip() != null)
            {
                cursor = cursor.skip(querySettings.getSkip());
            }
            if (querySettings.getLimit() != null)
            {
                cursor = cursor.limit(querySettings.getLimit());
            }

        }
        return cursor;
    }

    @Override
    public JSONArray getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException
    {

        DBCursor cursor = null;
        try
        {
            JSONArray list = new JSONArray();
            BasicDBObject fields = null;
            if (keys != null && keys.length != 0)
            {
                fields = new BasicDBObject();
                for (int i = 0; i < keys.length; i++)
                {
                    fields.put(keys[i], 1);
                }
            }

            cursor = dealQuerySettings(collection.find(checkToFinal(query), fields), querySettings);

            for (DBObject dbObject : cursor)
            {
                list.add(getJSONObject(dbObject, keys));
            }
            return list;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(cursor);
        }
    }

    @Override
    public DBEnumeration<JSONObject> getDBEnumerations(Condition query, QuerySettings querySettings,
            String... keys) throws DBException
    {
        try
        {
            BasicDBObject fields = null;
            if (keys != null && keys.length != 0)
            {
                fields = new BasicDBObject();
                for (int i = 0; i < keys.length; i++)
                {
                    fields.put(keys[i], 1);
                }
            }

            DBCursor cursor = dealQuerySettings(collection.find(checkToFinal(query), fields), querySettings);

            DBEnumeration<JSONObject> enumeration = new DBEnumeration<JSONObject>()
            {
                @Override
                public boolean hasMoreElements() throws DBException
                {

                    try
                    {
                        return cursor.hasNext();
                    } catch (Exception e)
                    {
                        throw new DBException(e);
                    }
                }

                @Override
                public JSONObject nextElement() throws DBException
                {
                    if (!hasMoreElements())
                    {
                        throw new DBException("no more elements!");
                    }
                    try
                    {
                        JSONObject jsonObject = getJSONObject(cursor.next(), keys);
                        return jsonObject;
                    } catch (Exception e)
                    {
                        throw new DBException(e);
                    }
                }

                @Override
                public void close() throws DBException
                {
                    WPTool.close(cursor);
                }
            };

            return enumeration;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    private JSONObject getJSONObject(DBObject dbObject, String[] keys) throws JSONException
    {

        JSONObject jsonObject;
        if (keys == null || keys.length == 0)
        {
            Set<Map.Entry<String, Object>> set = dbObject.toMap().entrySet();
            jsonObject = new JSONObject(set.size());
            for (Map.Entry<String, Object> entry : set)
            {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        } else
        {
            jsonObject = new JSONObject(keys.length);
            for (String string : keys)
            {
                jsonObject.put(string, dbObject.get(string));
            }
        }

        return jsonObject;
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException
    {
        try
        {
            BasicDBObject fields = null;
            if (keys != null && keys.length != 0)
            {
                fields = new BasicDBObject();
                for (int i = 0; i < keys.length; i++)
                {
                    fields.put(keys[i], 1);
                }
            }

            DBObject dbObject = collection.findOne(checkToFinal(query), fields);

            if (dbObject != null)
            {

                return getJSONObject(dbObject, keys);
            } else
            {
                return null;
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }


    @Override
    public JSONArray get(Condition query, QuerySettings querySettings, String key) throws DBException
    {
        DBCursor cursor = null;
        try
        {
            cursor = dealQuerySettings(collection.find(checkToFinal(query), new BasicDBObject(key, 1)), querySettings);
            JSONArray list = new JSONArray(cursor.count());
            for (DBObject object : cursor)
            {
                list.add(object.get(key));
            }
            return list;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(cursor);
        }

    }

    @Override
    public int update(Condition query, NameValues nameValues) throws DBException
    {
        return this._replace(query, nameValues, false);
    }

    @Override
    public long exists(Condition query) throws DBException
    {

        DBCursor cursor = null;

        try
        {
            DBObject dbObject = checkToFinal(query);
            if (dbObject == null || dbObject.keySet().isEmpty())
            {
                return collection.count();
            }
            DBObject keys = new BasicDBObject("_id", 1);
            cursor = collection.find(dbObject, keys);
            int c = cursor.count();
            return c;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(cursor);
        }

    }

    @Override
    public boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException
    {
        try
        {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(data, offset, length);
            DBObject dbObject = new BasicDBObject(name, bos.toByteArray());
            int n = collection.update(checkToFinal(query), new BasicDBObject("$set", dbObject), false, true).getN();
            return n > 0;

        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public void close() throws IOException
    {

    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException
    {
        DBObject dbObject = collection.findOne(checkToFinal(query));
        byte[] bs = null;
        if (dbObject != null)
        {
            Object object = dbObject.get(name);

            if (object != null)
            {
                if (object instanceof byte[])
                {
                    bs = (byte[]) object;
                } else
                {
                    bs = object.toString().getBytes();
                }
            }
        }
        return bs;
    }

    @Override
    public JSONArray advancedQuery(AdvancedQuery advancedQuery) throws DBException
    {
        if (!(advancedQuery instanceof MongoAdvancedQuery))
        {
            throw new DBException("the object must be " + QueryAdvanced.class);
        }
        MongoAdvancedQuery mongoAdvancedQuery = (MongoAdvancedQuery) advancedQuery;
        return mongoAdvancedQuery.execute(collection, this);
    }


    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException
    {
        if (!(advancedExecutor instanceof MongoAdvancedExecutor))
        {
            throw new DBException("the object must be " + MongoAdvancedExecutor.class);
        }
        MongoAdvancedExecutor mongoAdvancedNeed = (MongoAdvancedExecutor) advancedExecutor;
        return mongoAdvancedNeed.execute(collection, this);
    }

    @Override
    public boolean supportTransaction() throws DBException
    {
        return false;
    }

    @Override
    public void startTransaction() throws DBException
    {

    }

    @Override
    public void commitTransaction() throws DBException
    {

    }

    @Override
    public boolean isTransaction()
    {
        return false;
    }

    @Override
    public void rollback() throws DBException
    {

    }

    private Object tempObject;

    @Override
    public Object tempObject(Object tempObject)
    {
        Object obj = this.tempObject;
        this.tempObject = tempObject;
        return obj;
    }

}
