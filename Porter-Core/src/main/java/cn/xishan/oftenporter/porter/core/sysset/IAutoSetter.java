package cn.xishan.oftenporter.porter.core.sysset;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet.SetOk;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.exception.AutoSetException;

/**
 * 不含@{@linkplain PortStart}事件。会执行{@linkplain SetOk}、{@linkplain PortDestroy}事件。
 *
 * @author Created by https://github.com/CLovinr on 2019-05-14.
 */
public interface IAutoSetter
{
    /**
     * 对实例进行注入处理（包括静态变量）。
     *
     * @param objects
     * @throws AutoSetException
     */
    void forInstance(Object[] objects) throws AutoSetException;

    /**
     * 对类的静态变量进行注入处理。
     *
     * @param classes
     * @throws AutoSetException
     */
    void forClass(Class[] classes) throws AutoSetException;
}
