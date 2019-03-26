package cn.xishan.oftenporter.porter.core.util;

import com.alibaba.fastjson.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class OftenStrUtil
{

    /**
     * 将“name1=value1&name2=value2”转换成map,且采用有序map。
     *
     * @param encodingContent
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> fromEncoding(String encodingContent,
            String encoding) throws UnsupportedEncodingException
    {
        String[] strs = OftenStrUtil.split(encodingContent, "&");
        Map<String, String> paramsMap = new LinkedHashMap<>(strs.length);
        int index;
        for (String string : strs)
        {
            index = string.indexOf('=');
            if (index != -1)
            {
                paramsMap.put(string.substring(0, index),
                        URLDecoder.decode(string.substring(index + 1), encoding));
            }
        }
        return paramsMap;
    }

    /**
     * 分割字符串，不包括空字符串。
     *
     * @param srcString 待分割的字符串
     * @param splitStr  分隔内容
     * @return
     */
    public static String[] split(String srcString, String splitStr)
    {
        return split(srcString, splitStr, false);
    }

    /**
     * 分隔字符串。
     *
     * @param srcString           待分割的字符串
     * @param splitStr            分隔内容
     * @param containsEmptyString 是否包含空的字符串
     * @return
     */
    public static String[] split(String srcString, String splitStr, boolean containsEmptyString)
    {
        List<String> list = new ArrayList<>();
        if (srcString != null)
        {
            int from = 0;
            int slen = splitStr.length();
            while (from < srcString.length())
            {
                int index = srcString.indexOf(splitStr, from);
                if (index != -1)
                {
                    if (containsEmptyString || index > from)
                    {
                        list.add(srcString.substring(from, index));
                    }
                    from = index + slen;
                } else
                {
                    list.add(srcString.substring(from));
                    break;
                }
            }
            if (containsEmptyString && (srcString.length() == 0 || srcString.length() >= splitStr.length() && srcString
                    .endsWith(splitStr)))
            {
                list.add("");
            }
        }
        String[] rs = list.toArray(new String[0]);
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
     *
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
        OftenTool.addAll(jsonArray, args);
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

    public static String getNameFormPath(String path)
    {
        return getNameFormPath(path, '/');
    }

    public static String getNameFormPath(String path, char seperator)
    {
        int index = path.lastIndexOf(seperator);
        return index >= 0 ? path.substring(index + 1) : path;
    }

    /**
     * 获取后缀名，不包括分隔符".";若没有找到，则返回空字符串"".
     * {@linkplain #getSuffix(String, char, boolean)}
     *
     * @param content
     * @return
     */
    public static String getSuffix(String content)
    {
        return getSuffix(content, '.', false);
    }

    /**
     * 获取后缀名，包括分隔符".";若没有找到，则返回空字符串"".
     * {@linkplain #getSuffix(String, char, boolean)}
     *
     * @param content
     * @return
     */
    public static String getSuffixWithChar(String content)
    {
        return getSuffix(content, '.', true);
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


    /**
     * 连接成字符串。
     *
     * @param separator 分隔字符串
     * @param args
     * @return
     */
    public static String join(String separator, Object... args)
    {
        if (args.length == 0)
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(args[0]);
        for (int i = 1; i < args.length; i++)
        {
            builder.append(separator).append(args[i]);
        }
        return builder.toString();
    }

    public static String join(String separator, String... strs)
    {
        Object[] args = strs;
        return join(separator, args);
    }

    /**
     * 连接成字符串。
     *
     * @param separator  分隔字符串
     * @param collection
     * @return
     */
    public static String join(String separator, Collection<?> collection)
    {
        if (collection.isEmpty())
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Iterator<?> it = collection.iterator();
        builder.append(it.next());
        while (it.hasNext())
        {
            builder.append(separator).append(it.next());
        }
        return builder.toString();
    }


}
