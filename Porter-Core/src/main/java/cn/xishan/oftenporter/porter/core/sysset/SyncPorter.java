package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * Created by chenyg on 2017-04-26.
 */
public interface SyncPorter
{
    <T> T request(@MayNull WObject wObject);

    <T> T request(@MayNull WObject wObject, AppValues appValues);

    /**
     * @param wObject
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestSimple(@MayNull WObject wObject, Object... nameValues);

    <T> T requestWNull();
    <T> T requestWNull(AppValues appValues);

    /**
     *
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestWNullSimple(Object... nameValues);
}
