package cn.xishan.oftenporter.porter.core.sysset;

/**
 * @author Created by https://github.com/CLovinr on 2019/1/2.
 */
public interface IAutoVarGetter
{
    <T> T getContextSet(String objectName);

    <T> T getGlobalSet(String objectName);

    <T> T getContextSet(Class<T> objectClass);

    <T> T getGlobalSet(Class<T> objectClass);
}
