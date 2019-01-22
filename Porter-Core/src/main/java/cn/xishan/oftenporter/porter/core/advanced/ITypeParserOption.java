package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.NotNull;

/**
 * <ol>
 * <li>
 * 对于声明的参数名可以添加参数，格式为:1)"varName"、2)"varName()"、3)"varName( varConfigContent )".
 * {@linkplain #getNameConfig()}分别返回:1)null,2)"",3)"varConfigContent"会去除varConfigContent前后的空白符
 * (使用{@linkplain String#trim()}若去除空白符后为空字符串，结果同2))。对于参数的意义，要看具体的实现者。
 * </li>
 * <li>
 * 默认值："varName[默认值]"
 * </li>
 * <li>
 *     括号：使用转义字符如"varName[\\[\\]]"
 * </li>
 * <li>
 * 例子："sex(0,1,2)[0]"表示字符串变量（未绑定其他类型情况下）sex只能取值"0"或"1"或"2"、且默认为"0"
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2017/12/30.
 */
public interface ITypeParserOption
{

    @NotNull
    String getNameConfig();
}

