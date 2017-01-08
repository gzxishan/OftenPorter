package cn.xishan.oftenporter.oftendb.db.mysql;

import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.Unit;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * SqlCondition Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>1/8/2017</pre>
 */
public class SqlConditionTest
{
    @Test
    public void test()
    {
        SqlCondition sqlCondition = new SqlCondition();
        sqlCondition.put(Condition.ENDSSWITH,new Unit("type","%.jpg"));
        sqlCondition.put(Condition.STARTSWITH,new Unit("name","[chen]"));
        Object[] as = (Object[]) sqlCondition.toFinalObject();
        LogUtil.printErrPos(as[0]);
        LogUtil.printErrPos((Object[])as[1]);
    }

} 
