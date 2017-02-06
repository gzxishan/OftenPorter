package cn.xishan.oftenporter.porter.local.mixin;

import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.simple.parsers.IntParser;

/**
 * @author Created by https://github.com/CLovinr on 2017/2/6.
 */
@Parser({
        @Parser.parse(paramNames = "age", parser = IntParser.class)
})
public class MinxParserTest
{
}
