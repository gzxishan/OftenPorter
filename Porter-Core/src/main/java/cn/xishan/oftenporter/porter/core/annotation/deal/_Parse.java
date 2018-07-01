package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _Parse
{
    String[] paramNames;
    String parserName;
    Class<? extends ITypeParser> parserClass;

    public _Parse()
    {
    }

    public String[] getParamNames()
    {
        return paramNames;
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
