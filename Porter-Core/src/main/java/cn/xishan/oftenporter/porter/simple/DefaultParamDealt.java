package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ParamDealt;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.deal._NeceUnece;
import cn.xishan.oftenporter.porter.core.base.InNames.Name;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * 默认的参数处理实现。
 * Created by https://github.com/CLovinr on 2016/9/3.
 */
public class DefaultParamDealt implements ParamDealt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParamDealt.class);
    public static final ParamSource EMPTY_PARAM_SOURCE = new ParamSource() {
        @Override
        public void setUrlResult(UrlDecoder.Result result) {

        }

        @Override
        public <T> T getParam(String name) {
            return null;
        }

        @Override
        public <T> T getNeceParam(String name, String errmsgOfEmpty) {
            return null;
        }

        @Override
        public <T> T getNeceParam(String name) {
            return null;
        }

        @Override
        public void putNewParams(Map<String, ?> newParams) {

        }

        @Override
        public Enumeration<String> paramNames() {
            return new EnumerationImpl<String>(Collections.emptyList());
        }

        @Override
        public Enumeration<Map.Entry<String, Object>> params() {
            return new EnumerationImpl<Map.Entry<String, Object>>(Collections.emptyList());
        }
    };

    @Override
    public FailedReason deal(OftenObject oftenObject, Name[] names, Object[] values, boolean isNecessary,
            ParamSource paramSource, TypeParserStore typeParserStore, String namePrefix) {
        if (paramSource == null) {
            paramSource = EMPTY_PARAM_SOURCE;
        }

        for (int i = 0; i < names.length; i++) {
            Name name = names[i];
            _NeceUnece neceUnece = isNecessary ? name.getNeceUnece() : null;
            Object value;
            String varName = namePrefix == null ? name.varName : namePrefix + name.varName;

            if (name.isRequestData()) {
                value = oftenObject == null ? null : oftenObject.getRequestData(varName);
            } else {
                value = getParam(oftenObject, namePrefix, name, paramSource, typeParserStore.byId(name.typeParserId),
                        name.getDealt());
            }

            if (value != null) {
                if (value instanceof FailedReason) {
                    return (FailedReason) value;
                } else {
                    values[i] = value;
                }
            } else if (isNecessary && (neceUnece == null || name.isNece(oftenObject))) {
                return DefaultFailedReason.lackNecessaryParams("Lack necessary params!", varName);
            }
        }
        return null;
    }

    public static Object getParam(OftenObject oftenObject, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt) {
        return getParam(oftenObject, null, theName, paramSource, typeParser, dealt);
    }

    public static Object getParam(OftenObject oftenObject, String namePrefix, Name theName, ParamSource paramSource,
            ITypeParser typeParser, Object dealt) {
        if (paramSource == null) {
            paramSource = EMPTY_PARAM_SOURCE;
        }

        String name = namePrefix == null ? theName.varName : namePrefix + theName.varName;
        Object v = paramSource.getParam(name);
        if (v == null) {
            try {
                v = theName.getTypeObject(oftenObject, paramSource);//见PortUtil.paramDealOne
            } catch (Throwable e) {
                e = OftenTool.getCause(e);
                LOGGER.warn(e.getMessage(), e);
                return DefaultFailedReason.parseOftenEntitiesException(e.getMessage());
            }
            if (v instanceof FailedReason) {
                return v;
            }
        }

        if (OftenTool.isNullOrEmptyCharSequence(v)) {
            v = theName.getDefaultValue();
        }

        if (typeParser != null) {
            if (OftenTool.isNullOrEmptyCharSequence(v)) {
                ITypeParser.ParseResult parseResult = typeParser.parseEmpty(oftenObject, name, dealt);
                if (parseResult != null) {
                    if (parseResult.isLegal()) {
                        Object obj = parseResult.getValue();
                        if (obj instanceof ITypeParser.DecodeParams) {
                            ITypeParser.DecodeParams decodeParams = (ITypeParser.DecodeParams) obj;
                            Map<String, Object> map = decodeParams.getParams();
                            paramSource.putNewParams(map);
                            v = map.get(name);
                        } else {
                            v = obj;
                        }
                    } else {
                        return DefaultFailedReason.illegalParams(parseResult.getFailedDesc(), name);
                    }
                }
            } else {
                ITypeParser.ParseResult parseResult = typeParser.parse(oftenObject, name, v, dealt);
                if (parseResult.isLegal()) {
                    Object obj = parseResult.getValue();
                    if (obj instanceof ITypeParser.DecodeParams) {
                        ITypeParser.DecodeParams decodeParams = (ITypeParser.DecodeParams) obj;
                        Map<String, Object> map = decodeParams.getParams();
                        paramSource.putNewParams(map);
                        v = map.get(name);
                    } else {
                        v = obj;
                    }
                } else {
                    return DefaultFailedReason.illegalParams(parseResult.getFailedDesc(), name);
                }
            }
        }

        v = theName.dealString(v);

        if ((v instanceof CharSequence) && v.equals("")) {
            v = null;
        }
        return v;
    }
}
