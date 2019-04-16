package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.Mixin;
import cn.xishan.oftenporter.porter.core.annotation.MixinTo;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;

import java.util.List;

/**
 * porter类继承,另见:{@linkplain Mixin},{@linkplain MixinTo}。
 *
 * @author Created by https://github.com/CLovinr on 2019-04-16.
 */
public interface MixinListener
{
    void beforeStartOfMixin(List<Porter> mixins);

    void afterStartOfMixin(List<Porter> mixins);
}
