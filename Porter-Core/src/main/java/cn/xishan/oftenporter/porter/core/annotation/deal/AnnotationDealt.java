package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.base.InNames;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.PortUtil;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class AnnotationDealt
{
    private boolean enableDefaultValue;

    /**
     * @param enableDefaultValue 是否允许{@linkplain PortIn#value()}取默认值。
     */
    public AnnotationDealt(boolean enableDefaultValue)
    {
        this.enableDefaultValue = enableDefaultValue;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationDealt.class);

    public _AutoSet autoSet(Field field)
    {
        AutoSet autoSet = field.getAnnotation(AutoSet.class);
        if (autoSet == null)
        {
            return null;
        }
        _AutoSet _autoSet = new _AutoSet();
        _autoSet.classValue = autoSet.classValue();
        _autoSet.value = autoSet.value();
        _autoSet.range = autoSet.range();
        return _autoSet;
    }

    public _Nece nece(Field field)
    {
        PortInObj.Nece nece = field.getAnnotation(PortInObj.Nece.class);
        if (nece == null)
        {
            return null;
        }
        _Nece _nece = new _Nece();
        if("".equals(nece.value())){
            _nece.value=field.getName();
        }else {
            _nece.value = nece.value();
        }
        return _nece;
    }

    public _UnNece unNece(Field field)
    {
        PortInObj.UnNece unNece = field.getAnnotation(PortInObj.UnNece.class);
        if (unNece == null)
        {
            return null;
        }
        _UnNece _unNece = new _UnNece();
        if("".equals(unNece.value())){
            _unNece.value=unNece.value();
        }else{
            _unNece.value = unNece.value();
        }
        return _unNece;
    }

    public _PortInObj portInObj(Method method)
    {
        return to_PortInObj(AnnoUtil.getAnnotation(method, PortInObj.class));
    }

    public _PortInObj portInObj(Class<?> clazz)
    {
        return to_PortInObj(clazz.getAnnotation(PortInObj.class));
    }

    private _PortInObj to_PortInObj(PortInObj portInObj)
    {
        if (portInObj == null)
        {
            return null;
        }
        _PortInObj _portInObj = new _PortInObj();
        _portInObj.value = portInObj.value();
        return _portInObj;
    }

    public _Parser parser(Method method)
    {
        return to_parser(AnnoUtil.getAnnotation(method, Parser.class));
    }

    public _Parser parser(Class<?> clazz)
    {
        return to_parser(clazz.getAnnotation(Parser.class));
    }

    private _Parser to_parser(Parser parser)
    {
        if (parser == null)
        {
            return null;
        }
        _Parser _parser = new _Parser();
        _parse[] ps = new _parse[parser.value().length];
        int i = 0;
        for (Parser.parse p : parser.value())
        {
            ps[i++] = to_parse(p);
        }
        _parser._parses = ps;
        return _parser;
    }

    public _parse parse(Method method)
    {
        return to_parse(AnnoUtil.getAnnotation(method, Parser.parse.class));
    }

    public _parse parse(Field field)
    {
        return to_parse(field.getAnnotation(Parser.parse.class));
    }

    private _parse to_parse(Parser.parse parse)
    {
        if (parse == null)
        {
            return null;
        }
        _parse _p = new _parse();
        _p.varName = parse.varName();
        _p.parserName = parse.parserName();
        _p.parserClass = parse.parser();
        return _p;
    }

    public _parse parse(Class<?> clazz)
    {
        return to_parse(clazz.getAnnotation(Parser.parse.class));
    }

    public _PortDestroy portDestroy(Method method)
    {
        PortDestroy portDestroy = AnnoUtil.getAnnotation(method, PortDestroy.class);
        if (portDestroy == null)
        {
            return null;
        }
        _PortDestroy _portDestroy = new _PortDestroy();
        _portDestroy.method = method;
        _portDestroy.order = portDestroy.order();
        return _portDestroy;
    }

    public _PortStart portStart(Method method)
    {
        PortStart portStart = AnnoUtil.getAnnotation(method, PortStart.class);
        if (portStart == null)
        {
            return null;
        }
        _PortStart _portStart = new _PortStart();
        _portStart.method = method;
        _portStart.order = portStart.order();
        return _portStart;
    }

    public _PortOut portOut(Method method)
    {
        _PortOut _portOut = new _PortOut();
        PortOut portOut = AnnoUtil.getAnnotation(method, PortOut.class);
        if (portOut != null)
        {
            _portOut.outType = portOut.value();
        } else
        {
            _portOut.outType = OutType.Object;
        }
        return _portOut;
    }

    public _PortIn portIn(Class<?> clazz)
    {
        PortIn portIn = clazz.getAnnotation(PortIn.class);
        if (portIn == null)
        {
            return null;
        }
        _PortIn _portIn = new _PortIn();
        _portIn.tiedName = PortUtil.tied(portIn, clazz, enableDefaultValue);
        _portIn.inNames = InNames.fromStringArray(portIn.nece(), portIn.unnece(), portIn.inner());
        _portIn.method = portIn.method();
        _portIn.checks = portIn.checks();
        _portIn.tiedType = portIn.tiedType();
        _portIn.isMultiTiedType = portIn.multiTiedType();
        _portIn.ignoreTypeParser=portIn.ignoreTypeParser();
        _portIn.newObjectWhenInit=portIn.newObjectWhenInit();

        LOGGER.debug("tiedName={},tiedType={},method={}", _portIn.tiedName, _portIn.tiedType, _portIn.method);

        return _portIn;
    }

    public _PortIn portIn(Method method, _PortIn class_PortIn)
    {
        _PortIn _portIn = null;
        PortIn portIn = AnnoUtil.getAnnotation(method, PortIn.class);
        if (portIn != null && (class_PortIn.isMultiTiedType() || class_PortIn.getTiedType() != TiedType.REST || portIn
                .tiedType() == TiedType.REST))
        {
//            Class<?>[] parameters = method.getParameterTypes();
//            if (parameters.length > 1 || parameters.length == 1 && !WObject.class.equals(parameters[0]))
//            {
//                throw new IllegalArgumentException("the parameter list of " + method + " is illegal!");
//            }
            _portIn = new _PortIn();
            _portIn.tiedType = portIn.tiedType();
            _portIn.tiedName = PortUtil.tied(portIn, method,
                    TiedType.type(class_PortIn.getTiedType(),
                            _portIn.getTiedType()) == TiedType.REST || enableDefaultValue);
            _portIn.inNames = InNames.fromStringArray(portIn.nece(), portIn.unnece(), portIn.inner());
            _portIn.checks = portIn.checks();
            _portIn.method = AnnoUtil.method(class_PortIn.getMethod(), portIn.method());
            _portIn.ignoreTypeParser=portIn.ignoreTypeParser();
            _portIn.newObjectWhenInit=portIn.newObjectWhenInit();

        }
        return _portIn;
    }
}
