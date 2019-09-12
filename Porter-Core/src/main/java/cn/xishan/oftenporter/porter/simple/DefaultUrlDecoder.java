package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的地址解析器:/contextName/classTied[/**=name1=value1&name2=value2][/funTied][=*=参数][?name1=value1&name2=value2]
 * <p>
 * 其中funTied可以包括“/”的字符,如:/often/Hello/util/base.js解析的funTied为util/base.js。
 * </p>
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class DefaultUrlDecoder implements UrlDecoder
{
    private String encoding;
    private final Logger LOGGER = LogUtil.logger(DefaultUrlDecoder.class);

    public static final String STAR_PARAM_KEY = "=*=";

    private static final Pattern STAR_PARAM2_PATTERN = Pattern.compile("/\\*\\*=([^/\\?#]*)");


    public DefaultUrlDecoder(String encoding)
    {
        this.encoding = encoding;
    }

    @Override
    public Result decode(String path)
    {
        if (!path.startsWith("/"))
        {
            return null;
        }
        String queryStr = "";
        String queryStr2 = "";

        Matcher matcher = STAR_PARAM2_PATTERN.matcher(path);
        if (matcher.find())
        {
            queryStr2 = matcher.group(1);
            path = path.substring(0, matcher.start()) + path.substring(matcher.end());
        }

        int queryIndex = path.indexOf('?');
        String tiedPath;
        if (queryIndex == -1)
        {
            tiedPath = path;
        } else
        {
            tiedPath = path.substring(0, queryIndex);
            queryStr = path.substring(queryIndex + 1);
        }

        int starParamIndex = tiedPath.indexOf(STAR_PARAM_KEY);
        String starParam = null;
        if (starParamIndex > 0)
        {
            starParam = tiedPath.substring(starParamIndex + STAR_PARAM_KEY.length());
            tiedPath = tiedPath.substring(0, starParamIndex);
        }

        int forwardSlash = tiedPath.indexOf('/', 1);
        String contextName, classTied, funTied;

        if (forwardSlash == -1)
        {
            return null;
        } else
        {
            contextName = tiedPath.substring(1, forwardSlash);
        }
        tiedPath = tiedPath.substring(forwardSlash);

        forwardSlash = tiedPath.indexOf('/', 1);
        if (forwardSlash == -1)
        {
            classTied = tiedPath.substring(1);
            funTied = "";
        } else
        {
            classTied = tiedPath.substring(1, forwardSlash);
//            try
//            {
//                funTied = URLDecoder.decode(tiedPath.substring(forwardSlash + 1), encoding);
//            } catch (UnsupportedEncodingException e)
//            {
//                LOGGER.warn(e.getMessage(), e);
//                return null;
//            }
            funTied=tiedPath.substring(forwardSlash + 1);
        }


        Map<String, Object> params;

        //先处理queryStr2，低优先级
        if (OftenTool.notEmpty(queryStr2))
        {
            params = decodeParam(queryStr2, encoding);
        } else
        {
            params = new HashMap<>(0);
        }

        if (OftenTool.notEmpty(queryStr))
        {
            int sharpIndex = queryStr.indexOf("#");
            if (sharpIndex == -1)
            {
                sharpIndex = queryStr.length();
            }
            Map<String, Object> map = decodeParam(queryStr.substring(0, sharpIndex), encoding);
            params.putAll(map);
        }
        if (starParam != null)
        {
            params.put(STAR_PARAM_KEY, starParam);
        }
        DefaultUrlResult result = new DefaultUrlResult(params, contextName, classTied, funTied);
        return result;
    }

    /**
     * 把name=value&name2=value2格式的内容解析成map。
     *
     * @param content     内容
     * @param urlEncoding 编码那个是
     * @return 解析的map。
     */
    public Map<String, Object> decodeParam(String content, String urlEncoding)
    {
        Map<String, Object> params;
        params = new HashMap<>();
        String[] strs = OftenStrUtil.split(content, "&");
        try
        {
            for (String string : strs)
            {
                int index = string.indexOf('=');
                if (index != -1)
                {
                    params.put(URLDecoder.decode(string.substring(0, index), urlEncoding),
                            URLDecoder.decode(string.substring(index + 1), urlEncoding));
                }
            }
        } catch (UnsupportedEncodingException e)
        {
            LOGGER.debug(e.getMessage(), e);
        }
        return params;
    }

    public static Result newResult(Map<String, Object> params, String contextName, String classTied, String funTied)
    {
        return new DefaultUrlResult(params, contextName, classTied, funTied);
    }

}
