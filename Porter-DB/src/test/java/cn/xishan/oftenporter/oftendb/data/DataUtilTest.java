package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.annotation.DBField;
import cn.xishan.oftenporter.oftendb.util.DataUtil;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.annotation.param.JsonObj;
import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chenyg on 2017-04-10.
 */
public class DataUtilTest
{
    @Test
    public void testResult()
    {
        Assert.assertTrue(DataUtil.resultIntOrLongGtZero(JResponse.success((long) 1)));
        Assert.assertTrue(DataUtil.resultIntOrLongGtZero(JResponse.success((int) 1)));

        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success(null)));
        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success("not number")));
        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success(1.0)));

    }

    /////////////////////////////////////////////////////
    static class LoopA
    {
        @Nece
        String name;
        @Nece
        int age;

        @JsonObj
        LoopB loopB;

        public LoopA(String name, int age)
        {
            this.name = name;
            this.age = age;
        }
    }

    static class LoopB
    {
        @JsonObj
        LoopC loopC;

        public LoopB(LoopC loopC)
        {
            this.loopC = loopC;
        }
    }

    static class LoopC
    {
        @JsonObj
        LoopA loopA;

        public LoopC(LoopA loopA)
        {
            this.loopA = loopA;
        }
    }

    @Test
    public void testLoop1()
    {
        LoopC loopC = new LoopC(new LoopA("A", 1));
        loopC.loopA.loopB = new LoopB(new LoopC(new LoopA("A1", 2)));
        JSONObject jsonObject = DataUtil.toJSON(loopC, true);
        System.out.println(jsonObject);
    }

    @Test(expected = RuntimeException.class)
    public void testLoop2()
    {
        LoopC loopC = new LoopC(new LoopA("A", 1));
        loopC.loopA.loopB = new LoopB(new LoopC(loopC.loopA));
        JSONObject jsonObject = DataUtil.toJSON(loopC, true);
        System.out.println(jsonObject);
    }


    static class C
    {
        @Nece
        String name;
        @Unece
        String description;
        @DBField
        String remark;
        @DBField
        String code;

        public C(String name, String description, String remark)
        {
            this.name = name;
            this.description = description;
            this.remark = remark;
        }
    }

    @Test
    public void testToJson()
    {
        C c = new C("nameC", "descC", "remarkC");
        JSONObject jsonObject = DataUtil.toJSON(c, true, true, "name");
        Assert.assertNull(jsonObject.get("name"));
        Assert.assertFalse(jsonObject.containsKey("code"));
        Assert.assertEquals("descC", jsonObject.get("description"));

        jsonObject = DataUtil.toJSON(c, true, false, "name");
        Assert.assertEquals("nameC", jsonObject.get("name"));
        Assert.assertFalse(jsonObject.containsKey("remark"));
        Assert.assertNull(jsonObject.get("description"));
    }
}
