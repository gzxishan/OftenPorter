package cn.xishan.oftenporter.porter.core.bridge;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public interface BridgeUrlDecoder
{
    interface Result
    {
        String bridgeName();

        String path();
    }

    Result decode(String fullPath);
}
