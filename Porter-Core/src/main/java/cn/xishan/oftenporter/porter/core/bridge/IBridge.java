package cn.xishan.oftenporter.porter.core.bridge;

/**
 * 用于访问可达的实例。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public interface IBridge
{
    void request(BridgeRequest request, BridgeCallback callback);
}
