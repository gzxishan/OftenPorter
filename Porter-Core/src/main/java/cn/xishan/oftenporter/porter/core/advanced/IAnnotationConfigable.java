package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

/**
 * 见{@linkplain AnnoUtil#pushAnnotationConfigable(IConfigData, IAnnotationConfigable)}
 *
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public interface IAnnotationConfigable<T>
{
    String getValue(@MayNull IConfigData configData, String value);

    /**
     * 见{@linkplain PorterConf#setAnnotationConfig(Object)}
     *
     * @param configObject
     * @return
     */
    IConfigData getConfig(T configObject);

    void isConfig(Object configObject) throws RuntimeException;
}
