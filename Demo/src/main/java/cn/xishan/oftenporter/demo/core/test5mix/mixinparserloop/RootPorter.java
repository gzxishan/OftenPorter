package cn.xishan.oftenporter.demo.core.test5mix.mixinparserloop;

import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
@PortIn
@Parser.MixinParser(RootPorter.class)
public class RootPorter
{
}
