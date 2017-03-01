package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.exception.WCallException;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;

/**
 * 接口中间对象。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public abstract class WObject
{

    /**
     * 类必须参数值数组。
     */
    public Object[] cn;
    /**
     * 类非必需参数值数组。
     */
    public Object[] cu;
    public Object[] cinner;
    /**
     * 类参数名称对象。
     */
    public InNames cInNames;

    /**
     * 函数必须参数值数组。
     */
    public Object[] fn;
    /**
     * 函数非必需参数值数组。
     */
    public Object[] fu;
    public Object[] finner;
    /**
     * 函数参数名称对象。
     */
    public InNames fInNames;

    /**
     * 如果当前是rest，则其值为""或非空;否则为null.
     */
    public String restValue;

    /**
     * 用于其他用处。
     */
    public Object _otherObject;

    public abstract WRequest getRequest();

    public abstract WResponse getResponse();

    /**
     * 获取函数上绑定的对象。
     */
    public abstract <T> T finObject(int index);

    /**
     * 获取类上绑定的对象。
     */
    public abstract <T> T cinObject(int index);

    /**
     * 见{@linkplain #savedObject(String) savedObject(Class.getName())}.
     */
    public <T> T savedObject(Class<T> key)
    {
        T obj = savedObject(key.getName());
        return obj;
    }


    /**
     * 获取当前context运行期对象实例。
     * 见{@linkplain PorterConf#addContextAutoSet(String, Object)}
     */
    public abstract <T> T savedObject(String key);


    /**
     * 见{@linkplain #gsavedObject(String) gsavedObject(Class.getName())}.
     */
    public <T> T gsavedObject(Class<T> key)
    {
        T obj = gsavedObject(key.getName());
        return obj;
    }


    /**
     * 获取全局运行期对象.
     * 见{@linkplain CommonMain#addGlobalAutoSet(String, Object)}
     */
    public abstract <T> T gsavedObject(String key);

    public abstract Delivery delivery();

    public abstract UrlDecoder.Result url();

    /**
     * 得到所属的接口框架名称。
     *
     * @return
     */
    public abstract PName getPName();

    public <T> T cnOf(int index)
    {
        T t = (T) cn[index];
        return t;
    }

    public <T> T cuOf(int index)
    {
        T t = (T) cu[index];
        return t;
    }

    public <T> T fnOf(int index)
    {
        T t = (T) fn[index];
        return t;
    }

    public <T> T fuOf(int index)
    {
        T t = (T) fu[index];
        return t;
    }

    /**
     * 使用当前请求的接口方法。
     *
     * @param funTied
     * @param appValues
     * @param callback
     */
    public void currentRequest(String funTied, AppValues appValues, PCallback callback)
    {
        currentRequest(funTied, getRequest().getMethod(), appValues, callback, true);
    }


    private static class Temp
    {
        WCallException exception;
    }

    /**
     * @param funTied
     * @param method
     * @param appValues
     * @param callback
     * @param throwWCallException 是否在返回码不为成功时抛出异常。
     */
    public void currentRequest(String funTied, PortMethod method, AppValues appValues, PCallback callback,
            boolean throwWCallException)
    {
        StringBuilder builder = new StringBuilder();
        UrlDecoder.Result result = url();
        builder.append('/').append(result.contextName()).append('/').append(result.classTied()).append('/');
        builder.append(funTied == null ? "" : funTied);
        PRequest request = PRequest.withNewPath(builder.toString(), method, getRequest(), true);
        request.addParamAll(appValues);
        if (throwWCallException)
        {
            Temp temp = new Temp();
            delivery().currentBridge().request(request, lResponse ->
            {
                Object rs = lResponse.getResponse();
                if (rs != null && rs instanceof JResponse)
                {
                    JResponse jResponse = (JResponse) rs;
                    if (jResponse.isNotSuccess())
                    {
                        temp.exception = new WCallException(jResponse);
                        return;
                    }
                }
                if(callback!=null){
                    callback.onResponse(lResponse);
                }
            });
            if (temp.exception != null)
            {
                throw temp.exception;
            }
        } else
        {
            delivery().currentBridge().request(request, callback);
        }
    }

}
