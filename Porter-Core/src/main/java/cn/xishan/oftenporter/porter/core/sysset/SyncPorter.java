package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.SyncPorterOption;
import cn.xishan.oftenporter.porter.core.base.ABOption;
import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * <pre>
 * 1.配合{@linkplain SyncPorterOption SyncPorterOption}进行相应设置：该设置可以为空，则请求方法为GET，方法名为变量名；当在非接口中使用时，必须含有类绑定名
 * 2.调用的{@linkplain ABOption#portFunType ABOption.portFunType}为{@linkplain PortFunType#INNER PortFunType.INNER}
 * </pre>
 * Created by chenyg on 2017-04-26.
 */
public interface SyncPorter {
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
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestWNullSimple(Object... nameValues);
}
