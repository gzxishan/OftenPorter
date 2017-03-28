package cn.xishan.oftenporter.oftendb.db.mysql;

import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.CUnit;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import org.junit.Test;

import java.util.Arrays;

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
        sqlCondition.put(Condition.ENDSSWITH,new CUnit("type","%.jpg"));
        sqlCondition.put(Condition.STARTSWITH,new CUnit("name","[chen]"));
        sqlCondition.put(Condition.EQ,new CUnit("pid",null));
        sqlCondition.put(Condition.IN,new CUnit("age",new String[]{"12","23","26"}));
        sqlCondition.put(Condition.IN,new CUnit("agex", Arrays.asList(new String[]{"12","23","26"})));
        sqlCondition.put(Condition.IN,new CUnit("age2","19"));

        Object[] as = (Object[]) sqlCondition.toFinalObject();
        LogUtil.printErrPos(as[0]);
        LogUtil.printErrPos((Object[])as[1]);
    }

} 
