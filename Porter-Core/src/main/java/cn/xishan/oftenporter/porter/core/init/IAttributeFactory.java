package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
public interface IAttributeFactory
{
    IAttribute getIAttribute(WObject wObject);
}
