package cn.xishan.oftenporter.oftendb.transaction.porter;

import cn.xishan.oftenporter.oftendb.transaction.entity.TestEntity;
import cn.xishan.oftenporter.oftendb.transaction.unit.TestUnit;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;

/**
 * @author Created by https://github.com/CLovinr on 2019/1/2.
 */
@PortIn
public class TestPorter
{
    @AutoSet
    TestUnit testUnit;

    @PortStart
    public void onStart()
    {
        testUnit.initTable();
    }

    @PortIn
    public void testOk(@Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2)
    {
        testUnit.clearAll();
        testUnit.teskOk(entity1, entity2);
    }

    @PortIn
    public void testFail(@Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2)
    {
        testUnit.clearAll();
        testUnit.testFail(entity1, entity2);
    }

    @PortIn
    public void testOk2(@Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2)
    {
        testUnit.clearAll();
        testUnit.teskOk2(entity1, entity2);
    }

    @PortIn
    public void testFail2( @Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2)
    {
        testUnit.clearAll();
        testUnit.testFail2(entity1, entity2);
    }

    @PortIn
    public void testOkPoint(@Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2)
    {
        testUnit.clearAll();
        testUnit.testOkPoint(entity1, entity2);
    }

    @PortIn
    public void testFailPoint(@Nece("entity1") TestEntity entity1,@Nece("entity2")  TestEntity entity2,@Nece("entity3")  TestEntity entity3)
    {
        testUnit.clearAll();
        testUnit.testFailPoint(entity1, entity2,entity3);
    }
}
