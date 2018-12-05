package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * 对{@linkplain PortFunType#FAST_INNER}与{@linkplain PortFunType#INNER}无效。
 * @author Created by https://github.com/CLovinr on 2018-09-11.
 */
public interface ResponseHandle
{
    /**
     *
     * @param oftenObject
     * @param porterOfFun
     * @param object
     * @return true,已经进行了输出（不会调用close，需要自己关闭连接）；false，没有进行输出（可能会调用close）。
     * @throws Throwable
     */
    boolean hasDoneWrite(OftenObject oftenObject, PorterOfFun porterOfFun, @NotNull Object object)throws Throwable;
}
