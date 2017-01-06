package cn.xishan.oftenporter.bridge.http;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.squareup.okhttp.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by  on 2015/9/12.
 */
public class HttpUtil
{

    private static class OkHttpClientImpl extends OkHttpClient
    {
        public OkHttpClientImpl()
        {
            setHostnameVerifier((s, sslSession) -> true);
            try
            {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                X509TrustManager x509TrustManager = new X509TrustManager()
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
                sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
                setSslSocketFactory(sslContext.getSocketFactory());
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public OkHttpClient setCookieHandler(CookieHandler cookieHandler)
        {
            return this;
        }

        @Override
        public CookieHandler getCookieHandler()
        {
            return null;
        }

        public OkHttpClient _setCookieHandler(CookieHandler cookieHandler)
        {
            return super.setCookieHandler(cookieHandler);
        }
    }


    private static final int SET_CONNECTION_TIMEOUT = 10 * 1000;
    private static final int SET_SOCKET_TIMEOUT = 20 * 1000;
    private static OkHttpClientImpl defaultClient;


    public static void doHttpOption(OkHttpClient okHttpClient, HttpOption httpOption)
    {
        if (httpOption == null)
        {

            return;
        }
        if (httpOption.conn_timeout != null)
        {
            okHttpClient.setConnectTimeout(httpOption.conn_timeout, TimeUnit.MILLISECONDS);
        }

        if (httpOption.so_timeout != null)
        {
            okHttpClient.setReadTimeout(httpOption.so_timeout, TimeUnit.MILLISECONDS);
            okHttpClient.setWriteTimeout(httpOption.so_timeout, TimeUnit.MILLISECONDS);
        }

    }

    private static OkHttpClientImpl _getClient(CookieHandler cookieHandler)
    {

        OkHttpClientImpl okHttpClient = new OkHttpClientImpl();
        okHttpClient.setConnectTimeout(SET_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClient.setReadTimeout(SET_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClient.setWriteTimeout(SET_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
        if (cookieHandler == null)
        {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
            okHttpClient._setCookieHandler(cookieManager);
        } else
        {
            okHttpClient._setCookieHandler(cookieHandler);
        }

        return okHttpClient;
    }


    /**
     * 忽略证书验证。
     *
     * @param cookieHandler 为null时，表示使用默认的对象，并且不支持cookie。
     * @return
     */
    public static synchronized OkHttpClient getClient(CookieHandler cookieHandler)
    {

        if (cookieHandler == null)
        {
            if (defaultClient == null)
            {
                defaultClient = _getClient(null);
            }
            return defaultClient;
        }
        return _getClient(cookieHandler);
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

    private static void addUrlParams(StringBuilder stringBuilder, Map<String, Object> params,
            String encoding) throws UnsupportedEncodingException
    {
        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() != null)
            {
                stringBuilder.append(URLEncoder.encode(entry.getKey(), encoding)).append("=")
                        .append(URLEncoder.encode(String.valueOf(entry.getValue()), encoding)).append('&');
            }
        }
    }

    private static void addUrlParams(StringBuilder stringBuilder, InNames.Name[] names,
            Object[] values, String encoding) throws UnsupportedEncodingException
    {
        if (names == null)
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

    private static String dealUrlParams(WObject wObject, String url) throws UnsupportedEncodingException
    {
        if (wObject == null || (wObject.fInNames == null && wObject.cInNames == null))
        {
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String encoding = "utf-8";

        if (wObject.fInNames != null)
        {
            addUrlParams(stringBuilder, wObject.fInNames.nece, wObject.fn, encoding);
            addUrlParams(stringBuilder, wObject.fInNames.unece, wObject.fu, encoding);
        }
        if (wObject.cInNames != null)
        {
            addUrlParams(stringBuilder, wObject.cInNames.nece, wObject.cn, encoding);
            addUrlParams(stringBuilder, wObject.cInNames.unece, wObject.cu, encoding);
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

    private static String dealUrlParams(Map<String, Object> params, String url) throws UnsupportedEncodingException
    {
        if (params == null)
        {
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String encoding = "utf-8";

        addUrlParams(stringBuilder, params, encoding);

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

    private static void addPostParams(FormEncodingBuilder formEncodingBuilder, Map<String, Object> params)
    {

        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() != null)
            {
                formEncodingBuilder.addEncoded(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    private static RequestBody dealBodyParams(WObject wObject) throws
            UnsupportedEncodingException
    {
        if (wObject == null || (wObject.fInNames == null && wObject.cInNames == null))
        {
            return null;
        }
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();

        if (wObject.fInNames != null)
        {
            addPostParams(formEncodingBuilder, wObject.fInNames.nece, wObject.fn);
            addPostParams(formEncodingBuilder, wObject.fInNames.unece, wObject.fu);
        }
        if (wObject.cInNames != null)
        {
            addPostParams(formEncodingBuilder, wObject.cInNames.nece, wObject.cn);
            addPostParams(formEncodingBuilder, wObject.cInNames.unece, wObject.cu);
        }
        return formEncodingBuilder.build();
    }

    private static void addPostParams(FormEncodingBuilder formEncodingBuilder, InNames.Name[] names, Object[] values)
    {
        if (names == null)
        {
            return;
        }
        for (int i = 0; i < names.length; i++)
        {
            if (values[i] != null)
            {
                formEncodingBuilder.addEncoded(names[i].varName, String.valueOf(values[i]));
            }
        }
    }

    private static RequestBody dealBodyParams(Map<String, Object> params) throws
            UnsupportedEncodingException
    {
        if (params == null)
        {
            return null;
        }
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addPostParams(formEncodingBuilder, params);
        return formEncodingBuilder.build();
    }


    /**
     * 把请求进行转发。
     *
     * @param wObject      可以为null
     * @param httpMethod
     * @param okHttpClient
     * @param url
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response一定为null。
     * @return
     * @throws IOException
     */
    public static Response request(WObject wObject, HttpMethod httpMethod, OkHttpClient okHttpClient,
            String url, Callback callback) throws IOException
    {
        Response response = null;
        try
        {
            Request.Builder builder = new Request.Builder();
            Request request = null;
            if (httpMethod == null)
            {
                httpMethod = HttpMethod.GET;
            }
            switch (httpMethod)
            {

                case PUT:
                {
                    RequestBody requestBody = dealBodyParams(wObject);
                    request = builder.url(url).put(requestBody).build();
                }
                break;
                case POST:
                {
                    RequestBody requestBody = dealBodyParams(wObject);
                    request = builder.url(url).post(requestBody).build();
                }
                break;
                case GET:
                {
                    url = dealUrlParams(wObject, url);
                    request = builder.url(url).get().build();
                }

                break;
                case DELETE:
                {
                    url = dealUrlParams(wObject, url);
                    request = builder.url(url).delete().build();
                }
                break;
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

    /**
     * 把请求进行转发。
     *
     * @param params       可以为null
     * @param httpMethod
     * @param okHttpClient
     * @param url
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response一定为null。
     * @return
     * @throws IOException
     */
    public static Response request(Map<String, Object> params, HttpMethod httpMethod, OkHttpClient okHttpClient,
            String url, Callback callback) throws IOException
    {
        Response response = null;
        try
        {
            Request.Builder builder = new Request.Builder();
            Request request = null;
            if (httpMethod == null)
            {
                httpMethod = HttpMethod.GET;
            }
            switch (httpMethod)
            {

                case PUT:
                {
                    RequestBody requestBody = dealBodyParams(params);
                    request = builder.url(url).put(requestBody).build();
                }
                break;
                case POST:
                {
                    RequestBody requestBody = dealBodyParams(params);
                    request = builder.url(url).post(requestBody).build();
                }
                break;
                case GET:
                {
                    url = dealUrlParams(params, url);
                    request = builder.url(url).get().build();
                }

                break;
                case DELETE:
                {
                    url = dealUrlParams(params, url);
                    request = builder.url(url).delete().build();
                }
                break;
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
            WPTool.close(responseBody);
        }
        return jResponse;
    }

    /**
     * 把数据发向服务器，并接受响应结果。（同步的）
     *
     * @param params       可以为null
     * @param httpMethod   向服务器发起的请求方法
     * @param okHttpClient
     * @param url          url地址
     * @param jrCallback
     * @return
     */
    public static JResponse requestWPorter(Map<String, Object> params, HttpMethod httpMethod, OkHttpClient okHttpClient,
            String url, final JRCallback jrCallback)
    {
        JResponse jResponse = null;
        try
        {

            if (jrCallback == null)
            {
                Response response = request(params, httpMethod, okHttpClient, url, null);
                jResponse = toJResponse(response);
            } else
            {
                request(params, httpMethod, okHttpClient, url, new Callback()
                {
                    @Override
                    public void onFailure(Request request, IOException e)
                    {
                        jrCallback.onResult(onIOException(e));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException
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

    /**
     * 把数据发向服务器，并接受响应结果。（同步的）
     *
     * @param wObject      可以为null
     * @param httpMethod   向服务器发起的请求方法
     * @param okHttpClient
     * @param url          url地址
     * @param jrCallback
     * @return
     */
    public static JResponse requestWPorter(WObject wObject, HttpMethod httpMethod, OkHttpClient okHttpClient,
            String url, final JRCallback jrCallback)
    {
        JResponse jResponse = null;
        try
        {

            if (jrCallback == null)
            {
                Response response = request(wObject, httpMethod, okHttpClient, url, null);
                jResponse = toJResponse(response);
            } else
            {
                request(wObject, httpMethod, okHttpClient, url, new Callback()
                {
                    @Override
                    public void onFailure(Request request, IOException e)
                    {
                        jrCallback.onResult(onIOException(e));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException
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
