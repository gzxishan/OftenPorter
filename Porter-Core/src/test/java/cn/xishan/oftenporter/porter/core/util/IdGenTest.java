package cn.xishan.oftenporter.porter.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
public class IdGenTest
{
    private static final long FROM_TIME_MILLIS;

    static
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 2 - 1, 16, 00, 00, 00);
        FROM_TIME_MILLIS = calendar.getTimeInMillis();
    }

    @Test
    public void test()
    {
        IdGen idGen = new IdGen(FROM_TIME_MILLIS,8, "oHmKnp".toCharArray(), true);
        int N = 100*10000;
        List<String> ids = new ArrayList<>(N);
        long t = System.nanoTime();
        idGen.nextIds(ids, N);
        float total = (System.nanoTime() - t) * 1.0f / 1000000000 * 1000;
//		for(int i=0;i<ids.size();i++){
//			String id = ids.get(i);
//			System.out.println(i+":"+id);
//		}
        System.out.println("total=" + total + "ms,dt=" + total / N);
    }

    @Test
    public void testSecure()
    {
        IdGen idGen = IdGen.getSecureRand(FROM_TIME_MILLIS,7, 8, 4, "oHmKnp".toCharArray(), true);
        int N = 100*10000;
        List<String> ids = new ArrayList<>(N);
        long t = System.nanoTime();
        idGen.nextIds(ids, N);
        float total = (System.nanoTime() - t) * 1.0f / 1000000000 * 1000;
//		for(int i=0;i<ids.size();i++){
//			String id = ids.get(i);
//			System.out.println(i+":"+id);
//		}
        System.out.println("secure:total=" + total + "ms,dt=" + total / N);
    }

    @Test
    public void testNum10ToNum6X()
    {
        Assert.assertEquals("10", IdGen.num10ToNum6X(63));
        Assert.assertEquals("1z", IdGen.num10ToNum6X(63 + 62));

        Assert.assertEquals(63, IdGen.num6XToNum10(IdGen.num10ToNum6X(63)));
        long time = System.currentTimeMillis();
        Assert.assertEquals(time, IdGen.num6XToNum10(IdGen.num10ToNum6X(time)));
    }

    @Test
    public void testNum6XAddNum10()
    {
        long value = 9239_969_989_193749_66L;
        String num6XValue = IdGen.num10ToNum6X(value);

        Random random = new Random();
        long t = System.nanoTime();
        int N = 100*10000;
        for (int i = 0; i < N; i++)
        {
            int add = random.nextBoolean() ? random.nextInt() : -random.nextInt();
            Assert.assertEquals(value + add, IdGen.num6XToNum10(IdGen.num6XAddNum10(num6XValue, add)));
        }
        float total = (System.nanoTime() - t) * 1.0f / 1000000000 * 1000;
        System.out.println("testNum6XAddNum10:" + total + "ms");
    }

    @Test
    public void testDefault()
    {
        IdGen idGen = IdGen.getDefault(FROM_TIME_MILLIS);
        LogUtil.printPosLn(idGen.nextId());
        long t = System.nanoTime();
        int N = 100*10000;
        for (int i = 0; i < N; i++)
        {
            Assert.assertTrue(idGen.nextId().length() == 21);
        }
        float total = (System.nanoTime() - t) * 1.0f / 1000000000 * 1000;
        System.out.println("testDefault:" + total + "ms");

    }
}
