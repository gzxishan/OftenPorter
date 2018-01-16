package cn.xishan.oftenporter.porter.core.util;

import com.alibaba.fastjson.JSONArray;

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
     * 得到target在strs中的位置。
     * @param target
     * @param strs
     * @return 未找到返回-1，找到返回对应索引。
     */
    public static int indexOf(String target, String... strs)
    {
        int index = -1;
        for (int i = 0; i < strs.length; i++)
        {
            if (strs[i].equals(target))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public static String[] newArray(String... args)
    {
        String[] as = new String[args.length];
        for (int i = 0; i < args.length; i++)
        {
            as[i] = args[i];
        }
        return as;
    }

    public static JSONArray toJSONArray(Object... args)
    {
        JSONArray jsonArray = new JSONArray(args.length);
        WPTool.addAll(jsonArray, args);
        return jsonArray;
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
     *
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


    /**
     * 获取后缀名;若没有找到，则返回空字符串"".
     *
     * @param content     内容
     * @param c           后缀名分隔符
     * @param includeChar 返回的后缀名是否包含分隔符。
     * @return
     */
    public static String getSuffix(String content, char c, boolean includeChar)
    {
        int index = content.lastIndexOf(c);
        return index >= 0 ? content.substring(includeChar ? index : index + 1) : "";
    }

    /**
     * 获取后缀名，不包括分隔符;若没有找到，则返回空字符串"".
     * {@linkplain #getSuffix(String, char, boolean)}
     *
     * @param content
     * @param c
     * @return
     */
    public static String getSuffix(String content, char c)
    {
        return getSuffix(content, c, false);
    }

    /**
     * 移除后缀名;若没有找到，则返回原字符串.
     *
     * @param content
     * @param c           后缀名分隔符
     * @param includeChar 返回值是否包含分隔符。
     * @return
     */
    public static String removeSuffix(String content, char c, boolean includeChar)
    {
        int index = content.lastIndexOf(c);
        return index >= 0 ? content.substring(0, includeChar ? index + 1 : index) : content;
    }

    /**
     * 移除后缀名,不包含分隔符;若没有找到，则返回原字符串.
     * 见{@linkplain #removeSuffix(String, char, boolean)}
     *
     * @param content
     * @param c       后缀名分隔符
     * @return
     */
    public static String removeSuffix(String content, char c)
    {
        return removeSuffix(content, c, false);
    }


}
