package cn.xishan.oftenporter.porter.core.util;

import java.util.StringTokenizer;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class StrUtil
{
    /**
     * 分隔字符串。
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
     * @param args
     * @return
     */
    public static String[] array(String... args)
    {
        return args;
    }
}
