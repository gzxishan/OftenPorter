package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的地址解析器:/contextName/classTied/[funTied|restValue][=*=参数][?name1=value1&name2=value2]
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class DefaultUrlDecoder implements UrlDecoder
{
    private String encoding;
    private final Logger LOGGER = LogUtil.logger(DefaultUrlDecoder.class);

    public static final String STAR_PARAM_KEY = "=*=";

    public DefaultUrlDecoder(String encoding)
    {
        this.encoding = encoding;
    }

    @Override
    public Result decode(String path)
    {
        int index = path.indexOf('?');

        String tiedPath = index == -1 ? path : path.substring(0, index);

        if (!tiedPath.startsWith("/"))
        {
            return null;
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
            try
            {
                funTied = URLDecoder.decode(tiedPath.substring(forwardSlash + 1), encoding);
            } catch (UnsupportedEncodingException e)
            {
                LOGGER.warn(e.getMessage(), e);
                return null;
            }
        }


        Map<String, Object> params;
        if (index != -1)
        {
            int sharpIndex = path.indexOf("#", index);
            if (sharpIndex == -1)
            {
                sharpIndex = path.length();
            }
            params = decodeParam(path.substring(index + 1, sharpIndex), encoding);
        } else
        {
            params = new HashMap<>(0);
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
