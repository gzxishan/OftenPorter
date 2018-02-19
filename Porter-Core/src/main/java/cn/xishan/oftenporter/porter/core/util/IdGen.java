package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.security.SecureRandom;
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
 * 使用@{@linkplain AutoSet}注解时，其中机器id为最大网卡mac得到的8为字符,生成id长度为28个字符,见{@linkplain #getDefault()}。
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

    interface IRand
    {
        int nextInt(int n);
    }

    interface IRandBuilder
    {
        IRand build();
    }

    private char[] base = BASE;
    private int[] nums;
    private char[] idchars;
    private int idindex;

    private int randlen;//随机值位数
    private int datelen = 7;//日期所占位数
    private static long lastTime;
    private static final Object KEY = new Object();
    private int count;
    private IRandBuilder iRandBuilder;

    /**
     * 随机长度为4.
     *
     * @param len         设定长度。
     * @param mchid       机器id。
     * @param rightOrLeft 机器id是拼接在右边还是左边。
     */
    public IdGen(int len, @MayNull char[] mchid, boolean rightOrLeft)
    {
        this(len, 4, rightOrLeft ? null : mchid, rightOrLeft ? mchid : null, getDefaultBuilder());
    }

    static IRandBuilder getDefaultBuilder()
    {
        IRandBuilder builder = new IRandBuilder()
        {
            Random random = new Random();
            IRand iRand = n -> random.nextInt(n);
            long lastTime = System.currentTimeMillis();

            void init()
            {
                if (System.currentTimeMillis() - lastTime >= 10 * 60 * 1000)
                {
                    random = new SecureRandom();
                    lastTime = System.currentTimeMillis();
                }
            }

            @Override
            public IRand build()
            {
                init();
                return iRand;
            }
        };
        return builder;
    }

    /**
     * @param len          设定长度
     * @param randlen
     * @param mchidLeft
     * @param mchidRight
     * @param iRandBuilder
     */
    public IdGen(int len, int randlen, @MayNull char[] mchidLeft, @MayNull char[] mchidRight, IRandBuilder iRandBuilder)
    {
        len += randlen;
        this.randlen = randlen;
        this.iRandBuilder = iRandBuilder;
        if (mchidLeft != null || mchidRight != null)
        {
            idchars = new char[datelen + len + (mchidLeft == null ? 0 : mchidLeft.length) + (mchidRight == null ? 0 :
                    mchidRight.length)];

            if (mchidLeft != null)
            {
                for (int i = 0; i < mchidLeft.length; i++)
                {
                    idchars[i] = mchidLeft[i];
                }
            }
            if (mchidRight != null)
            {
                int mi = idchars.length - mchidRight.length;
                for (int i = 0; i < mchidRight.length; i++)
                {
                    idchars[mi + i] = mchidRight[i];
                }
            }

        } else
        {
            idchars = new char[datelen + len];
        }

        nums = new int[len];
        if (mchidLeft != null)
        {
            idindex = mchidLeft.length;
        } else
        {
            idindex = 0;
        }
        idindex += datelen;
    }

    /**
     * @param specLen     指定长度
     * @param randBits    随机数位数
     * @param mchid       机器id
     * @param rightOrLeft
     * @return
     */
    public static IdGen getSecureRand(int specLen, int randBits, char[] mchid, boolean rightOrLeft)
    {
        IRandBuilder builder = new IRandBuilder()
        {
            SecureRandom secureRandom = new SecureRandom();
            IRand iRand = n -> secureRandom.nextInt(n);
            long lastTime = System.currentTimeMillis();

            void init()
            {
                if (System.currentTimeMillis() - lastTime >= 10 * 60 * 1000)
                {
                    secureRandom = new SecureRandom();
                    lastTime = System.currentTimeMillis();
                }
            }

            @Override
            public IRand build()
            {
                init();
                return iRand;
            }
        };
        IdGen idGen = new IdGen(specLen, randBits, rightOrLeft ? null : mchid, rightOrLeft ? mchid : null, builder);
        return idGen;
    }

    /**
     * 生成的id字符长度为28个，其中机器id由最大网卡mac得到的8为字符,并且字符串以i开头。
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

    public static long num64ToNum10(String num64)
    {
        char[] cs = num64.toCharArray();
        long v = 0;
        long b = 1;
        int blen = BASE.length;
        for (int i = cs.length - 1; i >= 0; i--)
        {
            char c = cs[i];
            int index = Arrays.binarySearch(BASE, c);
            if (index < 0)
            {
                throw new RuntimeException("illegal char:" + c);
            }
            v += index * b;
            b *= blen;
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
        IRand rand = iRandBuilder.build();
        nums[nums.length - 1]++;//确保一定会增加
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
