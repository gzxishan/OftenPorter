package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.ITypeParser;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _parse
{
    String varName;
    String parserName;
    Class<? extends ITypeParser> parserClass;

    public String getVarName()
    {
        return varName;
    }

    public Class<? extends ITypeParser> getParserClass()
    {
        return parserClass;
    }

    public String getParserName()
    {
        return parserName;
    }
}
