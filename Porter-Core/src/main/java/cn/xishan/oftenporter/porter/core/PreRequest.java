package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/22.
 */
public class PreRequest
{
    public interface IsOk
    {
        boolean isOk(PreRequest request);
    }

    public final Context context;
    public final UrlDecoder.Result result;

    public final Porter classPort;
    public final PorterOfFun funPort;
    private IsOk isok;

    public PreRequest(Context context, UrlDecoder.Result result, Porter classPort, PorterOfFun funPort, IsOk isOk)
    {
        this.context = context;
        this.result = result;
        this.classPort = classPort;
        this.funPort = funPort;
        this.isok = isOk;
    }

    public boolean isOk()
    {
        return isok.isOk(this);
    }
}
