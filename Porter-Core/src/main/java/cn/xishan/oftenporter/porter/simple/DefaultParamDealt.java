package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.Map;

import cn.xishan.oftenporter.porter.core.base.InNames.Name;

/**
 * 默认的参数处理实现。
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public class DefaultParamDealt implements ParamDealt
{
    @Override
    public FailedReason deal(WObject wObject, Name[] names, _Nece[] neceDeals, Object[] values, boolean isNecessary,
            ParamSource paramSource,
            TypeParserStore typeParserStore,String namePrefix)
    {
        for (int i = 0; i < names.length; i++)
        {
            Name name = names[i];
            Object value = getParam(namePrefix+name.varName, paramSource, typeParserStore.byId(name.typeParserId));
            if (value != null)
            {
                if (value instanceof FailedReason)
                {
                    return (FailedReason) value;
                } else
                {
                    values[i] = value;
                }
            } else if (isNecessary && (neceDeals == null || neceDeals[i].isNece(wObject)))
            {
                return DefaultFailedReason.lackNecessaryParams("Lack necessary params!", name.varName);
            }
        }
        return null;
    }

    public Object getParam(String name, ParamSource paramSource,
            ITypeParser typeParser)
    {

        Object v = paramSource.getParam(name);
        if (!WPTool.isEmpty(v) && typeParser != null)
        {
            ITypeParser.ParseResult parseResult = typeParser.parse(name, v);
            if (parseResult.isLegal())
            {
                Object obj = parseResult.getValue();
                if (obj instanceof ITypeParser.DecodeParams)
                {
                    ITypeParser.DecodeParams decodeParams = (ITypeParser.DecodeParams) obj;
                    Map<String, Object> map = decodeParams.getParams();
                    paramSource.putNewParams(map);
                    v = map.get(name);
                } else
                {
                    v = obj;
                }
            } else
            {
                return DefaultFailedReason.illegalParams(parseResult.getFailedDesc(), name);
            }
        }
        if ("".equals(v))
        {
            v = null;
        }
        return v;
    }
}
