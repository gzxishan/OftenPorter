package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
public class IdGenTest
{
    @Test
    public void test(){
        IdGen idGen = new IdGen(8,"oHmKnp".toCharArray(),true);
        int N=100000;
        List<String> ids = new ArrayList<>(N);
        long t=System.nanoTime();
        idGen.nextIds(ids,N);
        float total = (System.nanoTime()-t)*1.0f/1000000000*1000;
		/*for(int i=0;i<ids.size();i++){
			String id = ids.get(i);
			System.out.println(i+":"+id);
		}*/
        System.out.println("total="+total+"ms,dt="+total/N);
    }

    @Test
    public void testNum10ToNum64(){
        Assert.assertEquals("10",IdGen.num10ToNum64(64));
        Assert.assertEquals("1~",IdGen.num10ToNum64(64+63));

        Assert.assertEquals(64,IdGen.num64ToNum10(IdGen.num10ToNum64(64)));
        long time = System.currentTimeMillis();
        Assert.assertEquals(time,IdGen.num64ToNum10(IdGen.num10ToNum64(time)));
    }

    @Test
    public void testDefault(){
        Assert.assertTrue(IdGen.getDefault().nextId().length()==27);
    }
}
