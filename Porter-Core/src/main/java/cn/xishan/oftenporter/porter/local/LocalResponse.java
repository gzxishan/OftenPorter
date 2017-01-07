package cn.xishan.oftenporter.porter.local;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.WResponse;
import cn.xishan.oftenporter.porter.core.pbridge.PCallback;

import java.io.IOException;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class LocalResponse implements WResponse {
    private Object object;
    private PCallback callback;

    public LocalResponse(PCallback callback) {
        this.callback = callback;
    }

    @Override
    public void write(@NotNull Object object) throws IOException {
        if (this.object != null) {
            throw new IOException("already write before!");
        }
        this.object = object;
    }

    @Override
    public void close() throws IOException {
        if (callback != null) {
            callback.onResponse(object == null ? null : new LResponse(object));
        }
    }

}
