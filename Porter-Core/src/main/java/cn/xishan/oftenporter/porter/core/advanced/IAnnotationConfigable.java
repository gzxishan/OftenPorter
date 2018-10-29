package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

/**
 * 见{@linkplain AnnoUtil#pushAnnotationConfigable(IAnnotationConfigable)}
 *
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
public interface IAnnotationConfigable
{
    String getAnnotationStringValue(String value);

    IConfigData getConfigData();

}
