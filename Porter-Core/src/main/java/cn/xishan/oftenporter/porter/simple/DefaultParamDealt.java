package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.deal._Nece;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.Map;

import cn.xishan.oftenporter.porter.core.base.InNames.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的参数处理实现。
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public class DefaultParamDealt implements ParamDealt
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParamDealt.class);

    @Override
    public FailedReason deal(WObject wObject, Name[] names, Object[] values, boolean isNecessary,
            ParamSource paramSource,
            TypeParserStore typeParserStore, String namePrefix)
    {
        for (int i = 0; i < names.length; i++)
        {
            Name name = names[i];
            _Nece nece = isNecessary ? name.getNece() : null;
            Object value = getParam(wObject, namePrefix, name, paramSource, typeParserStore.byId(name.typeParserId),
                    name.getDealt());
            if (value != null)
            {
                if (value instanceof FailedReason)
                {
                    return (FailedReason) value;
                } else
                {
                    values[i] = value;
                }
            } else if (isNecessary && (nece == null || nece.isNece(wObject)))
            {
                return DefaultFailedReason.lackNecessaryParams("Lack necessary params!", namePrefix + name.varName);
            }
        }
        return null;
    }

    public static Object getParam(WObject wObject, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt)
    {
        return getParam(wObject, null, theName, paramSource, typeParser, dealt);
    }

    public static Object getParam(WObject wObject, String namePrefix, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt)
    {
        String name = namePrefix == null ? theName.varName : namePrefix + theName.varName;
        Object v = paramSource.getParam(name);
        if (v == null)
        {
            try
            {
                v = theName.getTypeObject(wObject, paramSource);//见PortUtil.paramDealOne
            } catch (Throwable e)
            {
                e = WPTool.getCause(e);
                LOGGER.warn(e.getMessage(), e);
                return DefaultFailedReason.parseOftenEntitiesException(e.getMessage());
            }
            if(v instanceof FailedReason){
                return v;
            }
        }

        if (WPTool.isEmpty(v))
        {
            v = theName.getDefaultValue();
        }
        if (typeParser != null)
        {
            if (WPTool.isEmpty(v))
            {
                ITypeParser.ParseResult parseResult = typeParser.parseEmpty(wObject, name, dealt);
                if (parseResult != null)
                {
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
            } else
            {
                ITypeParser.ParseResult parseResult = typeParser.parse(wObject, name, v, dealt);
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
        }
        if (v != null && (v instanceof CharSequence) && v.equals(""))
        {
            v = null;
        }
        return v;
    }
}
