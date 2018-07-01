package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

/**
 * <pre>
 *  1.对于声明的参数名可以添加参数，格式为:1)"varName"、2)"varName()"、3)"varName( varConfigContent )".
 *  {@linkplain #getNameConfig()}分别返回:1)null,2)"",3)"varConfigContent"会去除varConfigContent前后的空白符(使用{@linkplain String#trim()}若去除空白符后为空字符串，结果同2))
 *  2.对于参数的意义，要看具体的实现者。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/12/30.
 */
public interface ITypeParserOption
{

    @NotNull
    String getNameConfig();
}

