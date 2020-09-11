package cn.xishan.oftenporter.servlet.websocket;


import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponseWrapper;

/**
 * @author Created by https://github.com/CLovinr on 2020-09-11.
 */
public interface IContainerResource<T>
{
    /**
     * 获取容器最原始的request或response
     *
     * @param res
     * @return
     */
    T containerRes(T res);


    static <T> T getOrigin(T res)
    {
        if (res instanceof IContainerResource)
        {
            res = ((IContainerResource<T>) res).containerRes(res);
            res = getOrigin(res);//递归获取
        } else if (res instanceof ServletRequestWrapper)
        {
            res = (T) ((ServletRequestWrapper) res).getRequest();
            res = getOrigin(res);//递归获取
        } else if (res instanceof ServletResponseWrapper)
        {
            res = (T) ((ServletResponseWrapper) res).getResponse();
            res = getOrigin(res);//递归获取
        }
        return res;
    }

}
