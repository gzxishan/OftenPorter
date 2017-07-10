package cn.xishan.oftenporter.oftendb.db.mongodb;


import cn.xishan.oftenporter.oftendb.annotation.*;
import com.mongodb.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class MongodbUtil
{

    /**
     * @param db
     * @param code
     * @param nolock
     * @param args
     * @return
     * @throws MongoException
     */
    public static Object eval(DB db, String code, boolean nolock, Object... args) throws MongoException
    {
        DBObject dbObject = BasicDBObjectBuilder.start().add("$eval", code).add("args", args)
                .add("nolock", nolock).get();
        CommandResult commandResult = db.command(dbObject);
        commandResult.throwOnError();
        return commandResult.get("retval");
    }

    /**
     * 确保普通索引。使用@Index标记,并且必须带有@Key标记。
     *
     * @param collection
     * @param order      1或-1(表示升序或降序)
     * @param forSearch
     */
    public static void createIndex(DBCollection collection, int order, Class<?> forSearch)
    {
        _createIndex(collection, order, false, forSearch, Index.class);
    }

    /**
     * 确保唯一索引.使用@UniqueIndex标记,并且必须带有@Key标记。
     *
     * @param collection
     * @param order      1或-1(表示升序或降序)
     * @param forSearch
     */
    public static void createUniqueIndex(DBCollection collection, int order, Class<?> forSearch)
    {
        _createIndex(collection, order, true, forSearch, UniqueIndex.class);
    }

    /**
     * 确保索引,若不存在则创建索引,必须带有@Key标记 。
     *
     * @param collection      集合
     * @param order           1或-1(表示升序或降序)
     * @param unique          是否是唯一索引
     * @param forSearch       在此类中搜索
     * @param annotationClass 注解
     */

    @SuppressWarnings("unchecked")
    private static void _createIndex(final DBCollection collection, final int order, boolean unique, Class<?> forSearch,
            Class<? extends Annotation> annotationClass)
    {
        final BasicDBObject basicDBObject = new BasicDBObject("unique", unique);

        try
        {
            AnnotationSearch.searchPublicFields(new MyConsumer<Field, Exception>()
            {

                @Override
                public void accept(Field t) throws Exception
                {
                    DBField DBField = t.getAnnotation(DBField.class);
                    if (DBField == null)
                    {
                        return;
                    }
                    String name = DBField.value().equals("") ? t.getName()
                            : DBField.value();
                    collection.createIndex(new BasicDBObject(name, order), basicDBObject);
                }
            }, forSearch, null, annotationClass);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 确保索引,若不存在则创建索引,分别建立 。
     *
     * @param collection        集合
     * @param order             1或-1(表示升序或降序)
     * @param unique            是否是唯一索引
     * @param forSearch         在此类中搜索
     * @param annotationClasses 注解
     */

    public static void createIndex(final DBCollection collection, final int order, boolean unique, Class<?> forSearch,
            Class<? extends Annotation>... annotationClasses)
    {
        final BasicDBObject basicDBObject = new BasicDBObject("unique", unique);

        try
        {
            AnnotationSearch.searchPublicFields(new MyConsumer<Field, Exception>()
            {

                @Override
                public void accept(Field t) throws Exception
                {

                    collection.createIndex(new BasicDBObject(t.getName(), order), basicDBObject);
                }
            }, forSearch, null, annotationClasses);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 确保索引,若不存在则创建索引,分别建立 。
     *
     * @param collection mongodb集合名
     * @param order      1或-1(表示升序或降序)
     * @param unique     是否是唯一索引
     * @param keys       要设置的键名
     */
    @SafeVarargs
    public static void createIndex(DBCollection collection, int order, boolean unique, String... keys)
    {
        BasicDBObject basicDBObject = new BasicDBObject("unique", unique);

        /**
         * 索引
         */
        for (String string : keys)
        {
            collection.createIndex(new BasicDBObject(string, order), basicDBObject);
        }

    }

    /**
     * 确保组合索引,若不存在则创建索引 。
     *
     * @param collection
     * @param unique
     * @param oreders    1或-1(表示升序或降序)
     * @param keys
     */
    public static void createCombinIndex(DBCollection collection, boolean unique, int[] oreders, String[] keys)
    {
        BasicDBObject basicDBObject = new BasicDBObject("unique", unique);

        BasicDBObject dbObject = new BasicDBObject();
        for (int i = 0; i < oreders.length; i++)
        {
            dbObject.put(keys[i], oreders[1]);
        }

        collection.createIndex(dbObject, basicDBObject);

    }

}
