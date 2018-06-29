package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

/**
 * 见{@linkplain AnnoUtil#pushAnnotationConfigable(Object, IAnnotationConfigable)},{@linkplain PorterConf#setAnnotationConfig(Object)}
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public interface IAnnotationConfigable<T>
{
    String getValue(T config, String value);
}
