package cn.xishan.oftenporter.servlet;

/**
 * @author Created by https://github.com/CLovinr on 2017/6/4.
 */

import cn.xishan.oftenporter.porter.core.ParamSourceHandleManager;
import cn.xishan.oftenporter.porter.core.advanced.ParamSourceHandle;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
public class BodyParamSourceHandle implements ParamSourceHandle {

    /**
     *
     */
    private static final Pattern ENCODE_PATTERN = Pattern.compile("charset=([^;]+)");


    public static void addBodyDealt(PorterConf porterConf) {
        porterConf.addStateListener(new StateListener.Adapter() {
            @Override
            public void beforeSeek(InitParamSource initParamSource, PorterConf porterConf,
                    ParamSourceHandleManager paramSourceHandleManager) {
                paramSourceHandleManager.addByMethod(new BodyParamSourceHandle(), PortMethod.PUT, PortMethod.POST);
            }
        });
    }

    /**
     * 是否解析json对象为参数,默认为true。
     */
    private boolean decodeJsonParams = true;

    public BodyParamSourceHandle() {
    }

    public boolean isDecodeJsonParams() {
        return decodeJsonParams;
    }

    public void setDecodeJsonParams(boolean decodeJsonParams) {
        this.decodeJsonParams = decodeJsonParams;
    }

    /**
     * 得到编码方式，默认为utf-8
     *
     * @param contentType
     * @return
     */
    private String getEncode(String contentType) {
        if (contentType != null) {
            contentType = contentType.toLowerCase();
        } else {
            return "utf-8";
        }

        String encode;
        Matcher matcher = ENCODE_PATTERN.matcher(contentType);
        if (matcher.find()) {
            encode = matcher.group(1);
        } else {
            encode = "utf-8";
        }
        return encode;
    }

    protected void addQueryParams(HttpServletRequest request, Map paramsMap, String encoding) {
        String query = request.getQueryString();
        if (query != null) {
            try {
                paramsMap.putAll(fromEncoding(query, encoding));
            } catch (UnsupportedEncodingException e) {
                throw new OftenCallException(e);
            }
        }
    }

    @Override
    public ParamSource get(OftenObject oftenObject, Class<?> porterClass, Method porterFun) throws Exception {
        HttpServletRequest request = oftenObject.getRequest().getOriginalRequest();
        ParamSource paramSource = null;
        if (request != null) {
            String ctype = request.getContentType();
            if (ctype != null && ctype.contains(ContentType.APP_FORM_URLENCODED.getType())) {
                String encoding = getEncode(ctype);

                String body = FileTool.getString(request.getInputStream());
                if (body == null) {
                    return null;
                }
                Map paramsMap = fromEncoding(body, encoding);
                addQueryParams(request, paramsMap, encoding);
                paramSource = new DefaultParamSource(paramsMap, oftenObject.getRequest());
            } else if (decodeJsonParams) {
                JsonDecodeOption decodeOption = AnnoUtil.getAnnotation(porterFun, porterClass, JsonDecodeOption.class);
                boolean decodeJson = false;
                if (decodeOption == null) {
                    decodeJson = ctype.contains(ContentType.APP_JSON.getType());
                } else {
                    switch (decodeOption.decodeType()) {
                        case Ignore:
                            break;
                        case Force:
                            decodeJson = true;
                            break;
                        case Auto:
                            decodeJson = ContentType.APP_JSON.getType().equals(ctype);
                            break;
                    }
                }

                if (decodeJson) {
                    JSONObject jsonObject = JSON.parseObject(
                            FileTool.getString(request.getInputStream(), 1024, request.getCharacterEncoding()));
                    paramSource = new DefaultParamSource(jsonObject, oftenObject.getRequest());
                }
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
            String encoding) throws UnsupportedEncodingException {
        return OftenStrUtil.fromEncoding(encodingContent, encoding);
    }

}

