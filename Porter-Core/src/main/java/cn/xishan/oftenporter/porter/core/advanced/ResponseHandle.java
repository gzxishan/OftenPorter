package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * @author Created by https://github.com/CLovinr on 2018-09-11.
 */
public interface ResponseHandle
{
    boolean hasDoneWrite(WObject wObject, PorterOfFun porterOfFun, @NotNull Object object);
}
