package cn.xishan.oftenporter.bridge.http;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 宇宙之灵 on 2015/9/12.
 */
public class HttpUtil
{

    private static final int SET_CONNECTION_TIMEOUT = 20 * 1000;
    private static final int SET_SOCKET_TIMEOUT = 60 * 1000;
    private static OkHttpClient defaultClient;


    private static OkHttpClient _getClient(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager)
    {

        try
        {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (x509TrustManager == null)
            {
                x509TrustManager = new X509TrustManager()
                {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates,
                            String s) throws CertificateException
                    {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates,
                            String s) throws CertificateException
                    {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }
                };
            }

            if (sslSocketFactory == null)
            {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
                sslSocketFactory = sslContext.getSocketFactory();
            }
            builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
            builder.connectTimeout(SET_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            builder.readTimeout(SET_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
            builder.writeTimeout(SET_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
            OkHttpClient okHttpClient = builder.build();

            return okHttpClient;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * 忽略证书验证。
     *
     * @return
     */
    public static synchronized OkHttpClient getClient()
    {
        return getClient(null, null);
    }

    /**
     * 可以设置双向认证。
     *
     * @return
     */
    public static synchronized OkHttpClient getClient(SSLSocketFactory sslSocketFactory,
            X509TrustManager x509TrustManager)
    {

        if (sslSocketFactory == null)
        {
            if (defaultClient == null)
            {
                defaultClient = _getClient(null, x509TrustManager);
            }
            return defaultClient;
        }
        return _getClient(sslSocketFactory, x509TrustManager);
    }


    /**
     * 移除末尾指定的字符(若存在的话)。
     *
     * @param sb
     * @param c  要移除的字符
     */
    private static void removeEndChar(StringBuilder sb, char c)
    {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == c)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    /**
     * 添加地址参数
     *
     * @param url        地址
     * @param nameValues 如name=123&age=12
     * @param afterSharp 是否放在#后面
     */
    public static String addUrlParam(String url, String nameValues, boolean afterSharp)
    {
        int index = url.lastIndexOf(afterSharp ? "#" : "?");
        if (index == -1)
        {
            return url + (afterSharp ? "#" : "?") + nameValues;
        } else
        {
            return url + "&" + nameValues;
        }
    }

    private static void addUrlParams(StringBuilder stringBuilder, InNames.Name[] names,
            Object[] values, String encoding) throws UnsupportedEncodingException
    {
        if (names == null || values == null)
        {
            return;
        }
        for (int i = 0; i < names.length; i++)
        {
            if (values[i] != null)
            {
                stringBuilder.append(URLEncoder.encode(names[i].varName, encoding)).append("=")
                        .append(URLEncoder.encode(values[i] + "", encoding)).append('&');
            }
        }
    }

    private static String dealUrlParams(OftenObject oftenObject, String url) throws UnsupportedEncodingException
    {
        if (oftenObject == null || (oftenObject.fInNames == null && oftenObject.cInNames == null))
        {
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String encoding = "utf-8";

        if (oftenObject.fInNames != null)
        {
            addUrlParams(stringBuilder, oftenObject.fInNames.nece, oftenObject.fn, encoding);
            addUrlParams(stringBuilder, oftenObject.fInNames.unece, oftenObject.fu, encoding);
        }
        if (oftenObject.cInNames != null)
        {
            addUrlParams(stringBuilder, oftenObject.cInNames.nece, oftenObject.cn, encoding);
            addUrlParams(stringBuilder, oftenObject.cInNames.unece, oftenObject.cu, encoding);
        }

        if (stringBuilder.length() > 0)
        {
            removeEndChar(stringBuilder, '&');
            if (url.indexOf('?') == -1)
            {
                url += "?" + stringBuilder;
            } else
            {
                url += "&" + stringBuilder;
            }
        }
        return url;
    }


    private static void addPostParams(FormBody.Builder builder, InNames.Name[] names, Object[] values)
    {
        if (names == null || values == null)
        {
            return;
        }
        for (int i = 0; i < names.length; i++)
        {
            if (values[i] != null)
            {
                builder.addEncoded(names[i].varName, String.valueOf(values[i]));
            }
        }
    }

    private static RequestBody dealBodyParams(OftenObject oftenObject) throws
            UnsupportedEncodingException
    {
        FormBody.Builder builder = new FormBody.Builder();
        if (!(oftenObject == null || (oftenObject.fInNames == null && oftenObject.cInNames == null)))
        {
            addPostParams(builder, oftenObject.fInNames.nece, oftenObject.fn);
            addPostParams(builder, oftenObject.fInNames.unece, oftenObject.fu);
            addPostParams(builder, oftenObject.cInNames.nece, oftenObject.cn);
            addPostParams(builder, oftenObject.cInNames.unece, oftenObject.cu);
        }

        return builder.build();
    }


    /**
     * 把请求进行转发。
     *
     * @param requestData
     * @param method
     * @param okHttpClient
     * @param url
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response一定为null。
     * @return
     * @throws IOException
     */
    public static Response request(@NotNull RequestData requestData, PortMethod method,
            @MayNull OkHttpClient okHttpClient,
            String url, Callback callback) throws IOException
    {
        return request(new OftenObjectImpl(requestData), method, okHttpClient, url, callback);
    }

    /**
     * 把请求进行转发。
     *
     * @param oftenObject      可以为null
     * @param method
     * @param okHttpClient
     * @param url
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response一定为null。
     * @return
     * @throws IOException
     */
    public static Response request(@MayNull OftenObject oftenObject, PortMethod method, @MayNull OkHttpClient okHttpClient,
            String url, Callback callback) throws IOException
    {
        if (okHttpClient == null)
        {
            okHttpClient = getClient(null, null);
        }
        Response response = null;
        try
        {
            Request.Builder builder = new Request.Builder();
            Request request;
            if (method == null)
            {
                method = PortMethod.GET;
            }
            switch (method)
            {

                case PUT:
                {
                    RequestBody requestBody = dealBodyParams(oftenObject);
                    request = builder.url(url).put(requestBody).build();
                }
                break;
                case POST:
                {
                    RequestBody requestBody = dealBodyParams(oftenObject);
                    request = builder.url(url).post(requestBody).build();
                }
                break;
                case GET:
                {
                    url = dealUrlParams(oftenObject, url);
                    request = builder.url(url).get().build();
                }

                break;
                case DELETE:
                {
                    url = dealUrlParams(oftenObject, url);
                    request = builder.url(url).delete().build();
                }
                break;
                default:
                    throw new RuntimeException("not support:" + method);
            }
            if (callback == null)
            {
                response = okHttpClient.newCall(request).execute();
            } else
            {
                okHttpClient.newCall(request).enqueue(callback);
            }
        } catch (IOException e)
        {
            throw e;
        }

        return response;
    }

    public static JResponse requestWPorter(@MayNull RequestData requestData, PortMethod method,
            @MayNull OkHttpClient okHttpClient, String url, JRCallback jrCallback)
    {
        return requestWPorter(requestData == null ? null : new OftenObjectImpl(requestData), method, okHttpClient, url,
                jrCallback);
    }


    private static JResponse toJResponse(Response response)
    {
        JResponse jResponse;
        ResponseBody responseBody = null;
        try
        {
            int code = response.code();
            if (code == 200 || code == 201)
            {
                responseBody = response.body();
                String json = responseBody.string();
                jResponse = JResponse.fromJSON(json);
            } else if (code == 204)
            {
                jResponse = new JResponse(ResultCode.SUCCESS);
            } else
            {
                jResponse = new JResponse(ResultCode.toResponseCode(code));
                jResponse.setDescription(response.message());
            }
        } catch (IOException e)
        {
            jResponse = onIOException(e);
        } catch (JResponse.JResponseFormatException e)
        {
            jResponse = new JResponse();
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription(e.toString());
            jResponse.setExCause(e);
        } finally
        {
            OftenTool.close(responseBody);
        }
        return jResponse;
    }

    /**
     * 把数据发向服务器，并接受响应结果。（同步的）
     *
     * @param oftenObject      可以为null
     * @param method       像服务器发起的请求方法
     * @param okHttpClient
     * @param url          url地址
     * @param jrCallback
     * @return
     */
    public static JResponse requestWPorter(@MayNull OftenObject oftenObject, PortMethod method,
            @MayNull OkHttpClient okHttpClient,
            String url, final JRCallback jrCallback)
    {
        JResponse jResponse = null;
        try
        {

            if (jrCallback == null)
            {
                Response response = request(oftenObject, method, okHttpClient, url, null);
                jResponse = toJResponse(response);
            } else
            {
                request(oftenObject, method, okHttpClient, url, new Callback()
                {
                    @Override
                    public void onFailure(Call call, IOException e)
                    {
                        jrCallback.onResult(onIOException(e));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException
                    {
                        jrCallback.onResult(toJResponse(response));
                    }

                });
            }
        } catch (IOException e)
        {
            jResponse = onIOException(e);
            if (jrCallback != null)
            {
                jrCallback.onResult(jResponse);
            }
        }
        return jResponse;
    }

    private static JResponse onIOException(IOException e)
    {
        JResponse jResponse = new JResponse();
        jResponse.setCode(ResultCode.NET_EXCEPTION);
        jResponse.setDescription(e.toString());
        jResponse.setExCause(e);
        return jResponse;
    }


}


/**
 * Created by chenyg on 2017-03-07.
 */
class OftenObjectImpl extends OftenObject
{
    private Map<String, String> headers;

    public OftenObjectImpl(RequestData requestData)
    {
        String[] names = new String[requestData.params.size()];
        Object[] values = new Object[names.length];

        int k = 0;
        for (Map.Entry<String, Object> entry : requestData.params.entrySet())
        {
            names[k] = entry.getKey();
            values[k++] = entry.getValue();
        }
        fInNames = InNames.fromStringArray(null, names, null);
        fu = values;
        this.headers = requestData.headers;
    }

    @Override
    public OftenRequest getRequest()
    {
        return null;
    }

    @Override
    public OftenResponse getResponse()
    {
        return null;
    }

    @Override
    public ParamSource getParamSource()
    {
        return null;
    }

    @Override
    public boolean isInnerRequest()
    {
        return false;
    }

    @Override
    public <T> T fentity(int index)
    {
        return null;
    }

    @Override
    public <T> T centity(int index)
    {
        return null;
    }


    @Override
    public <T> T savedObject(String key)
    {
        return null;
    }

    @Override
    public <T> T gsavedObject(String key)
    {
        return null;
    }

    @Override
    public Delivery delivery()
    {
        return null;
    }

    @Override
    public UrlDecoder.Result url()
    {
        return null;
    }

    @Override
    public BridgeName getPName()
    {
        return null;
    }
}
