package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.deal._NeceUnece;
import cn.xishan.oftenporter.porter.core.base.InNames.Name;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 默认的参数处理实现。
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public class DefaultParamDealt implements ParamDealt
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParamDealt.class);

    @Override
    public FailedReason deal(OftenObject oftenObject, Name[] names, Object[] values, boolean isNecessary,
            ParamSource paramSource,
            TypeParserStore typeParserStore, String namePrefix)
    {
        for (int i = 0; i < names.length; i++)
        {
            Name name = names[i];
            _NeceUnece neceUnece = isNecessary ? name.getNeceUnece() : null;
            Object value = getParam(oftenObject, namePrefix, name, paramSource, typeParserStore.byId(name.typeParserId),
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
            } else if (isNecessary && (neceUnece == null || name.isNece(oftenObject)))
            {
                return DefaultFailedReason.lackNecessaryParams("Lack necessary params!", namePrefix + name.varName);
            }
        }
        return null;
    }

    public static Object getParam(OftenObject oftenObject, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt)
    {
        return getParam(oftenObject, null, theName, paramSource, typeParser, dealt);
    }

    public static Object getParam(OftenObject oftenObject, String namePrefix, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt)
    {
        String name = namePrefix == null ? theName.varName : namePrefix + theName.varName;
        Object v = paramSource.getParam(name);
        if (v == null)
        {
            try
            {
                v = theName.getTypeObject(oftenObject, paramSource);//见PortUtil.paramDealOne
            } catch (Throwable e)
            {
                e = OftenTool.getCause(e);
                LOGGER.warn(e.getMessage(), e);
                return DefaultFailedReason.parseOftenEntitiesException(e.getMessage());
            }
            if(v instanceof FailedReason){
                return v;
            }
        }

        if (OftenTool.isNullOrEmptyCharSequence(v))
        {
            v = theName.getDefaultValue();
        }

        if (typeParser != null)
        {
            if (OftenTool.isNullOrEmptyCharSequence(v))
            {
                ITypeParser.ParseResult parseResult = typeParser.parseEmpty(oftenObject, name, dealt);
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
                ITypeParser.ParseResult parseResult = typeParser.parse(oftenObject, name, v, dealt);
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
        if ((v instanceof CharSequence) && v.equals(""))
        {
            v = null;
        }
        return v;
    }
}
