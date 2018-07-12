package cn.xishan.oftenporter.servlet;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/4.
 */

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.ParamSourceHandle;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * put方法的，用于解析application/x-www-form-urlencoded的方法体
 *
 * @author ZhuiFeng
 */
public class PutParamSourceHandle implements ParamSourceHandle
{

    /**
     *
     */
    private static final Pattern ENCODE_PATTERN = Pattern.compile("charset=([^;]+)");

    public static void addPutDealt(PorterConf porterConf)
    {
        porterConf.addStateListener(new StateListener.Adapter()
        {
            @Override
            public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
                    ParamSourceHandleManager paramSourceHandleManager)
            {
                paramSourceHandleManager.addByMethod(new PutParamSourceHandle(), PortMethod.PUT);
            }
        });
    }

    /**
     * 得到编码方式，默认为utf-8
     *
     * @param contentType
     * @return
     */
    private String getEncode(String contentType)
    {
        if (contentType != null)
        {
            contentType = contentType.toLowerCase();
        } else
        {
            return "utf-8";
        }
        String encode;
        Matcher matcher = ENCODE_PATTERN.matcher(contentType);
        if (matcher.find())
        {
            encode = matcher.group(1);
        } else
        {
            encode = "utf-8";
        }
        return encode;
    }

    @Override
    public ParamSource get(WObject wObject, Class<?> porterClass, Method porterFun) throws Exception
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();

        String ctype = request.getContentType();
        if (ctype != null && ctype.indexOf(ContentType.APP_FORM_URLENCODED.getType()) != -1)
        {
            String encode = getEncode(ctype);

            String body = FileTool.getString(request.getInputStream());
            if (body == null)
            {
                return null;
            }
            Map paramsMap = fromEncoding(body, encode);
            ParamSource paramSource = new DefaultParamSource(paramsMap, wObject.getRequest());
            return paramSource;
        }
        return null;
    }

    public static Map<String, String> fromEncoding(String encodingContent,
            String encoding) throws UnsupportedEncodingException
    {
        String[] strs = StrUtil.split(encodingContent, "&");
        HashMap<String, String> paramsMap = new HashMap<>(strs.length);
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

}

