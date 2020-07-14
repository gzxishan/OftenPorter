package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.Context;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.DuringType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;

/**
 * @author Created by https://github.com/CLovinr on 2019-12-19.
 */
public interface CheckerBuilder
{
    interface Checker
    {
        void check();
    }

    interface Handle
    {
        void handle(Object failedObject);
    }

    /**
     * 当只有一个context时，会自动设置该context。
     * @param duringType
     * @return
     */
    Builder newBuilder(DuringType duringType);

    Builder newBuilder(DuringType duringType, String contextName);

    BridgeName getBridgeName();


    interface Builder
    {
        Builder setObject(OftenObject object, Object handleObject, Object handleMethod);

        Builder setObject(OftenObject object);


        Builder setPorterOfFun(PorterOfFun porterOfFun);


        Builder setContext(Context context);

        Builder setHandle(Handle handle);

        Builder withThrowable(UrlDecoder.Result url, Throwable throwable);

        Builder withUrl(UrlDecoder.Result url);

        Builder withReturn(UrlDecoder.Result url, Object returnObject);

        Builder setOutType(OutType outType);

        Checker build();
    }
}
