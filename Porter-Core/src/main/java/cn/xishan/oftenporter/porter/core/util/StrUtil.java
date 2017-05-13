package cn.xishan.oftenporter.porter.core.util;

import java.util.StringTokenizer;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class StrUtil
{
    /**
     * 分隔字符串。
     *
     * @param srcString
     * @param splitStr
     * @return
     */
    public static String[] split(String srcString, String splitStr)
    {
        StringTokenizer tokenizer = new StringTokenizer(srcString, splitStr);
        String[] rs = new String[tokenizer.countTokens()];
        for (int i = 0; i < rs.length; i++)
        {
            rs[i] = tokenizer.nextToken();
        }
        return rs;
    }

    /**
     * 可变数组转换。
     *
     * @param args
     * @return
     */
    public static String[] array(String... args)
    {
        return args;
    }

    /**
     * 生成一个指定位数的62进制的值为0的数。
     *
     * @param bits
     * @return
     */
    public static String num62Zero(int bits)
    {
        char[] cs = new char[bits];
        for (int i = 0; i < cs.length; i++)
        {
            cs[i] = '0';
        }
        return new String(cs);
    }

    /**
     * 增加1.
     * @param num
     * @param bits
     * @return
     */
    public static String num62Inc(String num, int bits)
    {
        if (num.length() < bits)
        {
            num = num62Zero(bits - num.length()) + num;
        }
        char[] cs = num.toCharArray();

        for (int i = cs.length - 1; i >= 0; i--)
        {
            char c = cs[i];
            if (c >= '0' && c < '9')
            {
                c = (char) (c + 1);
            } else if (c == '9')
            {
                c = 'A';
            } else if (c >= 'A' && c < 'Z')
            {
                c = (char) (c + 1);
            } else if (c == 'Z')
            {
                c = 'a';
            } else if (c >= 'a' && c < 'z')
            {
                c = (char) (c + 1);
            } else if (c == 'z')
            {
                c = '0';
            }
            cs[i] = c;
            if (c != '0')
            {
                break;
            }
        }

        return new String(cs);
    }
}
