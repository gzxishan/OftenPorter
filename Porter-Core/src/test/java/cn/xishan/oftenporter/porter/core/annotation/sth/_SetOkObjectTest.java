package cn.xishan.oftenporter.porter.core.annotation.sth;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by https://github.com/CLovinr on 2017/7/27.
 */
public class _SetOkObjectTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(_SetOkObjectTest.class);
    @Test
    public void sort() {
        AutoSetHandle._SetOkObject[] setOkObjects = {
                new AutoSetHandle._SetOkObject(null, null, 2,LOGGER),
                new AutoSetHandle._SetOkObject(null, null, 1,LOGGER),
                new AutoSetHandle._SetOkObject(null, null, 1,LOGGER),
                new AutoSetHandle._SetOkObject(null, null, -21,LOGGER)
        };
        Arrays.sort(setOkObjects);
        Assert.assertEquals(2,setOkObjects[0].priority);
    }
}
