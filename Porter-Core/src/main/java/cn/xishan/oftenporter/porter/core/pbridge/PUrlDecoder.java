package cn.xishan.oftenporter.porter.core.pbridge;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public interface PUrlDecoder
{
    public interface Result
    {
        String pName();

        String path();
    }

    Result decode(String fullPath);
}
