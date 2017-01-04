package cn.xishan.oftenporter.bridge.http;

/**
 * Created by 宇宙之灵 on 2016/2/15.
 */
public class HttpOption
{
    public Integer so_timeout=20*1000;
    public Integer conn_timeout=20*1000;
    /**
     * 默认为空，用于改变请求方法
     */
    public HttpMethod method = null;

    public boolean useCookie=true;
}
