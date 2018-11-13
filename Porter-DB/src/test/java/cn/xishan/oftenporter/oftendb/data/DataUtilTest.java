package cn.xishan.oftenporter.oftendb.data;

import cn.xishan.oftenporter.oftendb.util.DataUtil;
import cn.xishan.oftenporter.porter.core.JResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chenyg on 2017-04-10.
 */
public class DataUtilTest
{
    @Test
    public void testResult(){
        Assert.assertTrue(DataUtil.resultIntOrLongGtZero(JResponse.success((long)1)));
        Assert.assertTrue(DataUtil.resultIntOrLongGtZero(JResponse.success((int)1)));

        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success(null)));
        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success("not number")));
        Assert.assertFalse(DataUtil.resultIntOrLongGtZero(JResponse.success(1.0)));
        
    }
}
