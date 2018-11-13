package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;

import java.io.IOException;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/2.
 */
public interface CloseListener
{
    interface  CloseHandle{
        void doClose(Object writeObject) throws IOException;
    }

    /**
     * 调用{@linkplain OftenResponse#close()}时。
     * @param writeObject 写入的对象。
     * @param closeHandle
     */
    void onClose(@MayNull Object writeObject,CloseHandle closeHandle);
}
