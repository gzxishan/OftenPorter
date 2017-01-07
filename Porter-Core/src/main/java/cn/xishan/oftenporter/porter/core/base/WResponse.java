package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

import java.io.Closeable;
import java.io.IOException;

/**
 * 用于响应。
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public interface WResponse extends Closeable
{
    void write(@NotNull Object object)throws IOException;

    void close() throws IOException;

}
