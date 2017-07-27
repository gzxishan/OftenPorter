package cn.xishan.oftenporter.porter.core.annotation.sth;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by https://github.com/CLovinr on 2017/7/27.
 */
public class _SetOkObjectTest {

    @Test
    public void sort() {
        AutoSetHandle._SetOkObject[] setOkObjects = {
                new AutoSetHandle._SetOkObject(null, null, 2),
                new AutoSetHandle._SetOkObject(null, null, 1),
                new AutoSetHandle._SetOkObject(null, null, 1),
                new AutoSetHandle._SetOkObject(null, null, -21)
        };
        Arrays.sort(setOkObjects);
        Assert.assertEquals(2,setOkObjects[0].priority);
    }
}
