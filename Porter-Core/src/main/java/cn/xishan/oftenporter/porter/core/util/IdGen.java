package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.io.Serializable;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.*;

/**
 * <p>
 * 用于生成唯一的、趋势递增的、具有随机范围的id，假定应用启动间隔时间至少1秒。
 * </p>
 * <p>
 * 生成的id长度=[左填充位长度]+[秒级日期位长度(启动或达到最大设定数时的时间戳)]+[设定长度]+[随机位长度]+[右填充位长度]
 * </p>
 * <p>
 * id字符集:[0-9A-Z_a-z~]
 * </p>
 * <p>
 * 使用@{@linkplain AutoSet}注解时，默认生成的实例见{@linkplain #getDefault()}。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
@AutoSetDefaultDealt(gen = IdGenDealt.class)
public class IdGen implements Serializable
{

    private static final long serialVersionUID = -8251636420192526844L;

    private static final char[] BASE;
    private static IdGen DEFAULT_ID_GEN;


    static
    {
        BASE = ("0123456789" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "_~" +
                "abcdefghijklmnopqrstuvwxyz").toCharArray();
        Arrays.sort(BASE);
    }

    public interface IRand extends Serializable
    {
        int nextInt(int n);
    }

    public interface IRandBuilder extends Serializable
    {
        IRand build();
    }

    private char[] base = BASE;
    private int[] nums;
    private char[] idchars;
    private int dateindex, idindex;

    private int randlen;//随机值位数
    private int datelen;//日期所占位数
    private static long lastTime;
    private static final Object KEY = new Object();
    private IRandBuilder iRandBuilder;

    /**
     * 日期长度为7、随机长度为4.
     *
     * @param len         设定长度。
     * @param mchid       机器id。
     * @param rightOrLeft 机器id是拼接在右边还是左边。
     */
    public IdGen(int len, @MayNull char[] mchid, boolean rightOrLeft)
    {
        this(7, len, 4, rightOrLeft ? null : mchid, rightOrLeft ? mchid : null, getDefaultBuilder());
    }

    public static IRandBuilder getDefaultBuilder()
    {
        IRandBuilder builder = new IRandBuilder()
        {
            private static final long serialVersionUID = 5391777136533634204L;
            Random random = new Random();
            IRand iRand = new IRand()
            {
                private static final long serialVersionUID = 6139967705065877083L;

                @Override
                public int nextInt(int n)
                {
                    return random.nextInt(n);
                }
            };
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

    public static IRandBuilder getDefaultSecureBuilder()
    {
        IRandBuilder builder = new IRandBuilder()
        {
            private static final long serialVersionUID = 563785586169515068L;
            SecureRandom secureRandom = new SecureRandom();
            IRand iRand = new IRand()
            {
                private static final long serialVersionUID = 9152457555149315327L;

                @Override
                public int nextInt(int n)
                {
                    return secureRandom.nextInt(n);
                }
            };
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
        return builder;
    }

    /**
     * @param datelen      日期所占位数，5~16
     * @param len          设定长度
     * @param randlen      随机位数
     * @param mchidLeft    左侧填充的字符
     * @param mchidRight   右侧填充的字符
     * @param iRandBuilder 用于生成随机数
     */
    public IdGen(int datelen, int len, int randlen, @MayNull char[] mchidLeft, @MayNull char[] mchidRight,
            IRandBuilder iRandBuilder)
    {
        if (datelen < 5 || datelen > 16)
        {
            throw new IllegalArgumentException("datelen range:5~16");
        }
        len += randlen;
        this.datelen = datelen;
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
            dateindex = mchidLeft.length;
        } else
        {
            dateindex = 0;
        }
        idindex = dateindex + datelen;
        initTime();
    }

    /**
     * @param specLen     指定长度
     * @param randBits    随机数位数
     * @param mchid       机器id
     * @param rightOrLeft mchid是填充在右侧还是左侧
     * @return
     */
    public static IdGen getSecureRand(int datelen, int specLen, int randBits, char[] mchid, boolean rightOrLeft)
    {
        IRandBuilder builder = getDefaultSecureBuilder();
        IdGen idGen = new IdGen(datelen, specLen, randBits, rightOrLeft ? null : mchid, rightOrLeft ? mchid : null,
                builder);
        return idGen;
    }

    /**
     * 生成的id字符长度为(21个字符):[x][6位秒级日期][5位设定长度][3位随机位][6位最大网卡mac]。
     * <p>
     * <strong>注意</strong>：mac地址的高位的3个字节中只取了中间的那个字节。
     * </p>
     *
     * @return
     */
    public static synchronized IdGen getDefault()
    {
        if (DEFAULT_ID_GEN != null)
        {
            return DEFAULT_ID_GEN;
        }
        long mac = getMac();
        if (mac == -1)
        {
            mac = 0;
        }
        String mchid = IdGen.num10ToNum64(mac);
        DEFAULT_ID_GEN = new IdGen(6, 5, 3, "x".toCharArray(), mchid.toCharArray(), IdGen.getDefaultBuilder());
        return DEFAULT_ID_GEN;
    }

    private static long getMac()
    {
        long mac = -1;
        try
        {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

            while (enumeration.hasMoreElements())
            {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface != null)
                {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    if (bytes == null)
                    {
                        continue;
                    }
                    long s = bytes[1] & 0xFF;//BytesTool.readUnShort(bytes, 0);
                    long i = BytesTool.readInt(bytes, 2);
                    long l = (s << 24) | i;
                    if (l > mac)
                    {
                        mac = l;
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return mac;
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
        System.arraycopy(times.toCharArray(), 0, idchars, dateindex, datelen);
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

    /**
     * @param num64
     * @param value 可以为负数
     * @return
     */
    public static String num64AddNum10(String num64, long value)
    {
        char[] cs = num64.toCharArray();
        int blen = BASE.length;
        int[] nums = new int[cs.length];
        for (int i = 0; i < nums.length; i++)
        {
            char c = cs[i];
            int index = Arrays.binarySearch(BASE, c);
            if (index < 0)
            {
                throw new RuntimeException("illegal char:" + c);
            }
            nums[i] = index;
        }

        int k = nums.length - 1;
        boolean isAdd = value >= 0;
        if (!isAdd)
        {
            value = -value;
        }
        while (k >= 0)
        {
            int m = (int) (value % blen);
            if (isAdd)
            {
                nums[k--] += m;
            } else
            {
                nums[k--] -= m;
            }
            value /= blen;
            if (value == 0)
            {
                break;
            }
        }
        if (isAdd)
        {
            for (int i = nums.length - 1; i > 0; i--)
            {
                if (nums[i] >= blen)
                {
                    nums[i - 1]++;
                    nums[i] -= blen;
                }
            }
            if (nums[0] >= blen)
            {//处理溢出
                nums[0] = 0;
            }
        } else
        {
            for (int i = nums.length - 1; i > 0; i--)
            {
                if (nums[i] < 0)
                {
                    nums[i - 1]--;
                    nums[i] += blen;
                }
            }
            if (nums[0] >= blen)
            {//处理溢出
                nums[0] = blen - 1;
            }
        }
        for (int i = 0; i < nums.length; i++)
        {
            cs[i] = BASE[nums[i]];
        }
        return new String(cs);
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

    /**
     * @param value 必须大于等于0
     * @return
     */
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
        for (int i = 0, ci = idindex; i < nums.length; i++, ci++)
        {
            idchars[ci] = base[nums[i]];
        }
        String id = new String(idchars);

        if (nums[0] >= blen)
        {
            int length = nums.length - randlen;
            for (int i = 0; i < length; i++)
            {
                nums[i] = 0;
            }
            initTime();
        }
        return id;
    }
}
