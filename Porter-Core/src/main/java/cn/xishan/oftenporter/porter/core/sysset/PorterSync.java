package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.PorterSyncOption;
import cn.xishan.oftenporter.porter.core.base.INameValues;
import cn.xishan.oftenporter.porter.core.base.FunParam;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * <pre>
 * 1.配合{@linkplain PorterSyncOption PorterSyncOption}进行相应设置：该设置可以为空，则请求方法为GET，方法名为变量名；当在非接口中使用时，必须含有类绑定名
 * </pre>
 * Created by chenyg on 2017-04-26.
 */
public interface PorterSync
{
    <T> T request(@MayNull OftenObject oftenObject);

    <T> T request(@MayNull OftenObject oftenObject, INameValues INameValues);

    /**
     * @param oftenObject
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestSimple(@MayNull OftenObject oftenObject, Object... nameValues);

    /**
     * 等同于 requestSimple(wObject,objects[0].getClass().getName(),objects[0],FunParam.getName(),FunParam.getValue(),...)。
     * 另见:{@linkplain FunParam}.
     *
     * @param oftenObject
     * @param objects
     * @param <T>
     * @return
     */
    <T> T invokeWithObjects(@MayNull OftenObject oftenObject, Object... objects);

    <T> T requestWNull();

    <T> T requestWNull(INameValues INameValues);

    /**
     * @param nameValues name,value,name,value...
     * @param <T>
     * @return
     */
    <T> T requestWNullSimple(Object... nameValues);
}
