package cn.xishan.oftenporter.oftendb.transaction.unit;

import cn.xishan.oftenporter.oftendb.annotation.TransactionDB;
import cn.xishan.oftenporter.oftendb.transaction.entity.TestEntity;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;

/**
 * @author Created by https://github.com/CLovinr on 2019/1/2.
 */
public class TestUnit
{
    @AutoSet
    TestDao testDao;

    @TransactionDB
    public void initTable()
    {
        testDao.initTable();
    }

    @TransactionDB
    public void clearAll()
    {
        testDao.clearAll();
    }

    public boolean contains(String id)
    {
        return testDao.contains(id);
    }

    @TransactionDB
    public void teskOk(TestEntity entity1, TestEntity entity2)
    {
        testDao.insert(entity1);
        testDao.insert(entity2);
    }

    @TransactionDB
    public void testFail(TestEntity entity1, TestEntity entity2)
    {
        testDao.insert(entity1);
        testDao.insert(entity2);
        throw new RuntimeException("fail");
    }

    @TransactionDB
    public void teskOk2(TestEntity entity1, TestEntity entity2)
    {
        testDao.insert(entity1);
        insert(entity2);
    }

    @TransactionDB
    public void testFail2(TestEntity entity1, TestEntity entity2)
    {
        testDao.insert(entity1);
        insert(entity2);
        throw new RuntimeException("fail");
    }

    @TransactionDB
    protected void insert(TestEntity testEntity)
    {
        testDao.insert(testEntity);
    }


    @TransactionDB
    public void testOkPoint(TestEntity entity1, TestEntity entity2)
    {
        testDao.insert(entity1);
        try
        {
            insertPoint(entity2, false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @TransactionDB
    public void testFailPoint(
            TestEntity entity1, TestEntity entity2, TestEntity entity3)
    {
        testDao.insert(entity1);
        try
        {
            insertPoint(entity2, true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        testDao.insert(entity3);
    }

    @TransactionDB(setSavePoint = true)
    protected void insertPoint(TestEntity testEntity, boolean fail)
    {
        testDao.insert(testEntity);
        if (fail)
        {
            throw new RuntimeException("fail point");
        }
    }
}
