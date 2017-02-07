package cn.xishan.oftenporter.demo.core.test5mix.mixinloop;

import cn.xishan.oftenporter.porter.core.annotation.Mixin;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
@PortIn
@Mixin({Inner2Porter.class})
public class Root2Porter
{
}
