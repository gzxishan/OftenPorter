package cn.xishan.oftenporter.bridge.http;


import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by 宇宙之灵 on 2015/9/12.
 */
public class HttpUtil
{

    private static final int SET_CONNECTION_TIMEOUT = 30 * 1000;
    private static final int SET_SOCKET_TIMEOUT = 60 * 1000;
    private static OkHttpClient defaultClient;


    private static OkHttpClient _getClient(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager,
            boolean hasCookie)
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

            if (hasCookie)
            {
                builder.cookieJar(new CookieJar()
                {
                    private final Map<HttpUrl, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
                    {
                        cookieStore.put(url, cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url)
                    {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : Collections.emptyList();
                    }
                });
            } else
            {
                builder.cookieJar(CookieJar.NO_COOKIES);
            }

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
        return getClient(null, null, false);
    }

    /**
     * 可以设置双向认证。
     *
     * @return
     */
    public static synchronized OkHttpClient getClient(SSLSocketFactory sslSocketFactory,
            X509TrustManager x509TrustManager, boolean hasCookie)
    {

        if (sslSocketFactory == null)
        {
            if (defaultClient == null)
            {
                defaultClient = _getClient(null, x509TrustManager, hasCookie);
            }
            return defaultClient;
        }
        return _getClient(sslSocketFactory, x509TrustManager, hasCookie);
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
        if (oftenObject == null || (oftenObject._fInNames == null && oftenObject._cInNames == null))
        {
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String encoding = "utf-8";

        if (oftenObject._fInNames != null)
        {
            addUrlParams(stringBuilder, oftenObject._fInNames.nece, oftenObject._fn, encoding);
            addUrlParams(stringBuilder, oftenObject._fInNames.unece, oftenObject._fu, encoding);
        }
        if (oftenObject._cInNames != null)
        {
            addUrlParams(stringBuilder, oftenObject._cInNames.nece, oftenObject._cn, encoding);
            addUrlParams(stringBuilder, oftenObject._cInNames.unece, oftenObject._cu, encoding);
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


    private static void addFormParams(FormBody.Builder builder, InNames.Name[] names, Object[] values)
    {
        if (names == null || values == null)
        {
            return;
        }
        for (int i = 0; i < names.length; i++)
        {
            if (values[i] != null)
            {
                builder.add(names[i].varName, String.valueOf(values[i]));
            }
        }
    }

    private static RequestBody dealBodyParams(OftenObject oftenObject)
    {
        FormBody.Builder builder = new FormBody.Builder();
        if (oftenObject != null)
        {
            if (oftenObject._fInNames != null)
            {
                addFormParams(builder, oftenObject._fInNames.nece, oftenObject._fn);
                addFormParams(builder, oftenObject._fInNames.unece, oftenObject._fu);
            }
            if (oftenObject._cInNames != null)
            {
                addFormParams(builder, oftenObject._cInNames.nece, oftenObject._cn);
                addFormParams(builder, oftenObject._cInNames.unece, oftenObject._cu);
            }
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
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response为null。
     * @return
     * @throws IOException
     */
    public static Response request(@NotNull RequestData requestData, PortMethod method,
            @MayNull OkHttpClient okHttpClient,
            String url, Callback callback) throws IOException
    {
        return request(requestData == null ? null : new OftenObjectImpl(requestData), method, okHttpClient, url,
                callback);
    }

    /**
     * 把请求进行转发。
     *
     * @param oftenObject  可以为null
     * @param method
     * @param okHttpClient
     * @param url
     * @param callback     为null表示同步,则返回response；否则表示异步，返回的response为null。
     * @return
     * @throws IOException
     */
    public static Response request(@MayNull OftenObject oftenObject, PortMethod method,
            @MayNull OkHttpClient okHttpClient, String url, Callback callback) throws IOException
    {
        if (okHttpClient == null)
        {
            okHttpClient = getClient(null, null, false);
        }
        Response response = null;
        try
        {
            Request.Builder builder = new Request.Builder();
            if (oftenObject instanceof OftenObjectImpl)
            {
                OftenObjectImpl impl = (OftenObjectImpl) oftenObject;
                Map<String, String> headers = impl.getHeaders();
                if (OftenTool.notEmptyOf(headers))
                {
                    for (Map.Entry<String, String> entry : headers.entrySet())
                    {
                        builder.addHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
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

    public static JResponse requestJResponse(@MayNull RequestData requestData, PortMethod method,
            @MayNull OkHttpClient okHttpClient, String url, JRCallback jrCallback)
    {
        return requestJResponse(requestData == null ? null : new OftenObjectImpl(requestData), method, okHttpClient,
                url,
                jrCallback);
    }

    public static String requestString(@MayNull RequestData requestData, PortMethod method,
            @MayNull OkHttpClient okHttpClient, String url)
    {
        ResponseBody responseBody = null;
        Response response = null;
        try
        {
            response = request(requestData, method, okHttpClient, url, null);
            int code = response.code();
            if (code == 200 || code == 201)
            {
                responseBody = response.body();
                return responseBody.string();
            } else if (code == 204)
            {
                return null;
            } else
            {
                throw new OftenCallException("errcode=" + code + ",msg=" + response.message());
            }
        } catch (OftenCallException e)
        {
            throw e;
        } catch (Throwable e)
        {
            throw new OftenCallException(e);
        } finally
        {
            OftenTool.close(responseBody);
            OftenTool.close(response);
        }
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
                jResponse = new JResponse(code);
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
            OftenTool.close(response);
        }
        return jResponse;
    }

    /**
     * 把数据发向服务器，并接受响应结果。（同步的）
     *
     * @param oftenObject  可以为null
     * @param method       像服务器发起的请求方法
     * @param okHttpClient
     * @param url          url地址
     * @param jrCallback
     * @return
     */
    public static JResponse requestJResponse(@MayNull OftenObject oftenObject, PortMethod method,
            @MayNull OkHttpClient okHttpClient, String url, final JRCallback jrCallback)
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

