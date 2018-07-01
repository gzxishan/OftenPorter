package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/22.
 */
public class PreRequest {
    public Context context;
    public UrlDecoder.Result result;

    public Porter classPort;
    public PorterOfFun funPort;

    public PreRequest(Context context, UrlDecoder.Result result, Porter classPort, PorterOfFun funPort) {
        this.context = context;
        this.result = result;
        this.classPort = classPort;
        this.funPort = funPort;
    }
}
