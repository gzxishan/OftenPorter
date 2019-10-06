package cn.xishan.oftenporter.porter.core.base;

import java.io.Closeable;

/**
 * @author Created by https://github.com/CLovinr on 2019/10/6.
 */
public interface ICloseable extends Closeable
{
    void close();
}
