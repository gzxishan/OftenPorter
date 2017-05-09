package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * Created by chenyg on 2017-04-26.
 */
public interface SyncPorter
{
    <T> T request(WObject wObject);

    <T> T request(WObject wObject, AppValues appValues);

    /**
     * @param wObject
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestSimple(WObject wObject, Object... nameValues);
}
