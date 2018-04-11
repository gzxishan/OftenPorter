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
 * id字符集:[0-9A-Z_a-z]
 * </p>
 * <p>
 * <strong color='red'>注意：</strong>
 * <ol>
 * <li>开始时间fromTimeMillis作用，是用当前时间减去fromTimeMillis，用得到的差值来计算日期部分。</li>
 * <li>日期位的长度datelen决定了最大能表示的时间，如日期位是5，则最大可以表示63x63x63x63x63秒的时间差值（约为31年,当前时间减去fromTimeMillis）</li>
 * </ol>
 * </p>
 * <p>
 * 使用@{@linkplain AutoSet}注解时，默认生成的实例见{@linkplain #getDefault(long)}。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/2/16.
 */
@AutoSetDefaultDealt(gen = IdGenDealt.class)
public class IdGen implements Serializable
{

    private static final long serialVersionUID = -8251636420192526844L;

    private static final char[] BASE;


    static
    {
        BASE = ("0123456789" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "_" +
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

    private static final Object KEY = new Object();
    private char[] base = BASE;
    private int[] nums;
    private char[] idchars;
    private int dateindex, idindex;

    private int randlen;//随机值位数
    private int datelen;//日期所占位数
    private long lastTime;
    private IRandBuilder iRandBuilder;
    private long fromTimeMillis;

    /**
     * 日期长度为7、随机长度为4.
     *
     * @param fromTimeMillis 开始时间，单位毫秒。
     * @param len            设定长度。
     * @param mchid          机器id。
     * @param rightOrLeft    机器id是拼接在右边还是左边。
     */
    public IdGen(long fromTimeMillis, int len, @MayNull char[] mchid, boolean rightOrLeft)
    {
        this(fromTimeMillis, 7, len, 4, rightOrLeft ? null : mchid, rightOrLeft ? mchid : null, getDefaultBuilder());
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
     * @param fromTimeMillis 开始时间，单位毫秒。
     * @param datelen        日期所占位数，5~9
     * @param len            设定长度
     * @param randlen        随机位数
     * @param mchidLeft      左侧填充的字符
     * @param mchidRight     右侧填充的字符
     * @param iRandBuilder   用于生成随机数
     */
    public IdGen(long fromTimeMillis, int datelen, int len, int randlen, @MayNull char[] mchidLeft,
            @MayNull char[] mchidRight,
            IRandBuilder iRandBuilder)
    {
        if (datelen < 5 || datelen > 9)
        {
            throw new IllegalArgumentException("datelen range:5~9");
        }
        if (System.currentTimeMillis() - fromTimeMillis < 0)
        {
            throw new IllegalArgumentException("fromTimeMillis should not great than current time millis!");
        }
        len += randlen;
        this.fromTimeMillis = fromTimeMillis;
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
     * @param datelen     日期所占位数，5~9
     * @param specLen     指定长度
     * @param randBits    随机数位数
     * @param mchid       机器id
     * @param rightOrLeft mchid是填充在右侧还是左侧
     * @return
     */
    public static IdGen getSecureRand(long fromTimeMillis, int datelen, int specLen, int randBits, char[] mchid,
            boolean rightOrLeft)
    {
        IRandBuilder builder = getDefaultSecureBuilder();
        IdGen idGen = new IdGen(fromTimeMillis, datelen, specLen, randBits, rightOrLeft ? null : mchid,
                rightOrLeft ? mchid : null,
                builder);
        return idGen;
    }

    /**
     * 见{@linkplain #getDefault(long)}
     *
     * @param fromYear  开始的年
     * @param fromMonth 开始的月,0~11
     * @return
     */
    public static IdGen getDefault(int fromYear, int fromMonth)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(fromYear, fromMonth, 1, 0, 0, 0);
        return getDefault(calendar.getTimeInMillis());
    }

    /**
     * 生成的id字符长度为(21个字符):[6位秒级日期][4位设定长度][3位随机位][8位最大网卡（已启动）mac]。
     *
     * @param fromTimeMillis 开始的日期毫秒数
     * @return
     */
    public static IdGen getDefault(long fromTimeMillis)
    {
        String mchid = getNetMac();
        IdGen idGen = new IdGen(fromTimeMillis, 6, 4, 3, null, mchid.toCharArray(), IdGen.getDefaultBuilder());
        return idGen;
    }

    /**
     * 见{@linkplain #getDefaultWithX(long)}
     *
     * @param fromYear  开始的年
     * @param fromMonth 开始的月,0~11
     * @return
     */
    public static IdGen getDefaultWithX(int fromYear, int fromMonth)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(fromYear, fromMonth, 1, 0, 0, 0);
        return getDefaultWithX(calendar.getTimeInMillis());
    }

    /**
     * 生成的id字符长度为(22个字符):[x][6位秒级日期][4位设定长度][3位随机位][8位最大网卡（已启动）mac]。
     *
     * @param fromTimeMillis 开始的日期毫秒数
     * @return
     */
    public static IdGen getDefaultWithX(long fromTimeMillis)
    {
        String mchid = getNetMac();
        IdGen idGen = new IdGen(fromTimeMillis, 6, 4, 3, "x".toCharArray(), mchid.toCharArray(),
                IdGen.getDefaultBuilder());
        return idGen;
    }

    /**
     * 根据可用网卡中的最大mac地址生成,8个字符长度
     *
     * @return
     */
    public static String getNetMac()
    {
        long mac = getMac();
        if (mac == -1)
        {
            mac = 0;
        }
        String mchid = IdGen.num10ToNum6X(mac, 8);
        return mchid;
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
                byte[] bytes = networkInterface.getHardwareAddress();
                if (bytes == null || !networkInterface.isUp())
                {
                    continue;
                }
                long s = BytesTool.readUnShort(bytes, 0);
                long i = 0xFFFFFFFFL & BytesTool.readInt(bytes, 2);
                long l = (s << 32) | i;
                if (l > mac)
                {
                    mac = l;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return mac;
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
            if (System.currentTimeMillis() - fromTimeMillis < 0)
            {
                throw new IllegalArgumentException("fromTimeMillis should not great than current time millis!");
            }
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
            long dtime = (t - fromTimeMillis) / (isSecond ? 1000 : 1);
            int blen = base.length;
            int dlen = isSecond ? datelen : datelen + 2;
            char[] cs = new char[dlen];
            for (int i = 0; i < cs.length; i++)
            {
                cs[i] = base[0];
            }
            for (int i = 0, k = dlen - 1; i < dlen; i++, k--)
            {
                int m = (int) (dtime % blen);
                dtime /= blen;
                cs[k] = base[m];
            }
            lastTime = t;
            return new String(cs);
        }
    }

    /**
     * @param num6X
     * @param value 可以为负数
     * @return
     */
    public static String num6XAddNum10(String num6X, long value)
    {
        char[] cs = num6X.toCharArray();
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

    public static long num6XToNum10(String num6X)
    {
        char[] cs = num6X.toCharArray();
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
    public static String num10ToNum6X(long value)
    {
        return num10ToNum6X(value, 0);
    }

    /**
     * @param value    必须大于等于0
     * @param minCount 最低位数,0表示忽略，不足的左边补0
     * @return
     */
    public static String num10ToNum6X(long value, int minCount)
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
        int len;
        if (minCount > 0 && list.size() < minCount)
        {
            len = minCount;
        } else
        {
            len = list.size();
        }
        char[] cs = new char[len];
        for (int i = 0, k = cs.length - 1; i < list.size(); i++, k--)
        {
            cs[k] = BASE[list.get(i)];
        }
        if (len > list.size())
        {
            len -= list.size();
            for (int i = 0; i < len; i++)
            {
                cs[i] = BASE[0];
            }
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
        if (nums[0] >= blen)
        {//最高位数值溢出、增加时间
            int length = nums.length - randlen;
            for (int i = 0; i < length; i++)
            {
                nums[i] = 0;
            }
            initTime();
        }
        for (int i = 0, ci = idindex; i < nums.length; i++, ci++)
        {
            idchars[ci] = base[nums[i]];
        }
        String id = new String(idchars);
        return id;
    }
}
