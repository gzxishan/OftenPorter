package cn.xishan.oftenporter.bridge.http;

import cn.xishan.oftenporter.servlet.ContentType;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2017/8/10.
 */
public class RequestData
{
    private ContentType contentType = ContentType.APP_FORM_URLENCODED;
    private Object bodyContent;
    private String encoding;
    private Map<String, String> headers;
    private Map<String, Object> params;

    public RequestData()
    {
    }

    public RequestData(Map<String, Object> params)
    {
        this.params = params;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public RequestData putParam(String name, Object value)
    {
        if (params == null)
        {
            params = new HashMap<>();
        }
        params.put(name, value);
        return this;
    }

    public RequestData putHeader(String name, String value)
    {
        if (headers == null)
        {
            headers = new HashMap<>();
        }
        headers.put(name, value);
        return this;
    }

    public ContentType getContentType()
    {
        return contentType;
    }

    public void setContentType(ContentType contentType)
    {
        this.contentType = contentType;
    }

    public Object getBodyContent()
    {
        return bodyContent;
    }

    public void setBodyContent(InputStream content)
    {
        this.bodyContent = content;
    }

    public void setBodyContent(File content)
    {
        this.bodyContent = content;
    }

    public void setBodyContent(byte[] content)
    {
        this.bodyContent = content;
    }

    public void setBodyContent(String bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
}
