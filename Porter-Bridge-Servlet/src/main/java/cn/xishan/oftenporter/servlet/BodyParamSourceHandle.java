package cn.xishan.oftenporter.servlet;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/4.
 */

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.ParamSourceHandle;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.init.InitParamSource;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * put方法的，用于解析application/x-www-form-urlencoded的方法体
 *
 * @author ZhuiFeng
 */
public class BodyParamSourceHandle implements ParamSourceHandle
{

    /**
     *
     */
    private static final Pattern ENCODE_PATTERN = Pattern.compile("charset=([^;]+)");

    public static void addBodyDealt(PorterConf porterConf)
    {
        porterConf.addStateListener(new StateListener.Adapter()
        {
            @Override
            public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
                    ParamSourceHandleManager paramSourceHandleManager)
            {
                paramSourceHandleManager.addByMethod(new BodyParamSourceHandle(), PortMethod.PUT, PortMethod.POST);
            }
        });
    }

    public BodyParamSourceHandle()
    {
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

    protected void addQueryParams(HttpServletRequest request, Map paramsMap, String encoding)
    {
        String query = request.getQueryString();
        if (query != null)
        {
            try
            {
                paramsMap.putAll(fromEncoding(query, encoding));
            } catch (UnsupportedEncodingException e)
            {
                throw new OftenCallException(e);
            }
        }
    }

    @Override
    public ParamSource get(OftenObject oftenObject, Class<?> porterClass, Method porterFun) throws Exception
    {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        ParamSource paramSource = null;
        if (request != null)
        {
            String ctype = request.getContentType();
            if (ctype != null && ctype.contains(ContentType.APP_FORM_URLENCODED.getType()))
            {
                String encoding = getEncode(ctype);

                String body = FileTool.getString(request.getInputStream());
                if (body == null)
                {
                    return null;
                }
                Map paramsMap = fromEncoding(body, encoding);
                addQueryParams(request, paramsMap, encoding);
                paramSource = new DefaultParamSource(paramsMap, oftenObject.getRequest());
            }
        }

        return paramSource;
    }

    /**
     * 见{@linkplain OftenStrUtil#fromEncoding(String, String)}
     *
     * @param encodingContent
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> fromEncoding(String encodingContent,
            String encoding) throws UnsupportedEncodingException
    {
        return OftenStrUtil.fromEncoding(encodingContent, encoding);
    }

}

