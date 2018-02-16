package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 用于生成唯一的、趋势递增的、每次具有千万个随机范围的id，假定应用启动间隔时间至少1秒。
 * </p>
 * <p>
 * 生成的id长度=7位时间戳(启动或达到21亿次时的时间戳)+设定长度+4位随机+提供的mchid长度
 * </p>
 * <p>
 * id字符集:0-9A-Z_a-z~
 * </p>
 * <p>
 * 使用@{@linkplain AutoSet}注解时，设置的实例为new IdGen(8,[由最大网卡mac得到的8为字符],true),生成id长度为27个字符,见{@linkplain #getDefault()}。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
@AutoSetDefaultDealt(gen = IdGenDealt.class)
public class IdGen
{
    private static final char[] BASE;

    static
    {
        BASE = ("0123456789" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "_~" +
                "abcdefghijklmnopqrstuvwxyz").toCharArray();
        Arrays.sort(BASE);
    }

    private char[] base = BASE;
    private int[] nums;
    private char[] idchars;
    private int idindex;

    private int randlen = 4;//随机值位数
    private int datelen = 7;//日期所占位数
    private static long lastTime;
    private static final Object KEY = new Object();
    private int count;

    /**
     * @param len         设定长度。
     * @param mchid       机器id。
     * @param rightOrLeft 机器id是拼接在右边还是左边。
     */
    public IdGen(int len, @MayNull char[] mchid, boolean rightOrLeft)
    {
        len += randlen;
        //System.out.println(new String(base));
        if (mchid != null)
        {
            for (int i = 0; i < mchid.length; i++)
            {
                if (Arrays.binarySearch(base, mchid[i]) < 0)
                {
                    throw new RuntimeException("unknown char:" + mchid[i]);
                }
            }
            idchars = new char[datelen + len + mchid.length];
            int mi = idchars.length - mchid.length;
            for (int i = 0; i < mchid.length; i++)
            {
                int index = rightOrLeft ? mi + i : i;
                idchars[index] = mchid[i];
            }
        } else
        {
            idchars = new char[datelen + len];
        }

        nums = new int[len];
        if (!rightOrLeft && mchid != null)
        {
            idindex = mchid.length;
        } else
        {
            idindex = 0;
        }
        idindex += datelen;
    }

    /**
     * 生成的id字符长度为27个。
     *
     * @return
     */
    public static IdGen getDefault()
    {
        return IdGenDealt.getDefault();
    }

    public static void setLastTime(long lastTime)
    {
        synchronized (KEY)
        {
            IdGen.lastTime = lastTime;
        }
    }

    public static long getLastTime()
    {
        synchronized (KEY)
        {
            return lastTime;
        }
    }

    private void initTime()
    {
        String times = getTimeId(true);
        System.arraycopy(times.toCharArray(), 0, idchars, 0, datelen);
    }

    public String getTimeId(boolean isSecond)
    {
        synchronized (KEY)
        {
            int sleep = 0;
            long t = System.currentTimeMillis();
            if (isSecond)
            {
                if ((t - lastTime) / 1000 == 0)
                {
                    sleep = 1000;
                }
            } else
            {
                if (t - lastTime == 0)
                {
                    sleep = 1;
                }
            }
            if (sleep > 0)
            {
                try
                {
                    Thread.sleep(sleep);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                t = System.currentTimeMillis();
            }
            long time = t / (isSecond ? 1000 : 1);
            int blen = base.length;
            int dlen = isSecond ? datelen : datelen + 2;
            char[] cs = new char[dlen];
            for (int i = 0, k = dlen - 1; i < dlen; i++, k--)
            {
                int m = (int) (time % blen);
                time /= blen;
                cs[k] = base[m];
            }
            lastTime = t;
            return new String(cs);
        }
    }

    public static long num64ToNum10(String num64){
        char[] cs = num64.toCharArray();
        long v=0;
        long b=1;
        int blen = BASE.length;
        for (int i = cs.length-1;i>=0; i--)
        {
            char c = cs[i];
            int index = Arrays.binarySearch(BASE,c);
            if(index<0){
                throw new RuntimeException("illegal char:"+c);
            }
            v+=index*b;
            b*=blen;
        }
        return v;
    }

    public static String num10ToNum64(long value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("the value have to be positive!");
        }
        int blen = BASE.length;
        List<Integer> list = new ArrayList<>(11);
        while (true)
        {
            int m = (int) (value % blen);
            list.add(m);
            value /= blen;
            if (value == 0)
            {
                break;
            }
        }
        char[] cs = new char[list.size()];
        for (int i = 0, k = cs.length - 1; i < cs.length; i++, k--)
        {
            cs[k] = BASE[list.get(i)];
        }
        return new String(cs);
    }

    public synchronized void nextIds(List<String> list, int count)
    {
        for (int i = 0; i < count; i++)
        {
            list.add(nextId());
        }
    }

    public synchronized String nextId()
    {
        if ((count++) % 2100000000 == 0)
        {
            initTime();
            if (count > 1)
            {
                count = 0;
            }
        }
        final int blen = base.length;
        final int len = randlen;
        Random rand = new Random();
        for (int i = 0, ni = nums.length - 1; i < len; i++, ni--)
        {
            nums[ni] += rand.nextInt(blen);
        }
        for (int i = nums.length - 1; i > 0; i--)
        {
            if (nums[i] >= blen)
            {
                nums[i - 1]++;
                nums[i] -= blen;
            }
        }
        if (nums[0] >= blen)
        {
            nums[0] = 0;
            initTime();
        }
        for (int i = 0, ci = idindex; i < nums.length; i++, ci++)
        {
            idchars[ci] = base[nums[i]];
        }
        return new String(idchars);
    }
}
