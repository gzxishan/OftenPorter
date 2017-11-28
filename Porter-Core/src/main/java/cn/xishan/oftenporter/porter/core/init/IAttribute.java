package cn.xishan.oftenporter.porter.core.init;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public interface IAttribute
{
    IAttribute setAttribute(String key, Object value);

    <T> T getAttribute(String key);

    <T> T removeAttribute(String key);
}
