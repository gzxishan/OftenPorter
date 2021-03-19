package cn.xishan.oftenporter.porter.core.util;

import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by https://github.com/CLovinr on 2016/9/6.
 */
public class OftenStrUtil
{

    /**
     * 判断两个数组是否相等。
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static boolean equals(Object[] arr1, Object[] arr2)
    {
        boolean isEq = true;
        if (arr1.length == arr2.length)
        {
            for (int i = 0; i < arr1.length; i++)
            {
                if (arr1[i].equals(arr2[i]))
                {
                    isEq = false;
                    break;
                }
            }
        } else
        {
            isEq = false;
        }
        return isEq;
    }

    /**
     * 解析地址查询参数。
     *
     * @param url      如果存在“?”，则去后面的内容进行解析；如果存在“#”，则去掉“#”及后面的内容。
     * @param encoding 字符编码，如utf-8
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> decodeQueryParams(String url, String encoding) throws UnsupportedEncodingException
    {
        int index = url.lastIndexOf("?");
        String params = index >= 0 ? url.substring(index + 1) : url;
        index = url.lastIndexOf("#");
        if (index >= 0)
        {
            params = params.substring(0, index);
        }

        return fromEncoding(params, encoding);
    }

    /**
     * 将“name1=value1&name2=value2”转换成map,且采用有序map。另见{@linkplain #toEncoding(String, Object...)}
     *
     * @param encodingContent
     * @param encoding        字符编码，如utf-8
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
                paramsMap.put(URLDecoder.decode(string.substring(0, index), encoding),
                        URLDecoder.decode(string.substring(index + 1), encoding));
            }
        }
        return paramsMap;
    }

    /**
     * 另见{@linkplain #fromEncoding(String, String)}
     *
     * @param encoding
     * @param nameValues
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toEncoding(String encoding, Object... nameValues) throws UnsupportedEncodingException
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nameValues.length - 1; i += 2)
        {
            builder.append(URLEncoder.encode(String.valueOf(nameValues[i]), encoding))
                    .append("=").append(URLEncoder.encode(String.valueOf(nameValues[i + 1]), encoding))
                    .append("&");
        }
        if (builder.length() > 0)
        {
            return builder.substring(0, builder.length() - 1);//不含最后的&
        } else
        {
            return "";
        }
    }

    /**
     * 另见{@linkplain #fromEncoding(String, String)}
     *
     * @param encoding
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toEncoding(String encoding, Map<String, Object> params) throws UnsupportedEncodingException
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet())
        {
            builder.append(URLEncoder.encode(entry.getKey(), encoding))
                    .append("=").append(URLEncoder.encode(String.valueOf(entry.getValue()), encoding))
                    .append("&");
        }
        if (builder.length() > 0)
        {
            return builder.substring(0, builder.length() - 1);//不含最后的&
        } else
        {
            return "";
        }
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

    public static String[] arrayFromJSONArray(String jsonArrayString)
    {
        JSONArray jsonArray = JSON.parseArray(jsonArrayString);
        String[] strings = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++)
        {
            strings[i] = jsonArray.getString(i);
        }
        return strings;
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

    /**
     * 替换#{properName}变量。
     *
     * @param srcMap        待替换属性值的map
     * @param propertiesMap 提供属性的map
     */
    public static void dealSharpProperties(Map srcMap, Map propertiesMap)
    {
        DealSharpProperties.dealSharpProperties(srcMap, propertiesMap);
    }

    public static void dealSharpProperties(Map srcMap, Map propertiesMap, boolean keepNotFound)
    {
        DealSharpProperties.dealSharpProperties(srcMap, propertiesMap, keepNotFound);
    }

    /**
     * 替换所有的#{propertyName},对于不存在的会被替换成空字符串。
     *
     * @param string
     * @param properties
     * @return
     */
    public static String replaceSharpProperties(String string, Map<String, ?> properties)
    {
        return DealSharpProperties.replaceSharpProperties(string, properties, "");
    }

    /**
     * 替换所有的#{propertyName}.
     *
     * @param string
     * @param properties
     * @param forEmpty   如果不为null，则用于替换所有不存在的属性。
     * @return
     */
    public static String replaceSharpProperties(String string, Map<String, ?> properties, String forEmpty)
    {
        return DealSharpProperties.replaceSharpProperties(string, properties, forEmpty);
    }

}
